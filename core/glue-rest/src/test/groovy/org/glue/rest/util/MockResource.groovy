package org.glue.rest.util

import org.codehaus.jackson.map.ObjectMapper
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
class MockResource extends ServerResource{

	static final ObjectMapper mapper = new ObjectMapper()

	@Post("json")
	public Representation setStatus(Representation entity){

		Map<String, String> attributes = getRequest().getAttributes()

		new StringRepresentation(this.mapper.writeValueAsString(attributes),MediaType.APPLICATION_JSON)
	}

	@Get("json")
	public Representation getStatus(Representation entity){

		Map<String, String> attributes = getRequest().getAttributes()

		new StringRepresentation(this.mapper.writeValueAsString(attributes),MediaType.APPLICATION_JSON)
	}
}
