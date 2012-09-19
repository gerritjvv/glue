package org.glue.unit.exec.impl.queue;

import static org.junit.Assert.*
import groovy.util.ConfigObject

import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.impl.DefaultGlueContextBuilder
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.om.impl.GlueModuleFactoryImpl
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.repo.impl.DirGlueUnitRepository
import org.junit.Test

/**
 * 
 * Tests the QueuedExecServiceImpl
 *
 */
class QueuedExecServiceImplTest {

	/**
	 * We test that a glue unit with that throws an exception
	 */
	@Test
	public void testUnitExecutorFail() {

		GlueModuleFactory moduleFactory = new GlueModuleFactoryImpl();

		ConfigObject execConfig = new ConfigObject();
		//create a workflow that will never complete
		ConfigObject unitConfig =  new ConfigSlurper().parse("""
		  name='test'
		  
		  tasks{
		  
			 pA{
			  tasks={ ctx -> throw new RuntimeException('Induced')}
			 }
		  
		  }
	   
	   """)


		GlueUnitRepository repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			'src/test/resources/test-flow-repo'
		])

		
		QueuedExecServiceImpl exec = new QueuedExecServiceImpl(
				2, [], [],
				repo,
				new DefaultGlueUnitBuilder(),
				new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath,
				new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath,
				null,
				new DefaultGlueContextBuilder(null)
				)

		exec.retainErrors = true
		//we expect a validation exception here
		def uid = exec.submitUnitAsConfig(unitConfig, ['it':'1'])
		
		exec.waitFor(uid)
		
		exec.shutdown()
		exec.waitUntillShutdown()
		
		assertNotNull(exec.errors[uid])
	}

	@Test
	public void testUnitExecutorUsingRepository() {

		GlueModuleFactory moduleFactory = new GlueModuleFactoryImpl();

		ConfigObject execConfig = new ConfigObject();

		GlueUnitRepository repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			'src/test/resources/test-flow-repo'
		])

		QueuedExecServiceImpl exec = new QueuedExecServiceImpl(
				2, [], [],
				repo,
				new DefaultGlueUnitBuilder(),
				new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath,
				new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath,
				null,
				new DefaultGlueContextBuilder(null)
				)

		exec.retainErrors = true

		String uid=exec.submitUnitAsName( "testflow", [:] )

		exec.waitFor uid

		exec.shutdown()
		exec.waitUntillShutdown()

		Throwable t = exec.errors[uid]
		assertNull(t)
	}



	/**
	 * We run two processes that will loop forever then call terminate on the executor to test<br/>
	 * that the process can terminate.<br/>
	 * We set a timeout of 5 seconds so that if terminate does not work this test fails in 5 seconds.<br/>
	 */
	@Test(timeout=5000L)
	public void testUnitExecutorTerminateAll() {

		GlueModuleFactory moduleFactory = new GlueModuleFactoryImpl();

		ConfigObject execConfig = new ConfigObject();
		//create a workflow that will never complete
		ConfigObject unitConfig =  new ConfigSlurper().parse("""
		  name='test'
		  tasks{
		  
			 pA{
			  tasks={ while(true){Thread.sleep(1000);}}
			 }
		  
				pB{
			  tasks={ while(true){Thread.sleep(1000);}}
			 }
		  }
	   
	   """)

		GlueUnitRepository repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			'src/test/resources/test-flow-repo'
		])

		QueuedExecServiceImpl exec = new QueuedExecServiceImpl(
				2, [], [],
				repo,
				new DefaultGlueUnitBuilder(),
				new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath,
				new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath,
				null,
				new DefaultGlueContextBuilder(null)
				)

		exec.retainErrors = true


		String uid=exec.submitUnitAsConfig(unitConfig, ['it':'1'])

		Thread.sleep(1000L)

		exec.terminate uid
		exec.shutdown()

		Throwable t = exec.errors[uid]
		assertNull(t)

	}
}
