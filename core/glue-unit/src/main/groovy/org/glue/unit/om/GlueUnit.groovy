package org.glue.unit.om;


import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A glue unit contains multiple processes just as a junit test contains multiple tests.<br/>
 * <p/>
 * DeWAITING on the Executor a GlueUnit can have its processes executed either as a workflow or<br/>
 * as a simple one after the other process execution.<br/>
 *
 */
@Typed
public interface GlueUnit {

	/**
	 * If true indicates to any modules e.g. email module to notify if the workflow failed
	 * @return
	 */
	boolean isNotifyOnFail()
	/**
	 * If true indicates to any modules e.g. email module to notify if the workflow failed
	 * @param notifyOnFail
	 */
	void setNotifyOnFail(boolean notifyOnFail)
	
	String getName();
	void setName(String name);
	
	boolean isSerial();
	void setSerial(boolean serial);
	
	int getPriority();
	void setPriority(int priority);
	
	List<TriggerDef> getTriggers();
	
	Map<String,GlueProcess> getProcesses();
		
	/**
	 * Just a list of plugins that Unit requires for execution. Those plugins will be loaded into the UnitContext as properties.
	 * @return
	 */
	Set<String> getRequiredModules();
			
}
