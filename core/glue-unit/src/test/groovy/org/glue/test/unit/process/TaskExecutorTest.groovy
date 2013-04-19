package org.glue.test.unit.process

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger;

import org.glue.unit.process.TaskExecutor
import org.junit.Test


class TaskExecutorTest {


	@Test(expected=Exception.class)
	public void testFailAll() throws Throwable{


		new TaskExecutor(2, true).
				submit({ Thread.sleep(10); throw new Exception("Error") })
				.submit({Thread.sleep(10)})
				.await(10000)
	}


	@Test(expected=Exception.class)
	public void testFail() throws Throwable{

		AtomicBoolean t1 = new AtomicBoolean(false)
		try{
			new TaskExecutor(2, false).
					submit({ Thread.sleep(10); throw new Exception("Error") })
					.submit({Thread.sleep(10); t1.set(true);})
					.await(10000)
		}finally{
			assert(t1.get())
		}
		
	}


	@Test
	public void testNoFail() throws Throwable{

		AtomicBoolean t1 = new AtomicBoolean(false)
		AtomicBoolean t2 = new AtomicBoolean(false)


		new TaskExecutor(2, false).
				submit({ Thread.sleep(10); t1.set(true); })
				.submit({Thread.sleep(10); t2.set(true); })
				.await(10000)

		assert(t1.get() && t2.get())
	}
	
	
	/**
	 * RejectedExecutionException
	 * Is thrown by script when using two parallels p1 p2, p1 submits to p2.
	 *on complete p1.await then p2.await is called.
	 * @throws Throwable
	 */
	@Test
	public void testIssue24() throws Throwable{
		
		def p1 = new TaskExecutor(10, true)
		def p2 = new TaskExecutor(10, true)
		
		def work = { println new Random().nextInt() }
		def submitWork = { (0..10).each { p2.submit(work); Thread.sleep(100);} }
		
		(0..10).each {
			p1.submit(submitWork)
			
		}
				
		p1.await(1000000)
		p2.await(1000000)
		
		//we're not testing anything more than that we're able to run and submit 
		//to both p1 p2 and wait for orderly completion.
		
	}
}
