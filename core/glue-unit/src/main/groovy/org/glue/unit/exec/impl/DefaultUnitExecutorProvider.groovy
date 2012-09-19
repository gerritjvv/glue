package org.glue.unit.exec.impl

import groovyx.gpars.dataflow.DataFlowActorGroup;

import org.glue.unit.exec.ProcessExecutor;
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.om.Provider

/**
 *
 * Basic class that creates new unit executor instances
 *
 */
@Typed
class DefaultUnitExecutorProvider extends Provider<UnitExecutor>{

	Provider<ProcessExecutor> processExecutorProvider
	
	DataFlowActorGroup group
	
	public DefaultUnitExecutorProvider(
			Provider<ProcessExecutor> processExecutorProvider, int executorPoolThreads = 10) {
		super();
		this.processExecutorProvider = processExecutorProvider;
		group = new DataFlowActorGroup(executorPoolThreads)
	}
			
	UnitExecutor get(){
		new GParallizerUnitExecutor(processExecutorProvider, group)
	}
	
}
