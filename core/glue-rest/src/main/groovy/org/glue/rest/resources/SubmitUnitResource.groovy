package org.glue.rest.resources;

import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper
import org.glue.rest.job.JobSubmit
import org.glue.rest.job.JobSubmitResponse
import org.glue.unit.exec.GlueExecutor
import org.restlet.data.Status
import org.restlet.representation.Representation
import org.restlet.resource.Post
import org.restlet.resource.ServerResource


/**
 * Calls the Glue engine to execute a glue work flow.<br/>
 * Note that the work flow must already be installed i.e.<br/>
 * the glue unit repository must be able to see it.
 */
class SubmitUnitResource extends ServerResource {

	private static final Logger LOG = Logger.getLogger(UnitStatusResource.class)
	static final ObjectMapper mapper = new ObjectMapper()

	GlueExecutor exec


	public SubmitUnitResource(GlueExecutor exec) {
		super();
		this.exec = exec;
	}

	@Post("json")
	public JobSubmitResponse submit(JobSubmit jobSubmit) {

		
		String unitName = jobSubmit.unitName
		Map<String,String> params = jobSubmit.params
		
		if(!unitName){
			setStatus Status.CLIENT_ERROR_BAD_REQUEST, "Client must specify a unit name"
			return null
		}
		
		Representation rep
		try{
			def out =[:]

			String unitId = exec.submitUnitAsName(unitName, params)
			setStatus(Status.SUCCESS_CREATED);
			
			return new JobSubmitResponse(unitName:unitName, unitId:unitId, createTime:new Date())
		}
		catch(Throwable t) {
			LOG.error(t.toString(), t)

			setStatus Status.SERVER_ERROR_INTERNAL, t, t.toString()
			return null
		}
	}
}
