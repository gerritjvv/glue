
package org.glue.rest.resources;

import org.restlet.resource.ServerResource
import java.text.SimpleDateFormat

import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
import org.glue.unit.status.UnitStatus
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Get
import org.restlet.resource.ServerResource

/**
 * Returns the run history of a workflow
 */
class WorkflowHistoryResource extends ServerResource {

	private static final Logger LOG = Logger.getLogger(WorkflowHistoryResource.class)

	static final ObjectMapper mapper = new ObjectMapper()

	/**
	 * Gives information of the actual execution
	 */
	GlueUnitStatusManager unitStatusManager

	public WorkflowHistoryResource(GlueUnitStatusManager unitStatusManager) {
		super();
		this.unitStatusManager = unitStatusManager
	}

	@Get("plain/txt")
	public Representation getHistory(Representation entity) {
		String limitStr =  getQuery().getFirstValue("limit", "5")
		int limit = 5;
		try{
			limit = Integer.parseInt(limitStr)
		}catch(Exception excp){
		}
		
		String workflowName =  getRequest().getAttributes().get("workflowName")?.trim()
		
		Representation rep;
		try{

			def today = new Date()
			//do an inverse sort i.e. instead of x <=> y we do y <=> x to get the most recent date first
			Collection<UnitStatus> unitStatusList = unitStatusManager.findUnitStatus(workflowName, today - limit, today )?.sort({x, y -> y.startDate <=> x.startDate})
			
			def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
			//if any results create for each entry a line with data
			// unitId, status, startDate, endDate
			rep = new StringRepresentation(
				(unitStatusList) ?
					unitStatusList?.collect({ UnitStatus unitStatus ->
				
						unitStatus.unitId + "," + unitStatus.status + "," +	dateFormat.format(unitStatus.startDate) + "," + dateFormat.format(unitStatus.endDate)   
					
						
					}
				
					).join('\n')  
				: //if the list is empty return NoData
					"NoData"	
				);
		}
		catch(Throwable t) {
			LOG.error(t.toString(), t)

			def out=[:]
			out.error=t
			setStatus(Status.SERVER_ERROR_INTERNAL)
			rep = new StringRepresentation(this.mapper.writeValueAsString(out))
		}

		return rep;
	}

	
}
