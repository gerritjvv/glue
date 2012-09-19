package org.glue.rest.job

import org.codehaus.jackson.annotate.JsonProperty

/**
 * Encapsulates a successful job submit response.
 *
 */
class JobSubmitResponse {

	@JsonProperty
	String unitName
	
	@JsonProperty
	String unitId
	
	@JsonProperty
	Date createTime
	
}
