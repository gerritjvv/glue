package org.glue.test.unit.process

import java.util.concurrent.atomic.AtomicBoolean

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
}
