package org.glue.unit.om.impl

import org.glue.unit.om.GlueProcess 
import org.apache.log4j.Logger;

/**
 * 
 * See javadoc on GlueProcess.
 *
 */
@Typed
class GlueProcessImpl implements GlueProcess{
	private static final Logger log = Logger.getLogger(GlueProcessImpl.class)
	
	String description;
	String name;
	Set<String> dependencies;
	
	Closure error;
	Closure success;
	Closure task;
	
	boolean equals(GlueProcess glueProcess){ name?.equals( glueProcess?.name )	}
	int hashCode(){ name?.hashCode() }
	String toString(){ "GlueProcess $name" }
	
	public GlueProcessImpl(String name, ConfigObject config)
	{
		log.debug "received $config as process configuration"
		this.name = name;
		this.description = name
		this.dependencies=new HashSet<String>();
		if(config.dependencies){ config.dependencies.toString().split(', *').each{it-> this.dependencies.add(it) } }
		if(config.description) this.description=config.description;
		this.task=config.tasks;
		if(config.error) this.error=config.error;
		if(config.success) this.success=config.success;
	}
	
}
