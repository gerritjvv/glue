package org.glue.modules

import groovy.util.ConfigObject

import javax.mail.*
import javax.mail.internet.*

import org.apache.log4j.Logger
import org.glue.unit.exec.GlueState
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.restlet.data.Form
import org.restlet.resource.ClientResource

/**
 *
 * This module can be used by work flows to launch and monitor work flows.
 *
 */
@Typed(TypePolicy.DYNAMIC)
class ClientModule implements GlueModule {

	private static final Logger LOG = Logger.getLogger(ClientModule)
	
	String serverUrl;


	void destroy(){
	}

	void onProcessKill(GlueProcess process, GlueContext context){}
	
	Closure getUnitUrl ={ uiUrl, unitId ->
		return "$uiUrl/$unitId";
	}
	Closure getProcessUrl ={ uiUrl, unitId, processName ->
		return "$uiUrl/$unitId/$processName";
	}

	@Override
	public Boolean canProcessRun(GlueProcess process, GlueContext context) {
		return true
	}

	@Override
	public String getName() {
		return 'client';
	}

	@Override
	public void init(ConfigObject config) {
		if(config.server)
			serverUrl=config.server as String;
	}

	@Override
	public void onUnitFail(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
	}
	@Override
	public void configure(String unitId, ConfigObject config) {
	}
	@Override
	public void onProcessFail(GlueProcess process, GlueContext context,	Throwable t) {
//		GlueUnit unit=context.unit;
//		this.mail( recipientList,
//				"[glue] Process ${process.getName()} of ${unit.getName()} (${unit.getUnitId()}) failed: reason: ${t.getMessage()}",
//				"Process ${process.getName()} of ${unit.getName()} (${unit.getUnitId()}) failed: \nreason: \n${t.getMessage()}\n$processFailMessage\n${this.getProcessUrl(uiUrl,unit.getUnitId(),process.getName())}\n${this.getStackTrace(t)}");
	}
	@Override
	public void onProcessFinish(GlueProcess process, GlueContext context) {
	}
	@Override
	public void onProcessStart(GlueProcess process, GlueContext context) {
	}

	/**
	 * Starts a work flow and only returns if successful
	 * @param unitName
	 * @param params
	 * @return boolean true if successful
	 */
	public boolean start(String unitName, params, server=null) {

		String uid = startAsync(unitName, params, server)
		return waitUntilFinish(uid, server)
	}

	/**
	 * Stars a work flow and returns, not waiting for the workflow to complete.<br/>
	 * An id is returned and this can be used to monitor the work flow's progress.
	 * @param unitName
	 * @param params
	 * @return String the unit id
	 */
	public String startAsync(String unitName, params, server=null) {
		if(!server) {
			server=serverUrl;
		}
		ClientResource resource =new ClientResource("http://$server/submit")
		final Form form = new Form();
		
		params.each {k,v ->
			form.set(k.toString(),v?.toString())
		}
		form.set 'unit', unitName

		def out= resource.post(form.getWebRepresentation(),HashMap.class);
		return out?.unitId;
	}

	/**
	 * Get status on a work flow identified by the unitId
	 * @param unitId
	 * @param server optional server parameeter.
	 * @return
	 */
	public GlueState getStatus(String unitId, server=null) {
		if(!server) {
			server=serverUrl;
		}

		LOG.info("GET: http://$server/status/$unitId")
		ClientResource statusResource =new ClientResource("http://$server/status/$unitId")

		def state = statusResource.get(HashMap.class)
		return state.status as GlueState;
	}

	/**
	 * Waits for a work flow identified by the unitId to complete.
	 * @param unitId
	 * @param server optional
	 * @return GlueState
	 */
	public boolean waitUntilFinish(String unitId, server=null) {

		if(!server){
			server = serverUrl
		}

		GlueState state = getStatus(unitId,server);
		while(state==GlueState.RUNNING || state==GlueState.WAITING) {
			Thread.sleep 10000;
			state = getStatus(unitId,server);
		}
		return (state == GlueState.FINISHED)
	}
	
	@Override
	public Map getInfo()
	{
		return [defaultUrl: this.serverUrl];
	}
}
