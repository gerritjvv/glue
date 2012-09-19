package org.glue.unit.status.impl

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock

import org.glue.unit.status.GlueUnitStatusManager
import org.glue.unit.status.ProcessStatus
import org.glue.unit.status.UnitStatus

/**
 * 
 * This class stores the unit status into a map.<br/>
 * Its mainly used for testing and not for running a production system.<br/>
 * <p/>
 * This instance is thread safe and uses the java ReentrantReadWriteLock to synchronise read/write.
 */
@Typed
class MapUnitStatusManager implements GlueUnitStatusManager{

	ReentrantReadWriteLock lock = new ReentrantReadWriteLock()

	/**
	 * Maps key = unitId, value = UnitStatus
	 */
	Map<String, UnitStatus> unitStatusMap = new ConcurrentHashMap<String, UnitStatus>()
	/**
	 * Maps key = unitId, value = map { key=processName, value=ProcessStatus}
	 */
	Map<String, Map<String,ProcessStatus>> processStatusMap = new ConcurrentHashMap<String, UnitStatus>()


	/**
	 * 
	 * @param rangeStart
	 * @param rangeEnd
	 * @return Collection of UnitStatus
	 */
	Collection<UnitStatus> findUnitStatus(Date rangeStart, Date rangeEnd){
		Collection<UnitStatus> list = []

		//choose dates so that startDate < endDate
		Date startDate, endDate

		if(rangeStart.time > rangeEnd.time){
			startDate = rangeEnd
			endDate = rangeStart
		}else{
			startDate = rangeStart
			endDate = rangeEnd
		}

		unitStatusMap.values().each { UnitStatus unitStatus ->
			if(unitStatus.startDate >= startDate && unitStatus.endDate <= endDate){
				list << unitStatus
			}
		}

		return list
	}

	/**
	 * Sets the status of a GlueUnit
	 * @param unitStatus
	 */
	void setUnitStatus(UnitStatus unitStatus){
		lock.writeLock().lockInterruptibly()
		try{
			unitStatusMap[unitStatus.unitId] = unitStatus
		}finally{
			lock.writeLock().unlock()
		}
		return
	}

	/**
	 * Sets the status of a Process
	 * @param unitStatus
	 */
	void setProcessStatus(ProcessStatus processStatus){
		lock.writeLock().lockInterruptibly()
		try{
			Map<String,ProcessStatus> processMap = processStatusMap[processStatus.unitId]
			if(!processMap){
				processMap = new ConcurrentHashMap<String, ProcessStatus>()
				processStatusMap[processStatus.unitId] = processMap
			}
			processMap[processStatus.processName] = processStatus
		}finally{
			lock.writeLock().unlock()
		}
		return
	}

	/**
	 * @param unitId
	 * @return UnitStatus or null if the unit was not found
	 */
	UnitStatus getUnitStatus(String unitId){
		lock.readLock().lockInterruptibly()
		try{
			return unitStatusMap[unitId]
		}finally{
			lock.readLock().unlock()
		}
		return
	}

	/**
	 * Gets a collection of unit ProcessStatus instances ofr the unitId
	 * @param unitId The unit id
	 * @return Collection of ProcessStatus or nullt if the unit was not found
	 */
	Collection<ProcessStatus> getUnitProcesses(String unitId){
		lock.readLock().lockInterruptibly()
		try{
			return processStatusMap[unitId]?.values()
		}finally{
			lock.readLock().unlock()
		}
		return
	}

	/**
	 * Gets the process status of a unit
	 * @param unitId
	 * @param processName The process name
	 * @return ProcesStatus null if not found
	 */
	ProcessStatus getProcessStatus(String unitId, String processName){
		lock.readLock().lockInterruptibly()
		try{
			//note: we must use get here and cannot use the . property or else null is returned
			return processStatusMap[unitId]?.get(processName)
		}finally{
			lock.readLock().unlock()
		}
		return
	}

	void init(ConfigObject config){
	}

	void destroy(){
		unitStatusMap.clear()
		processStatusMap.clear()
	}
}
