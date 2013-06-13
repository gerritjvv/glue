package org.glue.unit.om.impl

import org.glue.unit.om.CallHelper;
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

/**
 *
 * A GlueModule that does nothing
 *
 */
@Typed(TypePolicy.DYNAMIC)
class MockGlueUnicodeModule implements GlueModule{

	ConfigObject config

	void init(ConfigObject config){
		this.config = config
	}

	public Map getInfo(){
		[:]
	}

	   
	/**
	 * Returns the name
	 * @param name
	 * @return
	 */
	public String getName(String name){
		name
	}
	
	
	public String getUnicode(){
		return "€ and one more Ñ Ã AMS Hortolândia"
	}
	
	public void callUnicode(cls){
		CallHelper.makeCallable(cls).call("€ and one more Ñ Ã AMS Hortolândia")
	}
	
	
	/**
	 * returns the unitId property in the context 
	 */
	public String getUnitId(GlueContext context, String name){
		context?.unitId
	}
	
	public String getName(){
		"mockGlueUnicodeModule"
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
