package org.glue.rest.resources;


import org.glue.unit.exec.GlueExecutor;
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
import org.glue.unit.status.ProcessStatus
import org.glue.unit.status.UnitStatus
import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Get
import org.restlet.resource.ServerResource

/**
 * Gets the status of a specified glue unit that is executing.<br/>
 * This class relies on the GlueUnitStatusManager and the GlueUnitRepository to get<br/> 
 * the glue execution information.
 */
class TerminateResource extends ServerResource {
	static final ObjectMapper mapper = new ObjectMapper()
	private static final Logger LOG = Logger.getLogger(TerminateResource.class)

GlueExecutor exec;

	
	public TerminateResource(GlueExecutor exec) {
		super();
		this.exec = exec;
	}


	@Get("json")
	public Representation getStatus(Representation entity) {
		String unitId=(String) getRequest().getAttributes().get("unitId");
		Representation rep;
		
		try{
			exec.terminate unitId
			rep = new StringRepresentation(this.mapper.writeValueAsString(['status':"OK"]),
					MediaType.APPLICATION_JSON );
		
		}
		catch(Throwable t) {
			
			def out=[:] 
			out.error=t
			out.status="error";
			
			setStatus(Status.SERVER_ERROR_INTERNAL)
			
			rep = new StringRepresentation(this.mapper.writeValueAsString(out),MediaType.APPLICATION_JSON)
		}
		finally{
			
		}
		return rep;
	}
}
