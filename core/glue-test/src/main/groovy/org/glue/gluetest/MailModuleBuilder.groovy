package org.glue.gluetest

import org.glue.gluetest.mail.MockMailModule
import org.glue.unit.script.ScriptClassCache

/**
 * 
 * Builds a mail module with a mock module 
 *
 */
class MailModuleBuilder implements ModuleBuilder{
   
	String name = "mail"
	boolean singleton = true
		
	
	/**
	 * Return a new instance of the module
	 * @param glueServer
	 * @return ConfigObject
	 */
	ConfigObject buildModule(GlueServer glueServer){

		return ScriptClassCache.getDefaultInstance().parse("""
			
					className='${MockMailModule.class.name}'
					isSingleton=true
					config{
						
					}
			   """)
	
				
	}
	
	/**
	 * Cleanup any resources
	 */
	void close(){
		
	}
	
}
