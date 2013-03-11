package org.glue.modules.hadoop.pig

import java.lang.Thread.UncaughtExceptionHandler

import org.apache.pig.PigRunner.ReturnCode
import org.apache.pig.tools.pigstats.PigStats
import org.glue.modules.hadoop.GluePigProgressNotificationListener


/**
 * 
 * Is a main class that will launch the pig script using the available pig configuration
 *
 */
@Typed
class PigRunner {

	public static void main(String[] args){

		GluePigProgressNotificationListener listener = new GluePigProgressNotificationListener()
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){
			public void uncaughtException(Thread t, Throwable e){
				try{
					e.printStackTrace()
					System.exit(-1)//exit
				}catch(Throwable t1){
				}
			}
		});
		
		println "Arguments ${args}"
		//run pig
		int exitCode = 0;
		try{
			PigStats pigStats = org.apache.pig.PigRunner.run(args as String[], listener)

			//if return code != SUCCESS exit with -1
			if(pigStats.getReturnCode() != ReturnCode.SUCCESS || !listener.completed){
				println pigStats.getErrorMessage()
			    exitCode = -1
			}
		}finally{
			System.exit(exitCode)
		}

	}

}
