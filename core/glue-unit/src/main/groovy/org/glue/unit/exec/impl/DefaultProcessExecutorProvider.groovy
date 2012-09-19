package org.glue.unit.exec.impl

import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.om.Provider

/**
*
* Basic class that creates new unit executor instances
*
*/
@Typed
class DefaultProcessExecutorProvider extends Provider<ProcessExecutor>{

	ProcessExecutor get(){
		new ProcessExecutorImpl()
	}
	
	
}
