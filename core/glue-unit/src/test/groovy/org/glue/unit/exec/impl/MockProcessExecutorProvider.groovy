package org.glue.unit.exec.impl

import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.om.Provider

/**
 *
 * Returns a new MockProcessExecutor
 *
 */
class MockProcessExecutorProvider extends Provider<ProcessExecutor>{

	boolean errorInExec = false

	/**
	 * If defined then this closure is called to return the process executor
	 */
	Closure processExecutorClosure

	ProcessExecutor get(){
		(processExecutorClosure)? processExecutorClosure() : new MockProcessExecutor(errorInExec:errorInExec)
	}
}
