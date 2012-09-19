package org.glue.unit.om

/**
 * 
 * A specific provider for creating module factories
 *
 */
@Typed
interface GlueModuleFactoryProvider {

	/**
	 * Returns a unique glue module factory for the context
	 * @param context
	 * @return GlueModuleFactory
	 */
	GlueModuleFactory get(GlueContext context)
	
	
}
