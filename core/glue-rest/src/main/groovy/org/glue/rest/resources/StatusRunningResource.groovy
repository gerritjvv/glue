package org.glue.rest.resources;

import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Get
import org.restlet.resource.ServerResource
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.UnitExecutor

import java.util.Map


/**
 * Reads the status of already running workflows
 */
class StatusRunningResource extends ServerResource {

	private static final Logger LOG = Logger.getLogger(UnitStatusResource.class)
	static final ObjectMapper mapper = new ObjectMapper()


	GlueExecutor executor;
	

	public StatusRunningResource(
			GlueExecutor executor) {
		super();
		this.executor = executor
	}

	@Get("json")
	public Representation represent(Representation entity) {

		Representation rep
		try{

			
			Map<String, UnitExecutor> unitList = executor.getUnitList();
			
			setStatus(Status.SUCCESS_CREATED);
			
			rep = new StringRepresentation(this.mapper.writeValueAsString(
				unitList?.collectEntries { unitId, uexec ->
					[unitId:unitId, startDate:uexec?.startDate, name:uexec?.glueUnit?.name]
				}
				),
					MediaType.APPLICATION_JSON);
		}catch(Throwable t){
		
			LOG.error(t.toString(), t)
			def out=[:]
			out.error=t
			setStatus Status.SERVER_ERROR_INTERNAL, t, t.toString()

			rep = new StringRepresentation( mapper.writeValueAsString(out) )
		}

		return rep;
	}
}
