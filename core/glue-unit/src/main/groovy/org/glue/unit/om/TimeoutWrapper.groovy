package org.glue.unit.om

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 
 * Is used to run a task and timeout 
 * 
 */
class TimeoutWrapper {

	static final ExecutorService service = Executors.newCachedThreadPool()
	

	static Object run(long timeout, Closure cls){
		return service.submit(new Callable<Object>(){
			public Object call(){
				return cls()
			}
		}).get(timeout, TimeUnit.MILLISECONDS)
	}	
	
}
