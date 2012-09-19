package org.glue.modules.hadoop.pig

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

		println "Arguments ${args}"
		//run pig
		PigStats pigStats = org.apache.pig.PigRunner.run(args as String[], listener)
		
		//if return code != SUCCESS exit with -1
		if(pigStats.getReturnCode() != ReturnCode.SUCCESS || !listener.completed){
			println pigStats.getErrorMessage()
			System.exit(-1)
		}else{
			System.exit(0)
		}
	}
	
}
