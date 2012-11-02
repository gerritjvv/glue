package org.glue.unit.status

import java.util.Collection;
import java.util.Date;

import org.glue.unit.om.GlueUnit

/**
 * 
 * Used to provide a view of the unit status.<br/>
 * GlueUnit execution status is not directly implemented inside the Glue Executor it self.<br/>
 * The implementation can be found inside the DbStoreModule
 *
 */
@Typed
interface GlueUnitStatusManager {

	
	/**
	 * 
	 * @param workflowName
	 * @param rangeStart
	 * @param rangeEnd
	 * @return
	 */
	Collection<UnitStatus> findUnitStatus(String workflowName, Date rangeStart, Date rangeEnd)
	
	/**
	 * 
	 * @param rangeStart
	 * @param rangeEnd
	 * @return Collection of UnitStatus
	 */
	Collection<UnitStatus> findUnitStatus(Date rangeStart, Date rangeEnd)
	
	/**
	 * Sets the status of a GlueUnit
	 * @param unitStatus
	 */
	void setUnitStatus(UnitStatus unitStatus);
	
	/**
	 * Sets the status of a Process
	 * @param unitStatus
	 */
	void setProcessStatus(ProcessStatus unitStatus);
	
	/**
	 * @param unitId
	 * @return UnitStatus or null if the unit was not found
	 */
	UnitStatus getUnitStatus(String unitId)
	
	/**
	 * @param workflowName
	 * @return UnitStatus or null if the unit was not found
	 */
	UnitStatus getLatestUnitStatus(String workflowName)
	
	/**
	 * Gets a collection of unit ProcessStatus instances ofr the unitId
	 * @param unitId The unit id
	 * @return Collection of ProcessStatus or nullt if the unit was not found
	 */
	Collection<ProcessStatus> getUnitProcesses(String unitId)
	
	/**
	 * Gets the process status of a unit
	 * @param unitId
	 * @param processName The process name
	 * @return ProcesStatus null if not found
	 */
	ProcessStatus getProcessStatus(String unitId, String processName)
	
	void init(ConfigObject config)
	
	void destroy()
	
}
