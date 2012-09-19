package org.glue.unit.exec;

import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueContext
import org.glue.unit.exec.GlueState

/**
 * 
 * The ProcessExecutor is where the actual running of the glue groovy code
 * happens.<br/>
 * Each GlueUnit is made up of tasks (processes), the dependencies between these
 * processes are handled by the UnitExecutor.<br/>
 * Each single process is run by an instance of ProcessExecutor.
 * 
 */
@Typed
public interface ProcessExecutor {

	void init(GlueProcess glueProcess, GlueContext context)
	
	/**
	 * Run the process
	 */
	void execute();

	/**
	 * State of the running process
	 */
	public GlueState getState();

	public Throwable getError();
	
	/**
	 * Progress indicator
	 * 
	 * @return
	 */
	public Double getProgress();

	/**
	 * The GlueProcess being executed
	 * 
	 * @return
	 */
	public GlueProcess getProcess();

	/**
	 * Date in which the GlueProcess execution started
	 * 
	 * @return
	 */
	Date getStartDate();

	/**
	 * Date in which the GlueProcess completed execution
	 * 
	 * @return
	 */
	Date getEndDate();

}
