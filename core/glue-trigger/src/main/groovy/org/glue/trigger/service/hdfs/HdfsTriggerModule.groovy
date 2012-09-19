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
 * HdfsTriggerModule is a glue module that implements scanning work flows from the defined repository and<br/>
 * for each workflow with a trigger defined for type hdfs a pooled polling thread is added to monitor the directory<br/>
 * specified in the trigger.<br/>
 * When new files are seen in hdfs the work flows with triggers for that file/directory is run.<br/>
 *
 */
@Typed(TypePolicy.DYNAMIC)
class HdfsTriggerModule implements GlueModule{

	private static final Logger LOG = Logger.getLogger(HdfsTriggerModule)

	@Inject
	GlueExecutor glueExecutor

	@Inject
	GlueUnitRepository unitRepository

	TriggerStore triggerStore

	HdfsTriggerWorker hdfsTriggerWorker

	ExecutorService executorService

	/**
	 * This stores the whole trigger definition for each glue unit.
	 * This allows us to check when a repository update happens that a trigger instance did change.
	 */
	private Set<String> unitTriggerDefinitionSet = []

	/**
	 * Every time the repository is read this date is setup.
	 */
	Date repoLastUpdated

	/**
	 * The maximum time spent on polling hdfs
	 */
	long maxPollTime = 0L
	
	/**
	 * The maximum amount of files that will be read in any update operation
	 */
	int maxFilesRead
	
	/**
	 * Used for testing and shows the number of times an update from the unit repository has been seen.
	 */
	AtomicInteger unitRepositoryUpdatedCount = new AtomicInteger(0)

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

		if(!glueExecutor){
			throw new ModuleConfigurationException("Error the HdfsTriggerModule requires a glue executor to be injected", config)
		}

		if(!unitRepository){
			throw new ModuleConfigurationException("Error the HdfsTriggerModule requires a GlueUnitRepository to be injected", config)
		}



		/*
		 * public HdfsTriggerWorker(TriggerStore triggerStore, ConfigObject hdfsTriggerConfig, GlueExecutor glueExecutor, int pollingThreads, long pollingPeriod)
		 * 
		 */

		int pollingThreads = 10
		if(config.pollingThreads){
			try{
				pollingThreads = Integer.valueOf(config.pollingThreads)
			}catch(Throwable t){
				throw new ModuleConfigurationException("Error reading pollingThreads $t", config, t)
			}
		}

		
		if(config.maxFilesRead){
			try{
				maxFilesRead = Integer.valueOf(config.maxFilesRead)
			}catch(Throwable t){
				throw new ModuleConfigurationException("Error reading maxFilesRead $t", config, t)
			}
		}else{
			maxFilesRead = 1000
		}
	LOG.warn "maxFileRead: $maxFilesRead";	
		long pollingPeriod = 600000
		if(config.pollingPeriod){
			try{
				pollingPeriod = Long.valueOf(config.pollingPeriod)
			}catch(Throwable t){
				throw new ModuleConfigurationException("Error reading pollingPeriod $t", config, t)
			}
		}

		long repoPollingPeriod = 10000
		if(config.repoPollingPeriod){
			try{
				repoPollingPeriod = Long.valueOf(config.repoPollingPeriod)
			}catch(Throwable t){
				throw new ModuleConfigurationException("Error reading repoPollingPeriod $t", config, t)
			}
		}


		LOG.info """
		
		  pollingThreads: $pollingThreads
		  pollingPeriod: $pollingPeriod
		  repoPollingPeriod: $repoPollingPeriod
		  triggerStoreClass: $triggerStoreClass
		
		"""

                            //new HdfsTriggerWorker(triggerStore, config, glueExecutor, pollingThreads, pollingPeriod)
		hdfsTriggerWorker =  new HdfsTriggerWorker(triggerStore, config, glueExecutor, pollingThreads, pollingPeriod, maxFilesRead);

       
		executorService = Executors.newScheduledThreadPool(1);

		executorService.scheduleAtFixedRate( {
			try{
				//update the worker with glue units from the repository
				repoLastUpdated = new Date()
				if(hasUpdates(unitRepository)){
					unitRepositoryUpdatedCount.getAndIncrement()
					LOG.info "Updating triggers from repository"
					hdfsTriggerWorker.update(unitRepository.iterator())
					maxPollTime = hdfsTriggerWorker.maxPollTime
				}
			}catch(Throwable t){
				LOG.error(t.toString(), t)
			}
		}, 1000L, repoPollingPeriod, TimeUnit.MILLISECONDS
		)
	}

	/**
	 * Checks if the GlueUnitRepository has any updated triggers
	 * @param unitRepository
	 * @return boolean true if an update was found in any of the triggers
	 */
	private synchronized final boolean hasUpdates(GlueUnitRepository unitRepository){

		Iterator<GlueUnit> iterator = unitRepository.iterator()

		Set<String> updatedTriggerDefinitionSet = []

		boolean updated = false

		while( iterator?.hasNext() ){
			GlueUnit unit = iterator.next()
			//List<TriggerDef> getTriggers
			List<TriggerDef> triggers = unit.triggers
			for(TriggerDef triggerDef in triggers){
				//this method checks and rebuilds a new updatedTriggerDefinitionSet
				if(triggerDef.type == 'hdfs'){

					String key = "${triggerDef.groupIdentifier}:${triggerDef.value}"
					if( !unitTriggerDefinitionSet.contains(key) ){
						//if the key does not exist in the set it means that we have
						//a trigger that changed. We add this new key to the updated
						//set
						updated = true
						LOG.info "New trigger definition for ${unit.name}"
					}

					//we add all definitions here
					//this ensures that the full set is always defined
					updatedTriggerDefinitionSet << key
				}
			}
		}

		//here we swap the unit trigger definition set
		//with the updated trigger definition set
		unitTriggerDefinitionSet = updatedTriggerDefinitionSet


		return updated
	}

	void destroy(){
		executorService.shutdownNow()
		hdfsTriggerWorker.stop()
	}

	public Map getInfo(){
		[
					repoLastUpdated:repoLastUpdated,
					unitTriggerDefinitions:unitTriggerDefinitionSet,
					unitRepositoryUpdatedCount:unitRepositoryUpdatedCount.get(),
					maxPollTime:maxPollTime
				]
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
		"HdfsTriggerModule"
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
    void listReadyFiles(GlueContext context, Closure closure){
		triggerStore.listReadyFiles context.unit.name, closure
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
	* @param path
	*/
	void markFilesAsProcessed(GlueContext context, Collection<String> paths){
		triggerStore.markFilesAsProcessed context.unit.name, context.unitId, paths
	}

   /**
	* Marks a file as ready, if the file doesn't already exist it will be created, else the entry is updated.
	* @param unitName
	* @param path
	*/
    void markFileAsReady(GlueContext context, String path){
		triggerStore.markFileAsReady context.unit.name, context.unitId, path
	}
	
}
