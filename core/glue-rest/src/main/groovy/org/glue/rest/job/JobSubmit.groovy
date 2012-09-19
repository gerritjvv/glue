package org.glue.rest.job

import org.apache.commons.collections.map.HashedMap
import org.codehaus.jackson.annotate.JsonProperty

/**
 * 
 * Encapsulates the job submission as a simple java object.
 *
 */
class JobSubmit{

	@JsonProperty
	String unitName
	
	@JsonProperty
	Map<String, String> params = new HashedMap<String, String>()
	
	
}
