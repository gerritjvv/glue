package org.glue.unit.om.impl

import groovy.util.ConfigObject;

import org.glue.unit.om.GlueContext;
import org.glue.unit.om.GlueProcess;
import org.glue.unit.om.GlueUnit;
import org.glue.unit.om.GlueModule

/**
 *
 * A GlueModule that does nothing
 *
 */
@Typed(TypePolicy.DYNAMIC)
class MockGlueModule implements GlueModule{

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
	
	/**
	 * Test method that returns the first item in the argument
	 * @param arrayList
	 * @return Object
	 */
	public Object getFirstItemInArray(Object[] arrayList){
		println "MockGlueModule:getFirstItemInArray: ${arrayList} returning ${arrayList[0]}"
		return arrayList[0]
	}
	
	/**
	* Test method that returns the first item in the argument
	* @param arrayList
	* @return Object
	*/
   public Object getFirstItemInCollection(Collection arrayList){
	   println "MockGlueModule:getFirstItemInArray: ${arrayList} returning ${arrayList[0]}"
	   return arrayList[0]
   }
   
	
	/**
	* Test method that returns the first item in the argument
	* @param arrayList
	* @return Object
	*/
   public Object getFirstItemInArrayWithContext(GlueContext context, Object[] arrayList){
	   return arrayList[0]
   }
   /**
   * Test method that returns the first item in the argument
   * @param arrayList
   * @return Object
   */
  public Object getFirstItemInArrayNameWithContext(GlueContext context, String name, Object[] arrayList){
	  return arrayList[0]
  }
	
	
	/**
	 * returns the unitId property in the context 
	 */
	public String getUnitId(GlueContext context, String name){
		context?.unitId
	}
	
	public String getName(){
		"testModule"
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
