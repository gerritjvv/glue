
package org.glue.modules.db



import java.io.Serializable;

import javax.persistence.Column 
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.EnumType 
import javax.persistence.Enumerated 
import javax.persistence.Id 
import javax.persistence.Table;
import javax.persistence.Entity;
import org.glue.unit.om.GlueUnit;

/**
 *
 * Represents a KeyValue that can be stored by any process in any glue unit.<br/>
 * The processName is supported that defines two types via convention.<br/>
 * Type 1: 'global' this is available to any process or glue unit.
 * Type 2: unit name this is available to a certain glue unit only. 
 * 
 * Both keys and values are treated as String types.<br/>
 * Values have a maximum value of 1000 characters.<br/>
 *
 */
@Typed
@Entity
@Table(name = "key_values")
public class KeyValueEntity {
	
	@Id
	@Column(name="glue_key", nullable=false)
	String key 
	
	@Column(name="glue_value", length=1000)
	String value

}
