package org.glue.rest.resources;

import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper
import org.glue.unit.exec.GlueState
import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
import org.glue.unit.status.ProcessStatus
import org.glue.unit.status.UnitStatus
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Get
import org.restlet.resource.ServerResource

/**
 * Gets the status of a specified process inside a GlueUnit
 */
class ProcessStatusResource extends ServerResource {

	private static final Logger LOG = Logger.getLogger(ProcessStatusResource.class)
	static final ObjectMapper mapper = new ObjectMapper()

	/**
	 * Gives information of the actual execution
	 */
	GlueUnitStatusManager unitStatusManager
	
	/**
	 * Gives info on modules.
	 */
	GlueModuleFactoryProvider moduleFactoryProvider;
	/**
	 * Gives the process definition information e.g. for dependencies
	 */
	GlueUnitRepository repo
	
	GlueExecLoggerProvider glueExecLoggerProvider

	public ProcessStatusResource(GlueUnitStatusManager unitStatusManager,
	GlueUnitRepository repo, GlueModuleFactoryProvider moduleFactoryProvider,
	GlueExecLoggerProvider glueExecLoggerProvider) {
		super();
		this.unitStatusManager = unitStatusManager
		this.repo = repo
		this.moduleFactoryProvider=moduleFactoryProvider
		this.glueExecLoggerProvider = glueExecLoggerProvider
	}

	/**
	 * Returns the log data for the unitId and processName
	 * This method will never throw an exception.
	 * @param unitId
	 * @param processName 
	 * @param maxLines
	 * @return String
	 */
	protected String getLogOutput(String unitId, String processName, int maxLines){
		
		def execLogger = glueExecLoggerProvider.get(unitId)
		def text
		try{
			
			 text = execLogger.tailLog(processName, maxLines)
				
		}catch(t){
			LOG.error(t.toString(), t)
			text = "Error in retreiving log data for $processName {$t} "
		}
		
		return text
	}
	
	@Get("json")
	public Representation represent(Representation entity) {
		String unitId=(String) getRequest().getAttributes().get("unitId");
		String processName=(String) getRequest().getAttributes().get("processName");

		if(!processName){
			processName = "main"
		}
		
		Representation rep;
		GlueModuleFactory moduleFactory;
		try{

			UnitStatus unitStatus = unitStatusManager.getUnitStatus(unitId)
			GlueUnit unit
			GlueProcess glueProcess
			

			//---------------- Validation --------------------//
			boolean notFound = false
			String msg = ""

			if(!(unitStatus && (unit = repo.find(unitStatus.name))) ){
				msg = "The Unit was not found $unitId ${unitStatus?.name}"
				notFound = true
			}else{

				//check that process exists
				glueProcess = unit.processes?.get(processName)

				if(!glueProcess){
					msg = "The process $processName for unit $unitId ${unitStatus.name} was not found"
					notFound = true
				}
			}

			//---------------- END Validation --------------------//
			
			def out

			if(!notFound){
				//note that we do allow for processStatus to be null
				//this may happen if the glue unit was not yet started
				//to the ui we report this as waiting status with zero progress
				ProcessStatus processStatus = unitStatusManager.getProcessStatus(unitId, processName)
				
				
				if(!processStatus){
					processStatus = new ProcessStatus(
							unitId:unitId,
							processName:processName,
							startDate:new Date(),
							endDate: new Date(),
							status:GlueState.WAITING,
							error:'',
							progress:0D
							)
				}

				out = processStatus.toMap()
				out.dependencies = glueProcess.dependencies
				out.output=getLogOutput(unitId, processName, 2000)
				
				setStatus(Status.SUCCESS_CREATED);
			}else{
				//the unit was not found
				setStatus(Status.CLIENT_ERROR_NOT_FOUND)
				out = [unitId:unitId, processName:processName, error:msg]
			}

			rep = new StringRepresentation(this.mapper.writeValueAsString(out),
					MediaType.APPLICATION_JSON );
		}
		catch(Throwable t) {
			LOG.error(t.toString(), t)

			def out=[:]
			out.error=t
			setStatus(Status.SERVER_ERROR_INTERNAL)
			rep = new StringRepresentation(this.mapper.writeValueAsString(out),MediaType.APPLICATION_JSON)
		}

		return rep;
	}
}
