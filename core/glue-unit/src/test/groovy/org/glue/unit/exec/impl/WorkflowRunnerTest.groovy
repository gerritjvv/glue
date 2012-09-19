package org.glue.unit.exec.impl;

import static org.junit.Assert.*

import org.glue.unit.exceptions.UnitSubmissionException
import org.junit.Test

/**
 * Test the workflow runner as a whole
 * 
 */
class WorkflowRunnerTest {

	/**
	 * Submit a workflow that should not fail
	 */
	@Test
	public void testSubmitWorkflow(){
		
		def execConf = new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath
		def modulesConf = new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath
			
		WorkflowRunner.testMode = true
		WorkflowRunner.main(
			[	'-execConf', execConf,
				'-moduleConf', modulesConf,
				'-uuid', '123',
				'-workflow', 'testflow'
				   ] as String[]
			)
		
		assertNull(WorkflowRunner.exception)
		
	}
	
	/**
	 * Test submit a workflow with exception
	 */
	@Test
	public void testSubmitWorkflowError(){
		
		def execConf = new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath
		def modulesConf = new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath
			
		WorkflowRunner.testMode = true
		WorkflowRunner.main(
			[	'-execConf', execConf,
				'-moduleConf', modulesConf,
				'-uuid', '123',
				'-workflow', 'testflowException'
				   ] as String[]
			)
		
		assertNotNull(WorkflowRunner.exception)
	}
	
}
