package org.glue.rest.util

import org.codehaus.jackson.map.ObjectMapper
import org.glue.rest.job.JobSubmit;
import org.glue.rest.job.JobSubmitResponse;
import org.restlet.data.MediaType
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Get
import org.restlet.resource.Post
import org.restlet.resource.ServerResource

/**
 * 
 * A simple resource that returns the arguments sent to it as a response
 *
 */
class MockSubmitResource extends ServerResource{

	static final ObjectMapper mapper = new ObjectMapper()

	@Post("json")
	public JobSubmitResponse submit(JobSubmit jobSubmit) {
		
		return new JobSubmitResponse(unitId:"123", unitName:jobSubmit.unitName, createTime:new Date())
		
	}
	
}
