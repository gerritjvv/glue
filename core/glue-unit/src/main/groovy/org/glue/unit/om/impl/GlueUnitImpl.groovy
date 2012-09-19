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
		
		//parse the required modules and run configure on all modules
//		def modules=moduleFactory.getAvailableModules();
//		if(requiredModules != null && !this.modules.keySet().containsAll(requiredModules))
//		{
//			throw new UnitParsingException("One or more required modules [${requiredModules}] is not available as a module [${modules}]")
//		}
//		if(config.modules){
//			config.modules.each { name, moduleConf ->
//				if(this.modules.containsKey(name)){
//					this.modules.get(name).configure(this.unitId,moduleConf)
//				}
//				}
//		}
//		
		//now create the context
//		this.context.moduleFactory=moduleFactory
//		this.context.unitId=this.unitId;
//		this.context.unit = this; 
	}
	
	public List<TriggerDef> getTriggers(){
		return triggers
	}
	
}
