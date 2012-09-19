package org.glue.trigger.persist.db

import java.io.Serializable;

import javax.persistence.Column 
import javax.persistence.Embeddable;
import javax.persistence.Entity 
import javax.persistence.EnumType 
import javax.persistence.Enumerated 
import javax.persistence.GeneratedValue 
import javax.persistence.GenerationType;
import javax.persistence.Id 
import javax.persistence.Table 

/**
 * 
 * Defines a checkpoint id.
 *
 */
@Typed
@Embeddable
class TriggersCheckPointEntityId implements Serializable{
	
	@Column( name='unit_name', nullable = false)
	String unitName
	
	@Column(nullable = false)
	String path

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result
				+ ((unitName == null) ? 0 : unitName.hashCode());
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
		TriggersCheckPointEntityId other = (TriggersCheckPointEntityId) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (unitName == null) {
			if (other.unitName != null)
				return false;
		} else if (!unitName.equals(other.unitName))
			return false;
		return true;
	}	
	
}
