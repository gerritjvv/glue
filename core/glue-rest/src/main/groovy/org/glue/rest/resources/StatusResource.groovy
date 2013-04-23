package org.glue.rest.resources;

import org.glue.unit.exec.GlueExecutor
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
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
 * Reads the status streams of a glue unit
 */
class StatusResource extends ServerResource {

	private static final Logger LOG = Logger.getLogger(UnitStatusResource.class)
	static final ObjectMapper mapper = new ObjectMapper()

	/**
	 * Gives information of the actual execution
	 */
	GlueUnitStatusManager unitStatusManager

	/**
	 * Gives the process definition information e.g. for dependencies
	 */
	GlueUnitRepository repo


	public StatusResource(
			GlueUnitStatusManager unitStatusManager, GlueUnitRepository repo) {
		super();
		this.unitStatusManager = unitStatusManager;
		this.repo = repo
	}

	@Get("json")
	public Representation represent(Representation entity) {

		Representation rep
		try{

			def out=[:]
		
			Date today = new Date()
			Date days5Ago = today - 5
			
			Collection<UnitStatus> unitStatusList = unitStatusManager.findUnitStatus(days5Ago, today)
			
			unitStatusList?.each { UnitStatus unitStatus ->
				
				out[unitStatus.unitId] = unitStatus.toMap()
			}
			
			setStatus(Status.SUCCESS_CREATED);
			
			rep = new StringRepresentation(this.mapper.writeValueAsString(out),
					MediaType.APPLICATION_JSON );
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
