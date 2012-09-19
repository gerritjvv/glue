package org.glue.unit.status.impl.db


import java.io.Serializable;

import javax.persistence.Column 
import javax.persistence.Embeddable;


/**
 *
 * Composite id for the ProcessEntity
 *
 */
@Typed
@Embeddable
class ProcessId implements Serializable{
	
		@Column(name = "unit_id", nullable=false)
		String unitId
		@Column(name = "process_name", nullable=false)
		String processName

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((processName == null) ? 0 : processName.hashCode());
			result = prime * result
					+ ((unitId == null) ? 0 : unitId.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProcessId other = (ProcessId) obj;
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
