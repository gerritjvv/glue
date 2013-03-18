package org.glue.unit.exec.impl.queue;

import static org.junit.Assert.*

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glue.unit.exec.impl.WorkflowRunner;
import org.glue.unit.process.DefaultJavaProcessProvider
import org.junit.Test


/**
 * 
 * Tests that via the WofklowExecActor we can submit a workflow, that runs with and without errors.
 *
 */
class WorkflowExecActorTest {

	/**
	 * Submit a workflow via the WorkflowExecActor
	 */
	@Test
	public void testSubmitProcess(){

		def execConf = new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath
		def modulesConf = new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath

		WorkflowRunner.testMode = false

		def provider = new DefaultJavaProcessProvider([:])
		provider.javaOpts = ['-Xmx64m']
		provider.addCurrentClassPath()

		provider.mainClass = WorkflowRunner.class.name

		boolean hasError = false
		boolean completed = false

		def actor = new WorkflowExecActor(2, provider, execConf, modulesConf)
		actor.onErrorListener = { hasError = true; println "!!!!!! ERROR " }
		actor.onExecCompletedListener = { completed = true; println "!!!!! COMPLETE" }

		actor.start()

		//send the workflow for execution
		actor << new QueuedWorkflow('testflow', '123', [:])

		while(!(completed || hasError)){
			Thread.sleep(1000L)
		}

		actor.stop()

		assertFalse(hasError)
		assertTrue(completed)
	}


	/**
	 * Submit a workflow via the WorkflowExecActor
	 */
	@Test
	public void testSubmitProcessError(){

		def execConf = new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath
		def modulesConf = new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath

		def provider = new DefaultJavaProcessProvider([:])
		provider.javaOpts = ['-Xmx64m']
		provider.addCurrentClassPath()

		provider.mainClass = WorkflowRunner.class.name

		WorkflowRunner.testMode = false

		AtomicBoolean hasError = new AtomicBoolean(false)
		boolean completed = false

		def actor = new WorkflowExecActor(2, provider, execConf, modulesConf)
		actor.onErrorListener = { hasError.set(true); println "!!!!!! ERROR " }
		actor.onExecCompletedListener = { completed = true; println "!!!!! COMPLETE" }


		actor.start()

		//send the workflow for execution
		actor << new QueuedWorkflow('testflowException', '123', [:])

		while(!(completed || hasError.get())){
			Thread.sleep(1000L)
		}

		actor.stop()

		assertTrue(hasError.get())
		assertFalse(completed)
	}
	
	
	/**
	 * Submit a workflow via the WorkflowExecActor
	 */
	@Test
	public void testSubmitProcessQueueLimit(){

		def execConf = new File('src/test/resources/module_conf/exec_conf.groovy').absolutePath
		def modulesConf = new File('src/test/resources/module_conf/empty_conf.groovy').absolutePath

		WorkflowRunner.testMode = false

		def provider = new DefaultJavaProcessProvider()
		provider.javaOpts = ['-Xmx64m']
		provider.addCurrentClassPath()

		provider.mainClass = WorkflowRunner.class.name

		def actor = new WorkflowExecActor(1, provider, execConf, modulesConf)
		
		int numRun = 0;
		actor.onErrorListener = { numRun++; }
		actor.onExecCompletedListener = { numRun++; }

		actor.start()

		int numExpectRun = 2;
		def add = { QueuedWorkflow qwf ->
			actor << qwf;
			println("add: workflow='${qwf.name}' uuid='${qwf.uuid}'")
		}
		//send several workflows for execution
		add(new QueuedWorkflow('testflow', '123', [:]))
		add(new QueuedWorkflow('testflowException', '123', [:]))
		add(new QueuedWorkflow('testflow', '123', [:]))
		add(new QueuedWorkflow('testflowException', '123', [:]))
		add(new QueuedWorkflow('testflow', '123', [:]))
		add(new QueuedWorkflow('testflowException', '123', [:]))
		add(new QueuedWorkflow('testflow', '123', [:]))
		add(new QueuedWorkflow('testflowException', '123', [:]))

		while(numRun < numExpectRun){
			Thread.sleep(2000L)
		}
		Thread.sleep(2000L)

		actor.stop()

		assertTrue( Math.abs(numExpectRun-numRun) < 2 )
		
	}
	
}
