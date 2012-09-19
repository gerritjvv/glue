package org.glue.unit.log


import org.glue.unit.om.GlueContext

@Typed
interface GlueExecLoggerProvider {

	/**
	 * Returns a GlueExecLogger instance for the context.
	 * @param unitId
	 * @return GlueExecLogger
	 */
	GlueExecLogger get(String unitId)
	
}
