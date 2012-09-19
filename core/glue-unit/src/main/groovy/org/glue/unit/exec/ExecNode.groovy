package org.glue.unit.exec

import org.glue.unit.om.GlueProcess


/**
 *
 * Represents an Execution Node, i.e. each execution node helps in the coordination<br/>
 * of glue unit process during execution.<br/>
 * 
 * An execution node does not do the process execution itself but is just used for coordination.<br/>
 * Coordination methods are waitForParents and setDone.<br/>
 * waitForParents is used to wait until all dependencies (i.e. parents) have completed executions.<br/>
 * setDone is used to indicate that this node has completed its execution.
 *
 */
@Typed
interface ExecNode extends Serializable{

	GlueProcess getGlueProcess()
	String getName()
	Set<ExecNode> getParents()
	Set<ExecNode> getChildren()

	/**
	 * Execution completed without errors
	 */
	void setDone()
	/**
	 * Execution completed but an error occurred
	 * @param error
	 */
	void setDone(Throwable error)

	void waitForParents()

	/**
	 * If true execution had an error
	 * @return boolean
	 */
	boolean hasError()
	
	Throwable getError()
	
	/**
	 * Checks for all types of cyclic dependencies.
	 */
	void checkCyclicDependencies() throws CyclicDependencyException
	
}
