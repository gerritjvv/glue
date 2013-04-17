package org.glue.unit.om

import org.glue.unit.process.TaskExecutor


abstract class DefaultGlueContext implements GlueContext{

	
	def withTimeout(long timeout, Closure cl) {
		return TimeoutWrapper.run(timeout, cl)
	}
	
	def parallel(int threads, boolean failOnError){
		return new TaskExecutor(threads, failOnError)
	}
	
}
