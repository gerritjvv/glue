package org.glue.unit.exec;

import java.util.Date;
import java.util.Map;

import org.glue.unit.om.GlueContext;
import org.glue.unit.om.GlueUnit;

/**
 * 
 * The GlueExecutor delegates responsibility of running the GlueUnit to the
 * UnitExecutor.<br/>
 * A GlueUnit is made up of processes and each process's execution is delegated
 * to the ProcessExecutor.<br/>
 * <p/>
 * This means that:<br/>
 * GlueExecutor: job submission<br/>
 * UnitExecutor: GlueUnit process execution control ProcessExecutor: actual
 * execution of a task (process) <br/>
 * 
 * 
 */
@Typed
public interface UnitExecutor {

	/**
	 * Runs all processes in the GlueUnit
	 */
	void execute();

	/**
	* Runs all processes in the GlueUnit
	* @param closure calls the closure after the execute has completed.
	*/
	void execute(Closure closure);
	
	/**
	 * Configures the executor to use the unit and context provided.
	 * @param unit
	 * @param context
	 */
	void init(GlueUnit unit, GlueContext context)
	
	/**
	 * State of execution
	 * 
	 * @return
	 */
	GlueState getStatus();

	Map<String, Throwable> getErrors();
	
	/**
	* Waits for all process execution to complete
	*/
	void waitFor();
	/**
	 * Waits for all process execution to complete, or times out if the timeout has passed.
	 * @param timeout
	 * @param timeUnit
	 */
	void waitFor(long timeout, java.util.concurrent.TimeUnit timeUnit);
	
	/**
	 * Kills all running threads and processes
	 */
	void terminate();
	
	/**
	 * Progress indicator
	 * 
	 * @return
	 */
	double getProgress();

	/**
	 * The GlueUnit that is processed by this UnitExecutor
	 * 
	 * @return
	 */
	GlueUnit getUnit();

	/**
	 * All current running ProcessExecutor(s)
	 * 
	 * @return
	 */
	Map<String, ProcessExecutor> getProcessExecutors();

	/**
	 * Date that the UnitExecutor started running the GlueUnit
	 * 
	 * @return
	 */
	Date getStartDate();

	/**
	 * Execution end date
	 * 
	 * @return
	 */
	Date getEndDate();

}
