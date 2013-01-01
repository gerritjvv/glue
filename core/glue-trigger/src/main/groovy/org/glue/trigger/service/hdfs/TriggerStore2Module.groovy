package org.glue.trigger.service.hdfs


import groovy.lang.Closure;
import groovyx.gpars.actor.Actors

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import javax.inject.Inject

import org.glue.trigger.persist.TriggerStore2
import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.TriggerDef
import org.glue.unit.repo.GlueUnitRepository
import org.apache.log4j.Logger

/**
 * 
 * The HdfsTriggerStoreModule is used by clients to provide access to the trigger store.<br/>
 * This should be configured with the same parameters as that of the the HdfsTriggerModule.
 *
 */
@Typed(TypePolicy.DYNAMIC)
class TriggerStore2Module implements GlueModule{

	private static final Logger LOG = Logger.getLogger(TriggerStore2Module)

	TriggerStore2 triggerStore
	GlueContext context

	void init(ConfigObject config){

		/*
		 * we expect triggerStore{
		 *     className='trigger store implementation class'
		 *     config{
		 *        //configuration of trigger store
		 *     }
		 * }
		 */
		if(!config.triggerStore){
			throw new ModuleConfigurationException("The section triggerStore must be specified.", config)
		}

		ConfigObject triggerStoreConfig = config.triggerStore

		if(!triggerStoreConfig.className){
			throw new ModuleConfigurationException("The property className is required in the triggerStore section", config)
		}

		if(!triggerStoreConfig.config){
			throw new ModuleConfigurationException("The section config is required in the triggerStore section", config)
		}

		//load triggerStore class
		Class triggerStoreClass = Thread.currentThread().getContextClassLoader().loadClass(triggerStoreConfig.className)
		if(!TriggerStore2.isAssignableFrom(triggerStoreClass)){
			throw new ModuleConfigurationException("The class $triggerStoreClass must implement/extend the TriggerStore class", config)
		}

		//instantiate triggerStore class
		try{
			triggerStore = triggerStoreClass.newInstance()
			triggerStore.init(triggerStoreConfig.config)
		}catch(Throwable t){
			throw new ModuleConfigurationException("Error instantiating and configuring $triggerStoreClass", config, t)
		}
	}

	void destroy(){
		triggerStore.destroy()
	}

	public Map getInfo(){
		[:]
	}

	void configure(String unitId, ConfigObject config){
	}

	void onUnitStart(GlueUnit unit, GlueContext context){
		this.context = context
	}

	void onUnitFinish(GlueUnit unit, GlueContext context){
	}
	void onUnitFail(GlueUnit unit, GlueContext context){
	}

	Boolean canProcessRun(GlueProcess process, GlueContext context){
		true
	}
	void onProcessStart(GlueProcess process,GlueContext context){
	}
	void onProcessFinish(GlueProcess process, GlueContext context){
	}
	void onProcessFail(GlueProcess process, GlueContext context, Throwable t){
	}

	void onProcessKill(GlueProcess process, GlueContext context){
	}


	String getName(){
		"triggerStore"
	}
	
	void listReadyFiles(Closure closure, boolean lock=true){
		listReadyFiles(context, closure, lock)
	}
	
	/**
	* List all files that where updated as READY by the trigger<br/>
	* The closure is called with (fileid:int, filePath:String)<br/>
	* @param unitName
	* @param closure
	*/
    void listReadyFiles(GlueContext context, Closure closure, boolean lock=true){
		triggerStore.listReadyFiles context.unit.name, closure, lock
	}

	/**
	* Marks a file as processed
	* @param unitName
	* @param ids list of file ids
	*/
	void markFilesAsProcessed(GlueContext context, java.util.Collection ids){
		triggerStore.markFilesAsProcessed context.unit.name, ids;
	}

	void markFilesAsProcessed(java.util.Collection ids){
		markFilesAsProcessed(context, ids)
	}
   	
}
