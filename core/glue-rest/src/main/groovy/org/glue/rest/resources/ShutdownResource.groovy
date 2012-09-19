package org.glue.rest.resources;

import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.glue.unit.exec.GlueExecutor;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Shuts down the GlueExecutor instance passed in the constructor to this resource.
 */
class ShutdownResource extends ServerResource {

	private static final Logger LOG = Logger.getLogger(UnitStatusResource.class)
	static final ObjectMapper mapper = new ObjectMapper()

	GlueExecutor exec


	public ShutdownResource(GlueExecutor exec) {
		super();
		this.exec = exec;
	}


	@Get("json")
	public Representation represent(Representation entity) {
		Representation rep
		
		try{
			exec.shutdown();

			setStatus(Status.SUCCESS_CREATED);
			def out=[:]
			out.ok='ok';

			rep = new StringRepresentation(this.mapper.writeValueAsString(out),
					MediaType.APPLICATION_JSON );
		}catch(Throwable t){
			LOG.error(t.toString(), t)

			def out=[:]
			out.error=t
			
			setStatus(Status.SERVER_ERROR_INTERNAL)
			rep = new StringRepresentation(this.mapper.writeValueAsString(out),MediaType.APPLICATION_JSON)
		}


		return rep;
	}
}
