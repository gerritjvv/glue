package org.glue.trigger.persist.db

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

import org.glue.trigger.persist.db.TriggersFileEntity.STATUS

/**
 * 
 * Store's the following information:
 *  <ul>
 *    <li>What unit ran against a trigger file<li>
 *    <li>What status did it set the trigger file to</li>
 *    <li>The date/time that this happened</li>
 *  
 *
 */
@Entity
@Table(name="trigger_files_unit_status")
public class TriggerFileUnitStatus {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id
	
	@Enumerated(value = EnumType.STRING)
	@Column(nullable=false)
	STATUS status
	
	@Column(name='unit_id', nullable = false)
	String unitId
	
}

