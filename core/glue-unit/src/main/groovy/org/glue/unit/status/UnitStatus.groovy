package org.glue.unit.status

import java.util.Date

import org.glue.unit.exec.GlueState

/**
 * 
 * This class is a simple pojo that contains a snapshot of the current glue unit's execution
 *
 */
@Typed
class UnitStatus {

	/**
	 * This is is assigned by the Glue process execution
	 */
	String unitId

	String name

	Date startDate

	Date endDate

	GlueState status = GlueState.WAITING

	double progress = 0D

	def toMap(){
		[
					unitId:unitId,
					name:name,
					startDate:startDate,
					endDate:endDate,
					status:status,
					progress:progress
				]
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (!obj)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnitStatus other = (UnitStatus) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (unitId == null) {
			if (other.unitId != null)
				return false;
		} else if (!unitId.equals(other.unitId))
			return false;
		return true;
	}
}
