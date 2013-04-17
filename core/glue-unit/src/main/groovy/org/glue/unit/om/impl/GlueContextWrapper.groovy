package org.glue.unit.om.impl

import org.glue.unit.log.GlueExecLogger
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.process.TaskExecutor
import org.glue.unit.status.GlueUnitStatusManager

/**
 * 
 * Used to wrap another GlueContext and delegate work to it.
 * This class useful for subclasses
 *
 */
class GlueContextWrapper implements GlueContext{

	GlueUnit unit
	Map<String, String> args
	
	String unitId
	GlueModuleFactory moduleFactory
	GlueUnitStatusManager statusManager

	GlueExecLogger logger
	
	GlueContext parent
	
	GlueContextWrapper(GlueContext ctx){
		parent = ctx
		logger = ctx.logger
		statusManager = ctx.statusManager
		moduleFactory = ctx.moduleFactory
		unitId = ctx.unitId
		unit = ctx.unit
		args = ctx.args
	}
	
	
	def eval(className, method, values){
		parent.eval(className, method, values)
	}
	def eval(className, method){
		parent.eval(className, method)
	}
	
	def newInstance(className, arg){
		parent.newInstance(className, arg)
	}
	def newInstance(className){
		parent.newInstance(className)
	}
	
	def parallel(int threads, boolean failOnError){
		return new TaskExecutor(threads, failOnError)
	}
	
	void destroy(){
		parent.destroy()
	}
	
	void write(Writer writer) throws IOException{
		parent.write(writer)	
	}
	
	def withTimeout(long timeout, Closure cl) {
		parent.withTimeout(timeout, cl)
	}
	
}
