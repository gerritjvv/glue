package org.glue.gluetest

import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

/**
 *
 * A GlueModule that does nothing
 *
 */
class MockGlueModule implements GlueModule{

	ConfigObject config

	def propertyMissing(name, value){
		
	}
	def propertyMissing(name){
		null
	}

	def methodMissing(String methodName, args){
		//do nothing
	}
	
	void init(ConfigObject config){
		this.config = config
	}

	public Map getInfo(){
		[:]
	}

	public String getName(){
		"mockModule"
	}
	
	/**
	 * Returns the name
	 * @param name
	 * @return
	 */
	public String getName(String name){
		name
	}
	
	void onProcessKill(GlueProcess process, GlueContext context){
		
	}
	
	void destroy(){
	}

	void configure(String unitId, ConfigObject config){
	}


	void onUnitStart(GlueUnit unit, GlueContext context){
	}
	void onUnitFinish(GlueUnit unit, GlueContext context){
	}
	void onUnitFail(GlueUnit unit, GlueContext context){
	}

	Boolean canProcessRun(GlueProcess process, GlueContext context){
		true
	}
	void onProcessStart(GlueProcess process,GlueContext context){
	}
	void onProcessFinish(GlueProcess process, GlueContext context){
	}
	void onProcessFail(GlueProcess process, GlueContext context, Throwable t){
	}
}
