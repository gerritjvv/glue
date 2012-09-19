package org.glue.unit.exec.impl

import java.util.Date;

import org.glue.unit.exec.GlueState;
import org.glue.unit.exec.ProcessExecutor;
import org.glue.unit.om.GlueContext;
import org.glue.unit.om.GlueProcess;

/**
 * Used to test the UnitExecutor.
 */
class MockProcessExecutor implements ProcessExecutor{
		GlueState status = GlueState.PENDING

		GlueProcess process
		Double progress = 1.0D
		Date startDate = new Date()
		Date endDate = new Date()

		Throwable error

		int execCount = 0

		boolean errorInExec = false

		void init(GlueProcess process, GlueContext context){
			this.process = process
		}


		void execute(){
			execCount++

			if(errorInExec){
				throw new RuntimeException("Induced Exception")
			}

			status = GlueState.FINISHED
		}

		public GlueState getState(){
			status
		}

}
