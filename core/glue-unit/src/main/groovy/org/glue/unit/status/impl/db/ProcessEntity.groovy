
package org.glue.unit.status.impl.db




import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table

import org.glue.unit.exec.GlueState
import org.glue.unit.status.ProcessStatus

/**
 *
 * Each glue unit contains multiple glue processes. This entity persists the life cycle of each process execution.
 *
 */
@Typed
@Entity
@Table(name = "processes")
@NamedQueries(value=[
	@NamedQuery(name='ProcessEntity.byUnitId',
	query='from ProcessEntity e where e.id.unitId = :unitId')	
]
)
class ProcessEntity {

	@EmbeddedId
	ProcessId id

	@Column(name = "start_date", nullable=false)
	Date startDate

	@Column(name = "end_date")
	Date endDate

	@Enumerated(value = EnumType.STRING)
	@Column(nullable=false)
	GlueState status

	@Column(length=1000)
	String error

	@Column(name='unit_progress', nullable=false)
	double progress = 0D

	public ProcessEntity(){

	}
	
	void update(ProcessStatus processStatus){
		id = new ProcessId(unitId:processStatus.unitId, processName:processStatus.processName)
		startDate = processStatus.startDate
		endDate = processStatus.endDate
		status = processStatus.status
		error = processStatus.error
		progress = processStatus.progress
	}
	
	public ProcessEntity(ProcessStatus processStatus){
		super()
		update(processStatus)
	}
	
	ProcessStatus toProcessStatus(){
		return new ProcessStatus(
			unitId:(id?.unitId),
			processName:(id?.processName),
			startDate:startDate,
			endDate:endDate,
			status:status,
			error:error,
			progress:progress
			)	
	}
	
}
