package org.glue.trigger.service.hdfs

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

import org.glue.modules.hadoop.HDFSModule
import org.glue.modules.hadoop.impl.HDFSModuleImpl
import org.glue.trigger.persist.TriggerStore
import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.TriggerDef
import org.glue.unit.script.ScriptClassCache;
import org.apache.log4j.Logger


/**
 * The HdfsTrigger module polls hdfs for changes in directory structures.<br/>
 * If a change is seen, the associated GlueUnit is submitted for execution.
 * <p/>
 * The module uses a ScheduledExecutorService and for each path to be monitored a Runnable is submitted to this service.<br/>
 * The number of thread available to the service equals pollingTreads (default 10).<br/>
 * An instance of HDFStriggerPolling is created for each path.<br/>
 * When a runnable as executed it will call the HDFSTriggerPolling.poll method.<br/>
 * <p/>
 * When the update method is called to refresh the glue units, the current ScheduledExecutorService is shutdown, and a new instance created.
 * The new list of GlueUnit Triggers is applied.
 */
@Typed(TypePolicy.MIXED)
public class HdfsTriggerWorker{

	private static final Logger LOG = Logger.getLogger(HdfsTriggerWorker.class)
	/**
	 * Several directories might be scheduled for polling. This is the default number of threads for 
	 */
	static final int DEFAULT_POLLING_THREADS = 10
	static final long DEFAULT_POLLING_PERIOD = 600000

	TriggerStore triggerStore

	GlueExecutor glueExecutor
	/**	
	 * key = the cluster configuration name
	 * value = triggers and glue unit names associated with this cluster configuration
	 */
	Map<String, HDFSModuleTriggers> triggersMap = [:]


	ConfigObject hdfsTriggerConfig


	String defaultClusterName

	ScheduledExecutorService executorService;

	long pollingPeriod
	int pollingThreads

	int maxFilesRead
	
	/**
	 * The maximum time spent in milliseconds polling hdfs
	 */
	volatile long maxPollTime

	/**
	 * Wait before timeout on shutdown
	 */
	long shutdownTimeOutWait = 60000;


	/**
	 * Expects clusters{ cluster1{ hdfsProperties="propertiesFile" } cluster2 { hdfsProperties="propertiesFile" } }
	 * @param globalTriggerConfig
	 */
	public HdfsTriggerWorker(TriggerStore triggerStore, ConfigObject hdfsTriggerConfig, GlueExecutor glueExecutor){
		this(triggerStore, hdfsTriggerConfig, glueExecutor, DEFAULT_POLLING_THREADS, DEFAULT_POLLING_PERIOD, 1000)
	}

	/**
	 * Expects clusters{ cluster1{ hdfsProperties="propertiesFile" } cluster2 { hdfsProperties="propertiesFile" } }
	 * @param globalTriggerConfig
	 */
	public HdfsTriggerWorker(TriggerStore triggerStore, ConfigObject hdfsTriggerConfig, GlueExecutor glueExecutor, int pollingThreads, long pollingPeriod, 
		int maxFilesRead= 1000){
		this.triggerStore = triggerStore
		this.hdfsTriggerConfig = hdfsTriggerConfig
		this.glueExecutor = glueExecutor
		this.pollingThreads = pollingThreads
		this.pollingPeriod = pollingPeriod
		this.maxFilesRead = maxFilesRead
		configure(hdfsTriggerConfig)
	}

	/**
	 * Loads the configuration and searches for the default cluster properties.<br/>
	 * Example:<br/>
	 * <pre>
	 * 	clusters{
	 test1{
	 hdfsProperties="src/test/resources/testcluster.properties"
	 }
	 test2{
	 hdfsProperties="src/test/resources/testcluster.properties"
	 }
	 }
	 * </pre>
	 * @param hdfsTriggerConfig
	 */
	@Typed(TypePolicy.DYNAMIC)
	private void configure(ConfigObject hdfsTriggerConfig){

		if(!hdfsTriggerConfig.clusters) {
			new ModuleConfigurationException( "Can't find any clusters in config for the hdfs trigger module!" )
		}

		if(hdfsTriggerConfig.clusters.size() < 1) {
			new ModuleConfigurationException( "Can't find any clusters defined in clusters in the config for the hdfs trigger module!" )
		}

		//iterate through the config to set the defaultClusterName
		def firstClusterConfigName
		hdfsTriggerConfig.clusters.each { key, it ->


			if(it.isDefault) {
				defaultClusterName=key
			}

			if(!firstClusterConfigName){
				firstClusterConfigName = key
			}
		}

		if(!defaultClusterName){
			//set this to the first item
			defaultClusterName = firstClusterConfigName
		}

		if(hdfsTriggerConfig?.pollingThreads){
			pollingThreads = Integer.valueOf (hdfsTriggerConfig?.pollingThreads)
		}

		if(hdfsTriggerConfig?.pollingPeriod){
			pollingPeriod = Long.valueOf (hdfsTriggerConfig?.pollingPeriod)
		}
	}

	/**
	 * Stops all services and resources used by this module.
	 */
	public void stop(){
		//stop hdfs poll timers
		if(executorService){
			executorService.shutdown();
			executorService.awaitTermination shutdownTimeOutWait, TimeUnit.MILLISECONDS
		}

		//after all timers have stopped close all hadoop file systems
		triggersMap?.each { String key, HDFSModuleTriggers triggers ->
			triggers.close()
		}
	}


	/**
	 * The trigger module type is used to identify a modue.<br/>
	 * This must be unique between all HdfsTriggerWorker(s).<br/>
	 * The type definition is used in the glue unit groovy files and associated
	 * with the TriggerDef.type property.
	 *
	 * @return
	 */
	public String getType(){
		"hdfs"
	}

	/**
	 * Sets the GlueExecutor that will run any GlueUnits triggered by this module.
	 * @param glueExecutor
	 */
	void setGlueExecutor(GlueExecutor glueExecutor){
		this.glueExecutor = glueExecutor
	}

	/**
	 * The HdfsTriggerWorker should only take GlueUnits that have triggers for this module.<br/>
	 * @param triggers
	 */
	public void update(Iterator<GlueUnit> glueUnits){

		Map<String, HDFSModuleTriggers> triggersMapNew = [:]

		GlueUnit glueUnit = null

		//for each GlueUnit returned from the Iterator
		// Go through each TriggerDef and for type == HDFS save the paths ( i.e. the value property ) to the pathToGlueUnitMap
		// if the path already exists in the path we add the GlueUnit name to this list
		while(glueUnits.hasNext()){

			glueUnit = glueUnits.next()


			glueUnit.getTriggers().each { TriggerDef triggerDef ->

				//only handle triggers with type hdfs
				if(triggerDef.getType().toLowerCase() == getType()){

					//get the cluster name, if none is set use the default as from the hdfs trigger module config
					String clusterName = triggerDef.getGroupIdentifier()
					if(!clusterName){
						clusterName = defaultClusterName
					}

					HDFSModuleTriggers moduleTriggers = triggersMapNew.get(clusterName)
					//if no moduleTriggers instance for this cluster load one
					if(!moduleTriggers){

						moduleTriggers = loadModuleTriggers(glueUnit, clusterName)
						triggersMapNew[clusterName] = moduleTriggers
					}

					//add this trigger
					moduleTriggers.addTrigger(glueUnit.name, triggerDef)

				}
			}//eof triggers each

		}//eof while

		this.triggersMap = triggersMapNew

		//update time thread executions

		if(executorService){
			//if there is a running executor service we should call shutdown and create a new one.
			executorService.shutdown()
			try{
				executorService.awaitTermination shutdownTimeOutWait, TimeUnit.MILLISECONDS
			}catch(InterruptedException excp){
				LOG.error(excp)
			}
		}

		//create new executor service
		executorService = Executors.newScheduledThreadPool(pollingThreads);


		//create a ThreadExecutionService with caching and max == pollingThreads.
		//   -- For each HDFSModuleTriggers we go through each Path
		//   -- --  For each path we create an instance of HDFSTriggerPolling
		//   -- --  Submit the HDFSTriggerPolling to the thread service
		//   -- Error handling must be handled by the polling module instance
		//   -- This class should never throw an error but rather register it.

		for(HDFSModuleTriggers hdfsModuleTriggers : triggersMapNew.values()){

			for(String path : hdfsModuleTriggers?.paths){

				//create polling module
				HDFSTriggerPolling poller = new HDFSTriggerPolling(
						path, hdfsModuleTriggers.getGlueUnitNames(path), triggerStore, glueExecutor, hdfsModuleTriggers.hdfsModule,
						maxFilesRead
						);

				executorService.scheduleAtFixedRate({
					//at each polling period call the HDFSTriggerPolling.poll method
					try{
						poller.poll()
					}catch(error){
						println error
						error.printStackTrace()
					}
					maxPollTime = poller.maxPollTime
				}, 1000L, pollingPeriod, TimeUnit.MILLISECONDS);
			}
		}
	}

	/**
	 * Loads a HDFSModule instance based on the trigger groupIdentifier and the trigger module's configuration
	 * @param glueUnit
	 * @param triggerDef
	 * @return
	 */
	HDFSModuleTriggers loadModuleTriggers(GlueUnit glueUnit, String clusterName){


		HDFSModule hdfsModule = new HDFSModuleImpl()

		//initialize the module with the whole trigger module configuration
		//this config format is exactly the same as that of HDFSModule
		hdfsModule.init hdfsTriggerConfig

		//configure the module with the default configuration
		//we use ScriptClassCache here so that we do not create classes when the script is the same
		hdfsModule.configure( null, ScriptClassCache.getDefaultInstance().parse(
				"""
				defaultCluster='$clusterName'
			"""
				))


		new HDFSModuleTriggers(hdfsModule:hdfsModule)
	}
}