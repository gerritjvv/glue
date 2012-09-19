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

import org.glue.trigger.persist.TriggerStore
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
class HdfsTriggerStoreModule implements GlueModule{

	private static final Logger LOG = Logger.getLogger(HdfsTriggerStoreModule)

	TriggerStore triggerStore


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
		if(!TriggerStore.isAssignableFrom(triggerStoreClass)){
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
	
	/**
	* Store checkpoint variable for the current configured unit name.<br/>
	* @param unitName
	* @param path This is the directory path to which the trigger was registered.
	* @param checkpoint
	*/
   void storeCheckPoint(GlueContext context, String path, Date checkpoint){
	   triggerStore.storeCheckPoint context.unit.name, path, checkpoint
   }

   /**
	* Removes a checkpoint variable for the current configured unit name.<br/>
	* @param unitName
	* @param path This is the directory path to which the trigger was registered.
	*/
   void removeCheckPoint(GlueContext context, String path){
	   triggerStore.removeCheckPoint context.unit.name, path
   }
   
   /**
	* Returns a map of key = string, value = date<br/>
	* The key is the path for which the trigger was listening.
	* @param unitName
	* @return Map
	*/
    Map<String, Date> getCheckPoints(GlueContext context){
		triggerStore.getCheckPoints context.unit.name
    }

   /**
	* Get the check point date for the path.
	* @param unitName
	* @param path
	* @return Date
	*/
    Date getCheckPoint(GlueContext context, String path){
		triggerStore.getCheckPoint context.unit.name, path
	}

   /**
	* List all files that where updated as READY by the trigger<br/>
	* The closure is called with (entityName:String, filePath:String)<br/>
	* The closure will be called withing the scope of a database transaction.<br/>
	* @param unitName
	* @param closure
	*/
    void listReadyFiles(GlueContext context, Closure closure, boolean lock=true){
		triggerStore.listReadyFiles context.unit.name, closure, lock
	}

	/**
	 * Return true if the file was marked as processed
	 * @param context
	 * @param path
	 * @return boolean
	 */
	boolean isFileProcessed(GlueContext context, String path){
		triggerStore.isFileProcessed context.unit.name, path
	}
	
   /**
	* List all files that were found by the trigger<br/>
	* The closure is called with (entityName:String, status:String, filePath:String)<br/>
	* The closure will be called withing the scope of a database transaction.<br/>
	* @param unitName
	* @param closure
	*/
    void listAllFiles(GlueContext context, Closure closure){
		triggerStore.listAllFiles context.unit.name, closure
	}

   /**
	* List all files that were updated as PROCESSED by the work flows<br/>
	* The closure is called with (entityName:String, filePath:String)<br/>
	* The closure will be called withing the scope of a database transaction.<br/>
	* @param unitName
	* @param closure
	*/
    void listProcessedFiles(GlueContext context, Closure closure){
		triggerStore.listProcessedFiles context.unit.name, closure
	}

   /**
	* Marks a file as processed
	* @param unitName
	* @param path
	*/
    void deleteFile(GlueContext context, String path){
		triggerStore.deleteFile context.unit.name, path
	}

   /**
	* Marks a file as processed
	* @param unitName
	* @param path
	*/
    void markFileAsProcessed(GlueContext context, String path){
		triggerStore.markFileAsProcessed context.unit.name, context.unitId, path
	}
	
	/**
	* Marks a file as processed
	* @param unitName
	* @param path array or list of files
	*/
	void markFilesAsProcessed(GlueContext context, java.util.Collection paths){
		triggerStore.markFilesAsProcessed context.unit.name, context.unitId, paths
	}

   /**
	* Marks a file as ready, if the file doesn't already exist it will be created, else the entry is updated.
	* @param unitName
	* @param path
	*/
    void markFileAsReady(GlueContext context, String path){
		triggerStore.markFileAsReady context.unit.name, path
	}
	
}
