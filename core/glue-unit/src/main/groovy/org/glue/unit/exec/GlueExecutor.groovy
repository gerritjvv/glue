package org.glue.unit.exec;

import java.net.URL
import java.util.Map

import org.glue.unit.exceptions.UnitSubmissionException
import org.glue.unit.om.GlueContext

/**
 * 
 * A GlueExecutor process and runs a whole GlueUnit ( a glue definition of
 * processes )<br/>
 * The GlueExecutor is the master job sumission and execution controll,
 * delagating the actual execution of a submitted GlueUnit to a UnitExecutor.
 * <br/>
 * The GlueExecutor implementations are all Thread Safe.
 * 
 */
@Typed
public interface GlueExecutor {
	/**
	 * Submits a GlueUnit represented by the unitText field.
	 * 
	 * @param unitText
	 *            String the glue unit as a String
	 * @param params
	 *            properties Map
	 * @return String The Glue UUID
	 * @throws UnitSubmissionException
	 */
	public String submitUnitAsText(String unitText, Map<String, String> params)
			throws UnitSubmissionException;

	/**
	 * Submits the GlueUnit found at the URL location.
	 * 
	 * @param unitUrl
	 *            The url pointing to a GlueUnit
	 * @param params
	 *            properties Map
	 * @return String the Glue UUID
	 * @throws UnitSubmissionException
	 */
	public String submitUnitAsUrl(URL unitUrl, Map<String, String> params)
			throws UnitSubmissionException;

	/**
	 * Submits the GlueUnit identified by the unitName parameter. This is used
	 * when all GlueUnits are contained within a repository.<br/>
	 * The most simplest implementation of this being that all GlueUnit(s) are
	 * stored in a single folder.<br/>
	 * e.g.<br/>
	 * myglue-repo/myprocess.groovy <br/>
	 * If the unitName is 'myprocess' the code will detect and run the
	 * myprocess.groovy
	 * 
	 * @param unitName
	 * @param params
	 *            properties Map
	 * @return String the Glue UUID
	 * @throws UnitSubmissionException
	 */
	public String submitUnitAsName(String unitName, Map<String, String> params)
			throws UnitSubmissionException;

	public String submitUnitAsName(String unitName, Map<String, String> params, String unitId)
			throws UnitSubmissionException;

	/**
	 * Will terminate the workflow itendified by the unitId
	 * @param unitId
	 */
	public void terminate(String unitId)
	
	/**
	 * Returns a list of all the currently running GlueUnit executions in the form of a UnitExecutor per GlueUnit execution.
	 * @return
	 */
	public Map<String, UnitExecutor> getUnitList();

	/**
	 * Shutdown the execution for all GlueUnits
	 */
	public void shutdown();

	/**
	 * Wait for all current submitted GlueUnits to completed but does not accept
	 * any new submissions.
	 */
	public void waitUntillShutdown();

	/**
	 * Gets the state of a unit
	 */
	GlueState getStatus(String unitId);
	
	/**
	 * Returns the glue context
	 * @param unitId
	 * @return
	 */
	GlueContext getContext(String unitId);
	
	
	/**
	 * Gets the progress on a unit
	 * @param unitId
	 * @return
	 */
	double getProgress(String unitId);
	
	/**
	 * Waits for a unit to complete
	 */
	void waitFor(String unitId)
	
	/**
	 * Waits for a unit to complete or time out
	 */
	void waitFor(String unitId, long time, java.util.concurrent.TimeUnit timeUnit)
	
}