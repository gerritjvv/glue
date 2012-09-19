package org.glue.modules.hadoop

import java.util.concurrent.CountDownLatch

import org.apache.pig.tools.pigstats.JobStats
import org.apache.pig.tools.pigstats.OutputStats
import org.apache.pig.tools.pigstats.PigProgressNotificationListener

/**
 * 
 * Is a pig notification listener.<br/>
 * The class implements a waitFinish method that will block until pig has completed.<br/>
 * Note that all logs are printed to std out to allow the LogModule to print the information.<br/>
 */
@Typed
class GluePigProgressNotificationListener implements PigProgressNotificationListener{

	CountDownLatch latch = new CountDownLatch(1)

	volatile String msg
	volatile Exception error

	Set<String> jobIds = []
	Set<String> failedJobIds = []

	volatile  boolean hasError = false

	volatile  int numberOfJobs = 0

	volatile boolean completed = false;

	/**
	 * Causes the current thread to wait until the pig job has completed
	 * @return
	 */
	public boolean waitFinish(){
		latch.await()
		return !hasError
	}

	public void notifyExternalError(){
		hasError = true
		latch.countDown()
	}

	/**
	 * Invoked just before launching MR jobs spawned by the script.
	 *
	 * @param numJobsToLaunch the total number of MR jobs spawned by the script
	 */
	public void launchStartedNotification(int numJobsToLaunch){
		println "launchStartedNotification jobsToLaunch: $numJobsToLaunch"
		numberOfJobs = numJobsToLaunch
	}

	/**
	 * Invoked just before submitting a batch of MR jobs.
	 *
	 * @param numJobsSubmitted the number of MR jobs in the batch
	 */
	public void jobsSubmittedNotification(int numJobsSubmitted){
		println "jobsSubmittedNotification jobsSubmitted: $numJobsSubmitted"
	}

	/**
	 * Invoked after a MR job is started.
	 *
	 * @param assignedJobId the MR job id
	 */
	public void jobStartedNotification(String assignedJobId){
		println "jobStartedNotification jobId: $assignedJobId"
		jobIds << assignedJobId
	}

	/**
	 * Invoked just after a MR job is completed successfully.
	 *
	 * @param jobStats the {@link JobStats} object associated with the MR job
	 */
	public void jobFinishedNotification(JobStats jobStats){

		println """
	   		jobFinishedNotification (success: ${jobStats?.isSuccessful()})
			   
	   		MapInputRecords: ${jobStats?.getMapInputRecords()}
			MapOutputRecords: ${jobStats?.getMapOutputRecords()}
			ReduceInputRecords: ${jobStats?.getReduceInputRecords()}
			ReduceOutputRecords: ${jobStats?.getReduceOutputRecords()}
	   
	   """
	}

	/**
	 * Invoked when a MR job fails.
	 *
	 * @param jobStats the {@link JobStats} object associated with the MR job
	 */
	public void jobFailedNotification(JobStats jobStats){

		println """
	   
	   		jobFailedNotification (error: ${jobStats?.getErrorMessage()}
			   
	   		MapInputRecords: ${jobStats?.getMapInputRecords()}
			MapOutputRecords: ${jobStats?.getMapOutputRecords()}
			ReduceInputRecords: ${jobStats?.getReduceInputRecords()}
			ReduceOutputRecords: ${jobStats?.getReduceOutputRecords()}
	   
	        exception:
	        ${jobStats?.getException()}
	   
	   """

		try{
			error = jobStats?.getException()
			msg = jobStats?.getErrorMessage()
			hasError = true
			failedJobIds << jobStats.getJobId()
		}finally{
			latch.countDown()
		}
	}

	/**
	 * Invoked just after an output is successfully written.
	 *
	 * @param outputStats the {@link OutputStats} object associated with the output
	 */
	public void outputCompletedNotification(OutputStats outputStats){
		completed = true

		println """
	   outputCompletedNotification (success: ${outputStats.isSuccessful()})
	   
	   name: ${outputStats?.name}
	   location: ${outputStats?.location}
	   records: ${outputStats?.getNumberRecords()}
	   bytes: ${outputStats?.bytes}
	   
	   pig-alias: ${outputStats?.getAlias()}
	   function-name: ${outputStats?.getFunctionName()}
	   """
	}

	/**
	 * Invoked to update the execution progress.
	 *
	 * @param progress the percentage of the execution progress
	 */
	public void progressUpdatedNotification(int progress){
		println "progressUpdatedNotification ${progress}%"
	}

	/**
	 * Invoked just after all MR jobs spawned by the script are completed.
	 *
	 * @param numJobsSucceeded the total number of MR jobs succeeded
	 */
	public void launchCompletedNotification(int numJobsSucceeded){
		latch.countDown()
		println "launchCompletedNotification jobsSucceeded: ${numJobsSucceeded} of ${numberOfJobs}"
	}

	@Override
	public void jobFailedNotification(String arg0, JobStats stats) {
		jobFailedNotification(stats)
	}

	@Override
	public void jobFinishedNotification(String arg0, JobStats stats) {
		jobFinishedNotification(stats)
	}

	@Override
	public void jobStartedNotification(String arg0, String stats) {
		jobStartedNotification(stats)
	}

	@Override
	public void jobsSubmittedNotification(String arg0, int arg1) {
		jobsSubmittedNotification(arg1)
	}

	@Override
	public void launchCompletedNotification(String arg0, int arg1) {
		launchCompletedNotification(arg1)
	}

	@Override
	public void launchStartedNotification(String arg0, int arg1) {
		launchStartedNotification(arg1)
	}

	@Override
	public void outputCompletedNotification(String arg0, OutputStats stats) {
		outputCompletedNotification(stats)
	}

	@Override
	public void progressUpdatedNotification(String arg0, int arg1) {
		progressUpdatedNotification(arg1)
	}
}
