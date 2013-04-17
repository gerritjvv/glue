package org.glue.unit.om;

import org.glue.unit.log.GlueExecLogger
import org.glue.unit.status.GlueUnitStatusManager

/**
 * 
 * Each GlueUnit has a context during its execution.
 *
 */
@Typed
public interface GlueContext {

	String getUnitId()
	void setUnitId(String unitId)

	GlueUnit getUnit()
	void setUnit(GlueUnit unit)
	
	Map<String, String> getArgs()
	void setArgs(Map<String, String> args)
	
	Object getProperty(String name);
	void setProperty(String name, Object value);

	void write(Writer writer) throws IOException;

	GlueModuleFactory getModuleFactory()
	
	GlueUnitStatusManager getStatusManager()
	void setStatusManager(GlueUnitStatusManager statusManager)

	GlueExecLogger getLogger()
	void setLogger(GlueExecLogger logger)

	def parallel(int threads, boolean failOnError)
	
	def eval(className, method, values)
	def eval(className, method)
	
	def newInstance(className, arg)
	def newInstance(className)
	
	def withTimeout(long timeout, Closure clj)
	
	/**
	 * Called to allow the context to remove and resources
	 */
	void destroy()
}
