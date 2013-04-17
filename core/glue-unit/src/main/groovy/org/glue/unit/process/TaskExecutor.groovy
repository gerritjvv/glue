package org.glue.unit.process

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * 
 * A utility class to be used by workflow accessed via the context.
 * 
 */
@Typed
class TaskExecutor {

	final ExecutorService service;
	final boolean failOnError;
	AtomicBoolean failed = new AtomicBoolean(false);
	AtomicReference<Throwable> excpRef = new AtomicReference<Throwable>()


	public TaskExecutor(int threads, boolean failOnError){
		service = Executors.newFixedThreadPool(threads);
		this.failOnError = failOnError;
	}

	public TaskExecutor submit(Closure closure){
		service.submit(new Callable<Object>(){
					public Object call() throws Exception{
						if(failed.get()) return null
						try{
							return closure()
						}catch(Throwable excp){
							failed.set(true)
							excpRef.set(excp)

							//this needs to be the last call here
							if(failOnError)
								service.shutdownNow()
						}
					}
				})
		return this
	}

	/**
	 * Waits for all tasks to complete.<br/>
	 * After this method is called no more tasks can be sent.<br/>
	 * If any of the tasks threw an error the error is rethrown.<br/>
	 * A TimeoutException is thrown if the tasks did not complete without the timeout period<br/> 
	 */
	public void await(long timeout){
		service.shutdown()
		if(!service.awaitTermination(timeout, TimeUnit.MILLISECONDS))
			throw new TimeoutException()

		if(failed.get())
			throw excpRef.get()
		
		
	}
}
