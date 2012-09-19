package org.glue.unit.log.impl



import org.glue.unit.log.GlueExecLogger
import org.glue.unit.log.GlueExecLoggerProvider


/**
 * 
 * Creates GlueExecLoggers that will write out log messages for a unit execution<br/>
 * Each unit execution's logs are written in the following folder:<br/>
 * ${baseDir}/${unique unit id}/main
 *
 */
@Typed
class DefaultGlueExecLoggerProvider implements GlueExecLoggerProvider{
	
	/**
	 * 
	 */
	File baseDir
	
	/**
	 * 
	 */
	DefaultGlueExecLoggerProvider(File baseDir){
		this.baseDir = baseDir
	}
	
	/**
	* Returns a GlueExecLogger instance for the context.
	* @param unitId
	* @return GlueExecLogger
	*/
   GlueExecLogger get(String unitId){
	   
	  	File logDir = new File(baseDir, unitId)
		logDir.mkdirs()
	   
		return new GlueExecLoggerImpl(logDir)
		
   }
   

}
