package org.glue.trigger.service.hdfs

import static groovyx.gpars.actor.Actors.*
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

import org.apache.hadoop.fs.ContentSummary
import org.apache.hadoop.fs.FileStatus
import org.apache.log4j.Logger
import org.glue.modules.hadoop.ClosureStopException
import org.glue.modules.hadoop.HDFSModule
import org.glue.trigger.persist.CheckPoint
import org.glue.trigger.persist.TriggerStore
import org.glue.unit.exec.GlueExecutor



/**
 *
 * This class implements the polling a glue unit submitting logic.<br/>
 * It expects a thread safe queue reference.
 */
@Typed
class HDFSTriggerPolling {

	static private final Logger LOG = Logger.getLogger(HDFSTriggerPolling.class)

	static final String CHECKPOINT_PREFIX = "hdfsTriggerModule"


	/**
	 * This hdfsPath is treated as a groovy template.
	 */
	String hdfsPath
	List<String> unitNames
	TriggerStore triggerStore
	GlueExecutor executor
	HDFSModule hdfsModule

	int maxFilesRead

	Template hdfsPathTemplate

	/**
	 * The maximum time taken during polling
	 */
	long maxPollTime = 0L

	public HDFSTriggerPolling(String path, List<String> unitNames, TriggerStore triggerStoreModule, GlueExecutor executor, HDFSModule hdfsModule, int maxFilesRead = 500){
		this.unitNames = unitNames
		this.triggerStore = triggerStoreModule
		this.executor = executor
		this.hdfsModule = hdfsModule
		this.maxFilesRead = maxFilesRead

		setPath(path)
	}

	void setPath(String path){
		hdfsPath = path
		hdfsPathTemplate = new SimpleTemplateEngine().createTemplate(path)
	}


	/**
	 * Start searching for all files that were updated since the last updated checkpoint. 
	 */
	@Typed(TypePolicy.DYNAMIC)
	public boolean poll(){
		println "Starting polling";
		//we add in some functionality that complicates the method but it does mean units with the same check point
		//does not require hdfs to be polled again

		//we parse the hdfs path as a template before each poll
		def parsedHdfsPath = hdfsPathTemplate.make().toString()

		LOG.info "Using hdfsPath: ${parsedHdfsPath}"
		//get last checkpoint timestamp for each unit
		unitNames?.each { String unitName ->

			LOG.info "HdfsTriggerPolling: checking files for unit $unitName"
			long startTime = System.currentTimeMillis()

			AtomicLong lastMaxCheckpoint = new AtomicLong(0L)

			Map<String, CheckPoint> checkPointMap = triggerStore.getCheckPoints(unitName)

			Collection<FileStatus> filesToUpdate = []
			Map<String, CheckPoint> checkPointsToUpdate = [:]

			// Note this actor performs an expensive operation i.e a db call
			// so we prefer to do it async while the hdfs module is looping through the directories
			// this adds better performance.
			def updatedActors =  actor {

				loop{
					react{ FileStatus fileStatus ->

						String fileName = fileStatus.path.toUri().toString()
						if(triggerStore.getStatus(unitName, fileName) == null){
							lastMaxCheckpoint.set(Math.max(lastMaxCheckpoint.get(), fileStatus.getModificationTime()))
							filesToUpdate << fileName
						}

					}

				}
				
			} 

			hdfsModule.findNewFiles(parsedHdfsPath,
					/*if directory has been modified */ 
					{ FileStatus status ->
						String uri = status.path.toUri().toString()
						ContentSummary contentSummary = hdfsModule.getContentSummary(uri)
						if(!checkPointMap[uri]?.equals(contentSummary)){
							//we batch checkpoints to save later for performance
							checkPointsToUpdate[uri] = CheckPoint.create(contentSummary)
							return true
						}else{
							return false
						}

					},
					/*if the file is new if will have no status yet */ 
					{ FileStatus status -> updatedActors << status })


			//wait for updatedActors to complete
			updatedActors.stop()
			updatedActors.join()

			println "done hdfs list ${System.currentTimeMillis() - startTime}"
			

			//We have a list of filesToUpdate as ready files, and checkPointsToUpdate
			//we do this in one database transaction
			
			
			long maxPollTime = Math.max(maxPollTime, System.currentTimeMillis() - startTime)
			println "Found updatedFiles ${filesToUpdate?.size()}  ${System.currentTimeMillis() - startTime} milliseconds"
			
			
			if(filesToUpdate?.size() > 0){
				
				Date lastMaxCheckpointDate = new Date(lastMaxCheckpoint.get())
				
				triggerStore.markFilesAsReady(unitName, filesToUpdate)
				checkPointMap.each { k, CheckPoint v -> v.date = lastMaxCheckpointDate }
				  
				triggerStore.storeCheckPoints(unitName, checkPointMap)
				
				LOG.debug "submitting $unitName"
				//triggers create unique unit ids of their own
				String	unitId=java.util.UUID.randomUUID().toString();

				try{
					//on any error we still continue with the other name pollings
					executor.submitUnitAsName(unitName, ['lastMaxCheckpoint':String.valueOf(lastMaxCheckpoint.get())], unitId)
				}catch(Throwable t){
				  LOG.error(t.toString(), t)
				}

				LOG.debug "Complete submitting $unitName"
			}
			LOG.info "leaving polling method-------------------- "
		}


		return true
	}

}
