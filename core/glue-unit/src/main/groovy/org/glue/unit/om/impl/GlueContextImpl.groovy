package org.glue.unit.om.impl


import java.util.concurrent.ConcurrentHashMap

import org.glue.unit.exceptions.ProcessStopException
import org.glue.unit.log.GlueExecLogger
import org.glue.unit.om.DefaultGlueContext
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.status.GlueUnitStatusManager
import org.json.JSONObject
import org.json.JSONWriter

/**
 *
 * Each context instance is associated with a unit execution. i.e. multiple instances might exist for a glue unit<br/>
 * each corresponding to one execution.<br/>
 * 
 * Once the glue unit execution has completed the context's destroy method is called.<br/>
 * Modules can be singleton i.e. the same module instance is used for all context instances, or non singleton i.e.<br/>
 * for each new context instance a new instance of the module is created.<br/>
 * It is the responsibility of the moduleFactory and the GlueContextBuilder to ensure the instances are correctly managed.<br/>
 * 
 * 
 *
 */

class GlueContextImpl extends DefaultGlueContext{


	Map<String, Class> classCache = new ConcurrentHashMap<String, Class>();

	Map<String, Object> propertyMap = new ConcurrentHashMap<String, Object>();
	//this is to remember the variables that have been set
	Set<String> variables = []

	GlueUnit unit
	Map<String, String> args
	
	String unitId
	GlueModuleFactory moduleFactory
	GlueUnitStatusManager statusManager

	GlueExecLogger logger

	/**
	 * Calls a static method on the string class name provided
	 */
	def eval(className, method, values = null){
		Class cls = classCache[className]
		if(!cls){
			cls = Thread.currentThread().getContextClassLoader().loadClass(className)
			classCache[className] = cls
		}

		cls.invokeMethod(method, values)
	}


	/**
	 * Calls a static method on the string class name provided
	 */
	def newInstance(className, args = null){
		Class cls = classCache[className]
		if(!cls){
			cls = Thread.currentThread().getContextClassLoader().loadClass(className)
			classCache[className] = cls
		}

		if(args)
			cls.newInstance(args)
		else
			cls.newInstance()
	}


	void setProperty(String name, Object value){
		if(name == 'unitId'){
			unitId = value
		}else if(name =='moduleFactory'){
			moduleFactory = value
		}else if(name =='statusManager'){
			statusManager = value
		}else if(name == 'logger'){
			logger = value
		}else{
			if(value == null){
				propertyMap.remove name
			}else{
				propertyMap.put(name, value)
			}
			variables << name
		}
	}

	/**
	 * Will call destroy on the GlueModuleFactory, but this should not cause singleton instances to be destroyed.<br/>
	 * 
	 */
	void destroy(){
		classCache?.clear()
		propertyMap?.clear()
		variables.clear()
		moduleFactory?.destroy()
	}


	/**
	 * 	Allow modules to be called as ctx.mymodule
	 * 	@param name
	 * 	@return
	 */
	def propertyMissing(String name) {

		Object obj = propertyMap[name]

		//only search if not a variable
		if(!(obj || variables.contains(name)))	{
			Object module = moduleFactory?.getAvailableModules()?.get(name)

			if(!module){
				throw new NullPointerException("Module " + name + " cannot be found")
			}

			propertyMap[name]= module

			obj = module
		}


		return obj
	}


	void stop(String msg = null){
		throw new ProcessStopException(msg)
	}

	void error(String msg = null, Throwable t = null){
		throw new RuntimeException(msg, t)
	}

	void write(Writer writer) throws IOException{

		def json = new JSONWriter(writer)
		json.object()

		propertyMap.each { key, value ->
			json.key key
			json.value value
		}

		json.endObject()
	}

	void load(Reader reader) throws IOException{

		def json = new JSONObject(reader?.text)
		json.keys().each { key ->  propertyMap.putAt( key, json.get( key ) ) }
	}
}
