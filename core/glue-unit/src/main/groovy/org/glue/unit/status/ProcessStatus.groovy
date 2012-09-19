package org.glue.unit.status

import java.util.Date

import org.glue.unit.exec.GlueState

/**
 * 
 * This class is a simple pojo that contains a snapshot of the current glue unit's process execution
 *
 */
@Typed
class ProcessStatus {

	String unitId

	String processName

	Date startDate

	Date endDate

	GlueState status = GlueState.WAITING

	String error

	double progress = 0D

	def toMap(){

		[
					unitId:unitId,
					processName:processName,
					startDate:startDate,
					endDate:endDate,
					status:status,
					error:error,
					progress:progress
				]
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		+ ((processName == null) ? 0 : processName.hashCode());
		result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (!obj)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessStatus other = (ProcessStatus) obj;
		if (processName == null) {
			if (other.processName != null)
				return false;
		} else if (!processName.equals(other.processName))
			return false;
		if (unitId == null) {
			if (other.unitId != null)
				return false;
		} else if (!unitId.equals(other.unitId))
			return false;
		return true;
	}
}
