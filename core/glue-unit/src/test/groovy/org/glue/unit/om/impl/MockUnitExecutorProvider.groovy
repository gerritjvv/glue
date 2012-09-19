package org.glue.unit.om.impl

import org.glue.unit.exec.UnitExecutor
import org.glue.unit.exec.impl.GParallizerUnitExecutor
import org.glue.unit.om.Provider

/**
 *
 * Helper provider that returns a GParallizerUnitExecutor
 *
 */
class MockUnitExecutorProvider extends Provider<UnitExecutor>{

	def processExecutorProvider
	
	UnitExecutor get(){
		new GParallizerUnitExecutor(processExecutorProvider)
	}
	
}
