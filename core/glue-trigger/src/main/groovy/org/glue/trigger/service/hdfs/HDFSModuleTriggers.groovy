package org.glue.trigger.service.hdfs

import java.util.List;
import java.util.Map;
import org.glue.modules.hadoop.HDFSModule 
import org.glue.modules.hadoop.impl.HDFSModuleImpl;
import org.glue.unit.om.TriggerDef 

/**
 * Helper class to contain a HDFSModule and all its triggers
 *
 */
@Typed(TypePolicy.MIXED)
class HDFSModuleTriggers {
	
	/**
	* Maps a HDFS path to a list of GlueUnit names
	*/
	Map<String, List<String>> pathToGlueUnitMap = [:]
	
	HDFSModule hdfsModule
	
	public void close(){
		if(hdfsModule instanceof HDFSModuleImpl){
			((HDFSModuleImpl)hdfsModule).close()
		}
	}
	/**
	 * Adds the trigger of a GlueUnit
	 * @param glueUnitName
	 * @param triggerDef
	 * @return
	 */
	public void addTrigger(String glueUnitName, TriggerDef triggerDef){
		
		List<String> savedGlueUnits = pathToGlueUnitMap[triggerDef.value]
		if(!savedGlueUnits){ //if null
			savedGlueUnits = [] as List<String>
			pathToGlueUnitMap[triggerDef.value]  = savedGlueUnits
		}
		
		savedGlueUnits << glueUnitName	
	}
	
	/**
	 * Gets all of the HDFS paths
	 * @return
	 */
	public Set<String> getPaths(){
		pathToGlueUnitMap.keySet()
	}
	
	/**
	 * Gets all of the glue unit names associated with the path passed
	 * @param path
	 * @return
	 */
	public List<String> getGlueUnitNames(String path){
		pathToGlueUnitMap.get path
	}
}
