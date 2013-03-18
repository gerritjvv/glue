
package org.glue.rest.resources;

import org.restlet.resource.ServerResource
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactory
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
class UnitStatusResource extends ServerResource {

	private static final Logger LOG = Logger.getLogger(UnitStatusResource.class)

	static final ObjectMapper mapper = new ObjectMapper()
	/**
	 * Needed for retrieving context
	 */
	GlueExecutor exec

	/**
	 * Gives information of the actual execution
	 */
	GlueUnitStatusManager unitStatusManager
	/**
	 * Gives the process definition information e.g. for dependencies
	 */
	GlueUnitRepository repo

	public UnitStatusResource(GlueExecutor exec, GlueUnitStatusManager unitStatusManager, GlueUnitRepository repo) {
		super();
		this.exec=exec;
		this.unitStatusManager = unitStatusManager
		this.repo = repo
	}


	@Get("json")
	public Representation getStatus(Representation entity) {
		String unitId=(String) getRequest().getAttributes().get("unitId");
		Representation rep;
		try{

			UnitStatus unitStatus = unitStatusManager.getUnitStatus(unitId)
			GlueUnit unit

			def out

			if(unitStatus && (unit = repo.find(unitStatus.name))){

				Map<String,GlueProcess> unitProcesses = unit?.getProcesses()

				//convert the unit status to a map
				out = unitStatus.toMap()

				def processMap=[:]
				unitStatusManager.getUnitProcesses(unitId)?.each { ProcessStatus process ->
					//we convert ProcessStatus to a map
					//and find the GlueProcess definition to add its dependencies to the map
					def op = process.toMap()
					if(!op){
						op = [:]
					}
					  
					
					op.error = (op.error!=null) ? op.error=op.error.split ('\n')[0] : ""
					LOG.info("ProcessName is "+process.processName);
					op.dependencies=unitProcesses[process.processName].getDependencies();

					processMap[process.processName] = op
				}
				out.processes = processMap
				out.context = buildContextMap(exec.getContext(unitId))


				setStatus(Status.SUCCESS_CREATED);
			}else{
				//the unit was not found
				setStatus(Status.CLIENT_ERROR_NOT_FOUND)
				out = [unitId:unitId, error:'The unit id was not found']
			}

			rep = new StringRepresentation(mapper.writeValueAsString(out),
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

	/**
	 * Takes the context and builds a save context map
	 * @param context
	 * @return
	 */
	private Map<String, Object> buildContextMap(GlueContext context){


		Map<String, Object> out = [:]

		if(context){
			out['unitId'] = context.unitId
			GlueModuleFactory moduleFactory = context?.moduleFactory
			/*def modulesMap = [:]

			moduleFactory?.getAvailableModules()?.each { String name, GlueModule module ->

				modulesMap[name] = module.getInfo()
			}

			out['modules'] = modulesMap*/
			out.args=context.args;
			context.getProperties()?.each { String name, Object value ->
				//we only add primitive types
				if(value instanceof Number || value instanceof String || value instanceof GString){
					out[name] = value
				}
			}
		}

		return out
	}
}
