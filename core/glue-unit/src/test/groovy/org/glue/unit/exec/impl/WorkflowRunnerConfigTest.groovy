package org.glue.unit.exec.impl;

import static org.junit.Assert.*

import org.apache.commons.cli.ParseException
import org.junit.Test

/**
 * 
 * Tests the getInstance method
 *
 */
class WorkflowRunnerConfigTest {
 
	/**
	 * Test parse with error
	 */
	@Test(expected=ParseException)
	public void testGetInstanceWithError(){
		
		def config = WorkflowRunnerConfig.getInstance(
			[] as String[]
			) 	
		
	}
	
	/**
	 * Test parse with all arguments
	 */
	@Test
	public void testGetInstance(){
		
		def config = WorkflowRunnerConfig.getInstance(
			['-execConf', 'src/test/resources/module_conf/exec_conf.groovy',
			 '-moduleConf', 'src/test/resources/module_conf/empty_conf.groovy',
			 '-uuid', '123',
			 '-workflow', 'test'
				] as String[]
			)
		
	}
	
}
