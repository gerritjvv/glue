package org.glue.unit.exceptions


/**
 * 
 * Thrown when an error happens while configuring a GlueModule or a required parameter is missing. 
 *
 */
@Typed
class ModuleConfigurationException extends RuntimeException{

	String msg 
	ConfigObject moduleConfig
	
	public ModuleConfigurationException(String msg){
		super(msg)
		this.msg = msg
	}
	
	public ModuleConfigurationException(String msg, ConfigObject moduleConfig, Throwable t) {
		super(msg, t);
		this.msg = msg
		this.moduleConfig = moduleConfig
	}

	public ModuleConfigurationException(String msg, ConfigObject moduleConfig) {
		super(msg);
		this.msg = msg
		this.moduleConfig = moduleConfig
	}

	
	String toString(){
		
		String reason = getCause()?.getMessage()
		
		"""
		
			##Bad Module Configuration##
			${msg}
			--------------------------------------------------------
			${reason}
			--------------------------------------------------------
			<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			${(moduleConfig) ? moduleConfig : ''}
			<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				
		"""
		
	}
	
}
