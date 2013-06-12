package org.glue.unit.exec.impl;

import static org.junit.Assert.*

import org.glue.unit.exceptions.UnitSubmissionException
import org.junit.Test

/**
 * Test the workflow runner as a whole
 * 
 */
class ReplRunnerTest {

	@Test
	public void testClojureRepl(){
		
		def execConf = new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath
		def modulesConf = new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath
			
		ReplRunner.testMode = true
		ReplRunner.main(
			[	'-execConf', execConf,
				'-moduleConf', modulesConf,
				'-uuid', '123',
				'-workflow', 'testflow-exitontest',
				'-lang', 'clojure'
				   ] as String[]
			)
		
		assertNull(ReplRunner.exception)
		
	}
	
	@Test
	public void testGroovyRepl(){
		
		def execConf = new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath
		def modulesConf = new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath
			
		ReplRunner.testMode = true
		ReplRunner.main(
			[	'-execConf', execConf,
				'-moduleConf', modulesConf,
				'-uuid', '123',
				'-workflow', 'testflow-exitontest',
				'-lang', 'groovy'
				   ] as String[]
			)
		
		assertNull(ReplRunner.exception)
		
	}
	
	@Test
	public void testJythonRepl(){
		
		def execConf = new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath
		def modulesConf = new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath
			
		ReplRunner.testMode = true
		ReplRunner.main(
			[	'-execConf', execConf,
				'-moduleConf', modulesConf,
				'-uuid', '123',
				'-workflow', 'testflow-exitontest',
				'-lang', 'jython'
				   ] as String[]
			)
		
		assertNull(ReplRunner.exception)
		
	}
	
}
