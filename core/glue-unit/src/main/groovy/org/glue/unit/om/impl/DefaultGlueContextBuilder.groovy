package org.glue.unit.om.impl

import java.util.Map.Entry

import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.GlueUnit
import org.glue.unit.status.GlueUnitStatusManager

/**
 *
 * Provides a single place where the GlueContext is built.
 *
 */
@Typed
class DefaultGlueContextBuilder implements GlueContextBuilder{

	GlueModuleFactoryProvider moduleFactoryProvider
	GlueUnitStatusManager statusManager
	GlueExecLoggerProvider loggerProvider

	public DefaultGlueContextBuilder(){
	}

	public DefaultGlueContextBuilder(GlueModuleFactoryProvider moduleFactoryProvider){
		this(moduleFactoryProvider, null)
	}


	public DefaultGlueContextBuilder(GlueModuleFactoryProvider moduleFactoryProvider, GlueUnitStatusManager statusManager){
		this.moduleFactoryProvider = moduleFactoryProvider
		this.statusManager = statusManager
	}

	public DefaultGlueContextBuilder(GlueModuleFactoryProvider moduleFactoryProvider, GlueUnitStatusManager statusManager,
	GlueExecLoggerProvider loggerProvider){
		this.moduleFactoryProvider = moduleFactoryProvider
		this.statusManager = statusManager
		this.loggerProvider = loggerProvider
	}

	/**
	 * Normally the GlueContext modules are dynamically searched during call time.<br/>
	 * This method creates a String template class with all the defined modules identified and their module names added as methods to the glue
	 */
	@Typed(TypePolicy.MIXED)
	static GlueContext buildStaticGlueContext(GlueContext ctx){

		String s = """
		   package org.glue.unit.om.impl;

		   class GlueContextStatic extends org.glue.unit.om.impl.GlueContextWrapper{
            
		 	GlueContextStatic(org.glue.unit.om.GlueContext parent){super(parent); this.parent = parent}
		 	
         """


		for(Entry<String, GlueModule> entry in ctx.getModuleFactory().getAvailableModules().entrySet()){
			String name = entry.key
			GlueModule module = entry.value
			String clsName
			if(module.getClass().name.endsWith("GlueModuleProxy_delegateProxy"))
				clsName = module.module.getClass().name
			else
				clsName = module.getClass().name

			s += """

				
				//def $clsName ${name}(){
                def org.glue.unit.om.GlueModule ${name}(){
                  	def module = parent.getModuleFactory().getModule(\'${name}\')

					if(module?.getClass()?.name?.endsWith("GlueModuleProxy_delegateProxy")){
					    println('mymodule:1: ' + module?.module.class + " module " + module + " instanceof " + (module?.module instanceof org.glue.unit.om.GlueModule))
					  	return module?.module
					}else{
					    println('mymodule:2: ' + module.class   + " instanceof " + (module instanceof $clsName))
                        return module
                    }

                } 

			"""
		}

		s += """
               
           }

		 """
		println("static context: " + s)
		new GroovyClassLoader(ctx.getClass().getClassLoader()).parseClass(s).newInstance(ctx)
	}


	/**
	 * Normally the GlueContext modules are dynamically searched during call time.<br/>
	 * This method creates a String template class with all the defined modules identified and their module names added as methods to the glue
	 */
	@Typed(TypePolicy.MIXED)
	static Map buildStaticGlueContextMap(GlueContext ctx){


		def ctxm = ["unit":ctx.unit, "unitId":ctx.unitId]

		for(Entry<String, GlueModule> entry in ctx.getModuleFactory().getAvailableModules().entrySet()){
			String name = entry.key
			GlueModule module = entry.value

			if(module?.getClass()?.name?.endsWith("GlueModuleProxy_delegateProxy"))
				module = module?.module
			ctxm[name] = module
		}

		return ctxm
	}

	GlueContext build(String unitId, GlueUnit unit, Map<String,String> params){

		//with each GlueContext we create a unique instance of the moduleFactory
		//unique here means that all non singleton modules will be instantiated and
		//configured a new for this GlueContextInstance
		GlueContextImpl context = new GlueContextImpl()
		context.unitId = unitId
		context.unit = unit
		context.args = params
		context.statusManager = statusManager
		context.moduleFactory = moduleFactoryProvider?.get(context)
		context.logger = loggerProvider?.get(unitId)

		return context
	}
}
