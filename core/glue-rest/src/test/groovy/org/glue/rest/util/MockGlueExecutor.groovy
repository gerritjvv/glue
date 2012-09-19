package org.glue.rest.util


import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import org.glue.unit.exceptions.UnitSubmissionException
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.GlueState
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.om.GlueContext

/**
 * Utility executor.<br/>
 * This class counts the execution per glue unit, i.e. each time the submit methods are called that unit's counter is incremented.
 */
class MockGlueExecutor implements GlueExecutor{

	final Map<String, AtomicInteger> glueUnitExecMap = new ConcurrentHashMap<String, AtomicInteger>()
	final Map<String, Set<String>> glueUnitExecDirecties = new ConcurrentHashMap<String, AtomicInteger>()

	public Integer getCount(String unitName){
		return glueUnitExecMap[unitName]?.get()
	}

	public Boolean hasUnit(String unitName){
		return glueUnitExecMap.containsKey(unitName)
	}

	public Set<String> getUpdatedFiles(String unitName){
		return 	glueUnitExecDirecties[unitName]
	}


	void register(String unitName){
		
		AtomicInteger i = glueUnitExecMap[unitName]
		if(!i){
			i = new AtomicInteger(0)
			glueUnitExecMap[unitName] = i
		}
		i.incrementAndGet()
	}

	void registerDir(String unitName, Map<String, String> params){
		Set<String> directories = glueUnitExecDirecties[unitName]
		if(!directories){
			directories = new HashSet<String>()
			glueUnitExecDirecties[unitName] = directories
		}
		params['updatedFiles']?.each { String dir ->
			directories << dir 
		}
	}

	String submitUnitAsUrl(URL unitUrl, Map<String, String> params) throws UnitSubmissionException {
		synchronized(glueUnitExecMap){
			register(unitUrl)
			registerDir(unitUrl, params)
		}
		"ABC-" + unitUrl
	}

	String submitUnitAsText(String unitText, Map<String, String> params)
	throws UnitSubmissionException {
		synchronized(glueUnitExecMap){
			register(unitText)
			registerDir(unitText, params)
		}
		"ABC-" + unitText
	}

	String submitUnitAsName(String unitName, Map<String, String> params, String unitId = null)
	throws UnitSubmissionException {
		if(!unitId){
			//create a unique id that will represent this GlueUnit instance's execution
			unitId=java.util.UUID.randomUUID().toString();
		}
		
		synchronized(glueUnitExecMap){
			register(unitName)
			registerDir(unitName, params)
		}
		"ABC-" + unitName
	}

	
	/**
	* Will terminate the workflow itendified by the unitId
	* @param unitId
	*/
   public void terminate(String unitId){}
   
   public GlueContext getContext(String unitId){}
   
   /**
	* Returns a list of all the currently running GlueUnit executions in the form of a UnitExecutor per GlueUnit execution.
	* @return
	*/
   public Map<String, UnitExecutor> getUnitList(){null}

   /**
	* Shutdown the execution for all GlueUnits
	*/
   public void shutdown(){}

   /**
	* Wait for all current submitted GlueUnits to completed but does not accept
	* any new submissions.
	*/
   public void waitUntillShutdown(){}

   /**
	* Gets the state of a unit
	*/
   GlueState getStatus(String unitId){null}
   
   /**
	* Gets the progress on a unit
	* @param unitId
	* @return
	*/
   double getProgress(String unitId){1D}
   
   /**
	* Waits for a unit to complete
	*/
   void waitFor(String unitId){}
   
   /**
	* Waits for a unit to complete or time out
	*/
   void waitFor(String unitName, long time, java.util.concurrent.TimeUnit timeUnit){}
}