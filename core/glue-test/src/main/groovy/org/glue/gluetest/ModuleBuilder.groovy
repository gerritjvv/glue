package org.glue.gluetest


/**
 * Helps write helper code that automatically adds modules to the GlueServer
 */
interface ModuleBuilder {

	String getName()
	
	boolean isSingleton()
	
	/**
	 * Return a new instance of the module
	 * @param glueServer
	 * @return ConfigObject
	 */
	ConfigObject buildModule(GlueServer glueServer)
	/**
	 * Cleanup any resources
	 */
	void close()
	
}
