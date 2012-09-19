package org.glue.unit.om

/**
 * Builds to GlueContext with all its inner dependencies like GlueModuleFactory etc.
 */
@Typed
interface GlueContextBuilder {

	GlueContext build(String unitId, GlueUnit unit, Map<String,String> params)
	
}
