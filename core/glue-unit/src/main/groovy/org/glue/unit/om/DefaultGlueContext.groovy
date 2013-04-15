package org.glue.unit.om


abstract class DefaultGlueContext implements GlueContext{

	
	def withTimeout(long timeout, Closure cl) {
		return TimeoutWrapper.run(timeout, cl)
	}
	
}
