package org.glue.unit.om.impl;

import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.apache.log4j.Logger

/**
 * Manages a map of module instances
 */
@Typed
class GlueModuleFactoryImpl implements GlueModuleFactory {
	static final Logger log = Logger.getLogger(GlueModuleFactoryImpl.class)

	Map<String, GlueModule> modules =new HashMap<String,GlueModule>();

	/**
	 * References the prototype modules.
	 * These module will be destroyed when this factory's destroy method is called.
	 */
	Collection<GlueModule> prototypeModules = []

	public GlueModuleFactoryImpl() {
	}

	void destroy(){

		//notify all modules of destroy through the onProcessKill method
//		modules.each { String name, GlueModule module ->
//			try{
//			module.onProcessKill(null, context)
//			}catch(Throwable t){
//				log.error(t.toString(), t)
//			}
//		}

		//destroy all prototype modules
		prototypeModules?.each { GlueModule module ->
			//we can only print the module exception here
			//it makes no sense to do anything else here.
			try{
				module.destroy()
			}catch(Throwable t){
				log.error(t)
			}
		}

		modules.clear()
		prototypeModules.clear()
	}

	void onProcessFinish(GlueProcess process, GlueContext context){
		modules?.each { String name, GlueModule module ->

			try{
				module.onProcessFinish(process, context)
			}catch(Throwable t){
				log.error t
			}
		}
	}
	void onProcessStart(GlueProcess process, GlueContext context){
		modules?.each { String name, GlueModule module ->

			try{
				module.onProcessStart(process, context)
			}catch(Throwable t){
				log.error t
			}
		}
	}
	void onProcessFail(GlueProcess process, GlueContext context, Throwable t){
		modules?.each { String name, GlueModule module ->

			try{
				module.onProcessFail(process, context, t)
			}catch(Throwable exp){
				log.error exp
			}
		}
	}

	/**
	 * Calls onUnitFinish on all available modules
	 * @param unit
	 * @param context
	 */
	public void onUnitFinish(GlueUnit unit, GlueContext context){

		modules?.each { String name, GlueModule module ->

			try{
				module.onUnitFinish unit, context
			}catch(Throwable t){
				log.error t
			}
		}
	}

	/**
	 * Calls onUnitFail on all available modules
	 * @param unit
	 * @param context
	 */
	public void onUnitFail(GlueUnit unit, GlueContext context){

		modules?.each { String name, GlueModule module ->

			try{
				module.onUnitFail unit, context
			}catch(Throwable t){
				log.error t
			}
		}
	}

	/**
	 * Calls onUnitStart on all available modules 
	 * @param unit
	 * @param context
	 */
	public void onUnitStart(GlueUnit unit, GlueContext context){

		getAvailableModules()?.each { String name, GlueModule module ->

			try{
				module.onUnitStart unit, context
			}catch(Throwable t){
				log.error t
			}
		}
	}

	@Override
	public Map<String, GlueModule> getAvailableModules() {
		modules
	}

	GlueModule getModule(String moduleName){
		modules[moduleName]
	}

	void leftShift(String name, GlueModule module, boolean isSingleton = true){
		modules[name] = module
		if(!isSingleton){
			prototypeModules << module
		}
	}

	void addModule(String name, GlueModule module, boolean isSingleton = true){
		modules[name] = module
		if(!isSingleton){
			prototypeModules << module
		}
	}
}
