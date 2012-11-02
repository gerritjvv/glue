
package org.glue.unit.status.impl.db



import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table

import org.glue.unit.exec.GlueState
import org.glue.unit.status.UnitStatus

/**
 *
 * Represents the execution of the whole GlueUnit and its life cycles.<br/>
 * Each GlueUnit has several processes associated with it.  
 * 
 */
@Typed
@Entity
@Table(name = "units")
@NamedQueries(value=[
	@NamedQuery(name='UnitEntity.byDateRange',
	query='from UnitEntity e where  e.startDate>= :startDate AND e.endDate <= :endDate ORDER BY e.startDate'),
    @NamedQuery(name='UnitEntity.byNameAndDateRange',
	query='from UnitEntity e where  name = :name AND e.startDate>= :startDate AND e.endDate <= :endDate ORDER BY e.startDate'),
    @NamedQuery(name='UnitEntity.byLatestName',
	query='from UnitEntity e where  name = :name ORDER BY e.startDate DESC limit 1')
])
public class UnitEntity {
	
	/**
	 * This is is assigned by the Glue process execution
	 */
	@Id
	@Column(name = "unit_id", nullable=false)
	String unitId
	
	@Column(nullable=false)
	String name
	
	@Column(name = "start_date", nullable=false)
	Date startDate
	
	@Column(name = "end_date")
	Date endDate
	
	@Enumerated(value = EnumType.STRING)
	@Column(nullable=false)
	GlueState status
	
	@Column(name='unit_progress', nullable=false)
    double progress = 0D
	
		
	public UnitEntity(){
		
	}
	
	public UnitEntity(UnitStatus unitStatus){
		update(unitStatus)
	}
	
	void update(UnitStatus unitStatus){
		unitId = unitStatus.unitId
		name = unitStatus.name
		startDate = unitStatus.startDate
		endDate = unitStatus.endDate
		status = unitStatus.status
		progress = unitStatus.progress
	}
	
	UnitStatus toUnitStatus(){
		return new UnitStatus(
			unitId:unitId,
			name:name,
			startDate:startDate,
			endDate:endDate,
			status:status,			
			progress:progress
			)
	}
			
}
