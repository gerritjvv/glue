package org.glue.unit.om.impl

import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.script.ScriptClassCache

/**
 * 
 * To enable singleton modules to be thread safe the GlueContext is often required on each method call<br/>
 * that the client might make to it, but we do not wan't to pass this explicitly inside the work flow itself.
 * To solve this each module is wrapped inside a proxy that will capture each method call from the work flow and<br/>
 * if the module method starts with a GlueContext argument the appropriate context is passed to the method.
 *
 */
@Typed(TypePolicy.DYNAMIC)
class GlueModuleProxy {

	static {
		ExpandoMetaClass.enableGlobally()
	}
	
	GlueModule module
	GlueContext context

	Map<String, MetaMethod> contextMethods = [:]
	Map<String, MetaMethod> noncontextMethods = [:]
	
	
	GlueModule getModule(){
		return module
	}
	
	/**
	 * Creates a GlueModule proxy instance
	 * @param module
	 * @param context
	 * @return GlueModule
	 */
	public static GlueModule createProxy(GlueModule module, GlueContext context){

		GlueModuleProxy proxy = new GlueModuleProxy(module, context)
		GlueModule proxyModule = ProxyGenerator.INSTANCE.instantiateDelegate([GlueModule], proxy)
		
		return proxyModule
	}

	public GlueModuleProxy(GlueModule module, GlueContext context) {
		super();
		this.module = module;
		this.context = context;
	}


	def propertyMissing(name){
		module.getAt(name as String)
	}
	
	def methodMissing(String methodName, Object args){
		return findAndInvokeMethod(methodName as String, args)
	}

	/**
	 * Find the correct method and invokes it
	 * @param methodName
	 * @param args 
	 * @return Object the invoked method's return value
	 */
	private Object findAndInvokeMethod(String methodName, Object[] args){

		//closure to find method with args, or context + args
		def findMethod = { MetaMethod method, Map<Object, Object> map ->
			/*
			 * Map contains 3 variables
			 * foundArgs = the arguments for which to invoke the method if foundMethod == true
			 * foundMethod = if true a method with matching arguments was found
			 * argClasses = the classes of the argument types for the found method
			 */
			//(1) check args normally
			def foundArgs = map.foundArgs
			def foundMethod = false
			def argClasses = createClassList(foundArgs)

			foundMethod = isMethodParameters( method, argClasses )

			if(!foundMethod && isArrayArgument(foundArgs)){

				foundArgs = unwrapArray(foundArgs)
				argClasses = createClassList(foundArgs)
				foundMethod = isMethodParameters(method, argClasses)
			}

			map.argClasses = argClasses
			map.foundArgs = foundArgs
			map.foundMethod = foundMethod
		}

		//find method if it requires a context arg
		def findMethodWithContextArg = { MetaMethod method, Map<Object, Object> map ->
			def foundArgs = map.foundArgs
			def foundMethod = false

			//try with conntext
			map.foundContextArgs = createArgumentList(context, foundArgs)
			def argClasses = createClassList(map.foundContextArgs)

			foundMethod = isMethodParameters( method, argClasses )
			map.foundMethodWithContext = foundMethod
			
			//(3) check if the args contains one array
			if(!foundMethod && isArrayArgument(foundArgs)){

				foundArgs = createArgumentList(context, unwrapArray(foundArgs))
				argClasses = createClassList(foundArgs)
				foundMethod = isMethodParameters(method, argClasses)
			}

			if(!foundMethod && Collection.class.isAssignableFrom(map.foundArgs.class)){
				foundArgs = [context, map.foundArgs]
				argClasses = [GlueContext.class, map.foundArgs.class] as Class[]
				foundMethod = isMethodParameters(method, argClasses)
			}
			
			
			map.argClasses = argClasses
			map.foundArgs = foundArgs
			map.foundMethod = foundMethod
		}



		//for each method search the method name
		for(MetaMethod method in module.metaClass.methods){

			if(method.name != methodName){
				continue
			}
			//we create a new map every time a similiar method is found
			def methodFindArgMap = [foundArgs:args]

			//CHECK Method Parameters
			findMethod(method, methodFindArgMap)
			if(!methodFindArgMap.foundMethod){
				findMethodWithContextArg(method, methodFindArgMap)
			}

			if(methodFindArgMap.foundMethod){
				//if the method is found invoke
				def invokeArgs = (methodFindArgMap.foundMethodWithContext)? methodFindArgMap.foundContextArgs :	methodFindArgMap.foundArgs

				//add a closure that will directly pass this method to the module's found method
				//for future reference this is caching.
				createCachedMethod(method)

				return method.doMethodInvoke(module, invokeArgs as Object[])

			}
		}

		//if we reached this stage the foundMethod is also false
		throw new MissingMethodException(methodName, module.getClass(), args)
	}


	/**
	 * Creates a method definition with the argument types of the MetaMethod argument<br/>
	 * and adds this method to the current proxy instance.
	 * @param method
	 */
	private final void createCachedMethod(MetaMethod method){

		def argumentList = []
		def argumentNames = []
		method.nativeParameterTypes?.eachWithIndex { Class cls, int index ->

			def name = cls.name
			if(cls.isArray()){
				name = "${cls.componentType.name}[]"
			}

			argumentList << "$name var_$index"
			argumentNames << "var_$index"
		}

		//create method definition that has the same signature as the module's method
		//and then calls the module's method with the same arguments
		//its important we call getModule() and not module or else module is seen part
		//of the ConfigObject by groovy.
		def methodDefinitionStr = """
		
		   methodDefinition={ ${argumentList.join(',')} ->
				getModule().${method.name}(${argumentNames.join(',')})
		   }
		
		"""
		//we use the ScriptClassCache to cache scripts so that simular methods do not raise new classes to perm gen.
		getMetaClass()."${method.name}" = ScriptClassCache.getDefaultInstance().parse(methodDefinitionStr).methodDefinition
        
	}

	/**
	 * Create a Class array from an array of objects
	 * @param args we leave args without type so that groovy does not convert it on method passing.
	 * @return Class[]
	 */
	@SuppressWarnings("rawtypes")
	private Class[] createClassList(args){

		int len = args.size()
		def classes = new Class[len]
		
		for(int i = 0; i < len; i++){
				if(args[i] != null)
					classes[i] = args[i].getClass()
		}

		return classes
	}

	/**
	 * If the args parameter contains only an array true is returned<br/>
	 * The inner array cannot have more than 100 items.
	 * @param args 
	 * @return boolean
	 */
	private boolean isArrayArgument(args){
		(args && args.size() == 1 && (args[0] instanceof Object[] || java.util.Collection.isAssignableFrom(args[0].class)) && args[0].size() < 100)
	}

	/**
	 * Create an Object array containing [0] = glueContext and the rest is filled with the args.
	 * @param glueContext
	 * @param args We leave args without type to ensure ArrayList or Object[] types are not converted again.
	 * @return Object[] or ArrayList
	 */
	private createArgumentList(GlueContext glueContext, args){

		def augmentedArguments = []
		augmentedArguments << glueContext
		args.each{ augmentedArguments << it }
		return augmentedArguments as Object[]
	}

	/**
	 * Takes the first index item and expect it to be an array. This array is return returned.
	 * @param arr we leave arr without type.
	 * @return Object
	 */
	def unwrapArray(arr){
		return arr[0]
	}

	/**
	 * Helper method that runs method.checkParameters.<br/>
	 * If an illeagal argument exception is thrown this method return false,<br/>
	 * else true
	 * @param method
	 * @param params
	 * @return boolean
	 */
	@SuppressWarnings("rawtypes")
	private static final boolean isMethodParameters(MetaMethod method, Class[] params){
		try{
			method.checkParameters params
			true
		}catch(IllegalArgumentException e1){
			false
		}
	}
}
