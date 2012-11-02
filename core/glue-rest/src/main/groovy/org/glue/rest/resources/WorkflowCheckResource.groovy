
package org.glue.rest.resources;

import org.restlet.resource.ServerResource
import java.text.SimpleDateFormat

import org.apache.log4j.Logger
import org.codehaus.groovy.runtime.TimeCategory;
import org.codehaus.jackson.map.ObjectMapper
import org.glue.unit.exec.GlueState;
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
import org.glue.unit.status.UnitStatus
import org.restlet.data.Status
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Get
import org.restlet.resource.ServerResource

/**
 * Returns the run history of a workflow
 */
class WorkflowCheckResource extends ServerResource {

	private static final Logger LOG = Logger.getLogger(WorkflowHistoryResource.class)

	static final ObjectMapper mapper = new ObjectMapper()

	/**
	 * Gives information of the actual execution
	 */
	GlueUnitStatusManager unitStatusManager
	/**
	 * Gives the process definition information e.g. for dependencies
	 */
	GlueUnitRepository repo

	public WorkflowCheckResource(GlueUnitStatusManager unitStatusManager, GlueUnitRepository repo) {
		super();
		this.unitStatusManager = unitStatusManager
		this.repo = repo
	}

	@Get("plain/txt")
	public Representation getCheck(Representation entity) {
				
		String expectedRunStr = getQuery().getFirstValue("expectedRun", "30")
		int expectedRun = 30
		try{
			expectedRun = Integer.parseInt(expectedRunStr)
		}catch(Exception e){
			//ignore
		}
		
		String workflowName =  getRequest().getAttributes().get("workflowName")?.trim()
		
		Representation rep;

		try{
			
			//do an inverse sort i.e. instead of x <=> y we do y <=> x to get the most recent date first
			UnitStatus unitStatus =  unitStatusManager.getLatestUnitStatus(workflowName)
			
			def res = "0"
			use(groovy.time.TimeCategory){
				if(!repo.find(workflowName)){
					res = "3" //unknown the workflow cannot be found in the repo down or unreachable
				}else if(unitStatus == null){
					res = "1" 
				}else if(unitStatus.status == GlueState.FAILED || unitStatus.startDate < expectedRun.minutes.ago ){
					res = "2" //error 
				}
			}
			
			rep = new StringRepresentation(res);
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
