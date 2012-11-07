package org.glue.unit.om.impl;

import groovy.util.ConfigObject;


import java.util.Map;
import java.util.Set;

import org.glue.unit.exceptions.UnitParsingException;
import org.glue.unit.om.GlueContext;
import org.glue.unit.om.GlueModule;
import org.glue.unit.om.GlueProcess;
import org.glue.unit.om.GlueModuleFactory;
import org.glue.unit.om.GlueUnit;
import org.glue.unit.om.TriggerDef 
import org.apache.log4j.Logger;

/**
 * Object representation for the Glue groovy file.
 */
@Typed
class GlueUnitImpl implements GlueUnit {
	static final Logger log = Logger.getLogger(GlueUnitImpl.class)
	
	String name;
	
	boolean serial = true
	int priority = 0;
	
	Map<String,GlueProcess> processes;
	Set<String> requiredModules;
	
	TriggerDef[] triggers
	
	boolean notifyOnFail = false
	
	/**
	 * Builds a Glue object from the ConfigObject (this is the glue unit file)
	 * @param config
	 * @param moduleFactory
	 */
	@Typed(TypePolicy.DYNAMIC)
	public GlueUnitImpl(ConfigObject config){
		log.debug "Received $config as config object"
		this.name=config.name;
		
		
		if(config?.priority){
			try{
				priority = Integer.parseInt(config?.priority?.toString())
			}catch(Throwable t){
				priority = 0
			}
		}
		
		if(config?.serial){
			try{
				serial = Boolean.parseBoolean(config?.serial)
			}catch(Throwable t){
				serial = true
			}
		}
		
		//parse the triggers for a glue unit
		if(config?.triggers){
			triggers = TriggerDefImpl.parse(config.triggers)
		}else{
			triggers = []
		}
		
		//parse required modules
		this.requiredModules=new HashSet<String>();
		if(config.requiredModules)
		{
			this.requiredModules.addAll config.requiredModules.split(', *')
		}
		
		//parse the tasks that defines the actual processes to execute
		this.processes=new HashMap<String,GlueProcess>();
		if(config.tasks)
		{
			config.tasks.each { name, proc ->
				GlueProcess p = new GlueProcessImpl(name,proc);
				this.processes.put name, p
			}
		}
		
		if(config?.notifyOnFail){
			try{
				this.notifyOnFail = Boolean.valueOf(config.notifyOnFail)
			}catch(Exception exc){
			  this.notifyOnFail = true
			}
		}
		
	}
	
	public List<TriggerDef> getTriggers(){
		return triggers
	}
	
}
