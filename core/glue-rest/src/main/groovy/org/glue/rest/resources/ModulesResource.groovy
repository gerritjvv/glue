package org.glue.rest.resources;

import org.glue.unit.exec.GlueExecutor
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueModuleFactoryProvider
import org.codehaus.jackson.map.ObjectMapper
import org.restlet.data.Form
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Get
import org.restlet.resource.ServerResource

class ModulesResource extends ServerResource {

	static final ObjectMapper mapper = new ObjectMapper()

	GlueExecutor exec
	GlueModuleFactoryProvider moduleFactoryProvider

	public ModulesResource(GlueExecutor exec,
	GlueModuleFactoryProvider moduleFactoryProvider) {
		super();
		this.exec = exec;
		this.moduleFactoryProvider = moduleFactoryProvider;
	}

	@Get("json")
	public Representation getStatus(Representation entity) {

		//		String unitId=(String) getRequest().getAttributes().get("unitId");
		//println getRequest().getAttributes()
		def out =[:]
		Representation rep;
		GlueModuleFactory moduleFactory;
		try{
			moduleFactory = moduleFactoryProvider.get()
			moduleFactory.getAvailableModules().each{ name, module ->
				out[name]=module.getInfo();
			}
			rep = new StringRepresentation(this.mapper.writeValueAsString(out),
					MediaType.APPLICATION_JSON );
		}
		catch(Throwable t) {

			out.error=t

			setStatus(Status.SERVER_ERROR_INTERNAL)

			rep = new StringRepresentation(this.mapper.writeValueAsString(out),MediaType.APPLICATION_JSON)
		}
		finally{
			moduleFactory?.destroy()
		}

		return rep;
	}
}
