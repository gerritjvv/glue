package org.glue.unit.exec.impl

import org.glue.unit.om.GlueContext;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

import org.apache.log4j.Logger
import org.glue.unit.exceptions.UnitSubmissionException
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.GlueState
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.om.GlueUnitValidator
import org.glue.unit.om.Provider
import org.glue.unit.repo.GlueUnitRepository

/**
 * The GlueExecutor is the single point of entry into the submitting, executing and monitoring GlueUnit(s).<br/>
 *
 */
@Typed
class GlueExecutorImpl implements GlueExecutor{


	static final Logger log = Logger.getLogger(GlueExecutorImpl.class)

	/**
	 * Lists the GlueUnit(s) that are actively in execution.
	 */
	final Map<String,UnitExecutor> unitList = new ConcurrentHashMap<String, UnitExecutor>()

	final Map<String,GlueContext> unitContextList = new ConcurrentHashMap<String, GlueContext>()

	/**
	 * Executor config
	 */
	ConfigObject config
	/**
	 * Atomic variable to indicate shutdown
	 */
	AtomicBoolean isUnderShutdown = new AtomicBoolean(false)

	CountDownLatch shutdownLatch = new CountDownLatch(1)

	/**
	 * Abstract concept to how and where the GlueUnit definitions are stored.
	 */
	GlueUnitRepository glueUnitRepository

	GlueContextBuilder contextBuilder
	Provider<UnitExecutor> unitExecutorProvider
	GlueUnitBuilder glueUnitBuilder

	GlueUnitValidator unitValidator

	/**
	 * @param executorConfig 
	 * @param contextBuilder Builds GlueContext instances
	 * @param unitExecutorProvider create instances of the UnitExecutor
	 * @param glueUnitBuilder Build instances of GlueUnit
	 * @param unitValidator GlueUnitValidator used to validate the GlueUnit before execution, can be null
	 */
	public GlueExecutorImpl(ConfigObject executorConfig, GlueUnitRepository glueUnitRepository = null,
	GlueContextBuilder contextBuilder,
	Provider<UnitExecutor> unitExecutorProvider,
	GlueUnitBuilder glueUnitBuilder,
	GlueUnitValidator unitValidator = null) {

		this.config = executorConfig
		this.glueUnitRepository = glueUnitRepository
		this.contextBuilder = contextBuilder
		this.unitExecutorProvider = unitExecutorProvider
		this.glueUnitBuilder = glueUnitBuilder
		this.unitValidator = unitValidator
	}

	void terminate(String unitId){
		unitList[unitId]?.terminate()
		unitList.remove unitId
		unitContextList.remove unitId
	}


	GlueState getStatus(String unitId){
		GlueState status = unitList?.unitId?.status
		if(!status){
			status = GlueState.FINISHED
		}
		return status
	}

	/**
	 * Returns the glue context
	 * @param unitId
	 * @return
	 */
	GlueContext getContext(String unitId){
		unitContextList[unitId]
	}


	double getProgress(String unitId){
		(unitList[unitId]) ? unitList?.unitId?.progress : 1D
	}

	void waitFor(String unitId){
		unitList[unitId]?.waitFor()
	}

	void waitFor(String unitId, long time, java.util.concurrent.TimeUnit timeUnit){
		unitList[unitId]?.waitFor(time, timeUnit)
	}

	/**
	 * Notifies the execution threads to complete and shutdown.
	 * This method does not wait for shutdown and returns.
	 */
	public void shutdown(){
		//do shutdown
		isUnderShutdown.set true;
		shutdownLatch.countDown()

	}

	/**
	 * Start shutdown procedure and wait for all GlueUnits to complete processing
	 */
	public void waitUntillShutdown(){
		println "Waiting for shutdown"
		shutdownLatch.await()

		if(isUnderShutdown.get()){
			unitList.each{String name, UnitExecutor exec ->

				exec.waitFor()
			}
		}
		
		println "End Waiting for shutdown"
	}

	/**
	 * Submits the GlueUnit based on its logical name, e.g. the file name used to write the glue unit.<br/>
	 *
	 */
	@Override
	public String submitUnitAsName(String unitName,Map<String,String> params, String unitId = null)
	throws UnitSubmissionException {
		String unitFileName=unitName
		if(isUnderShutdown.get()) {
			throw new UnitSubmissionException("Cannot accept jobs, server is scheduled for shutdown")
		}

		if(glueUnitRepository == null){
			throw new UnitSubmissionException("No repository was defined for this GlueExecutor therefore GlueUnit(s) cannot be submit by name ")
		}

		if(!unitId){
			//create a unique id that will represent this GlueUnit instance's execution
			unitId=java.util.UUID.randomUUID().toString();
		}
		
		//query the GlueUnitRepository for the unit
		GlueUnit unit = glueUnitRepository.find( unitName )

		if(!unit) {
			throw new UnitSubmissionException("Could not find $unitName in repository " + glueUnitRepository)
		}

		return this.submit(unit, params)
	}

	/**
	* Helper method that submits the GlueUnit as a ConfigObject.<br/>
	* Note: This will not retrieve the unit from the UnitRepository.
	*/
   @Override
   public String submitUnitAsConfig(ConfigObject config,Map<String,String> params)
   throws UnitSubmissionException {
	   GlueUnit unit = glueUnitBuilder.build(config)
	   submit(unit, params)
   }

   
	/**
	 * Helper method that submits the GlueUnit as a text String.<br/>
	 * Note: This will not retrieve the unit from the UnitRepository.
	 */
	@Override
	public String submitUnitAsText(String unitText,Map<String,String> params)
	throws UnitSubmissionException {
		GlueUnit unit = glueUnitBuilder.build(unitText)
		submit(unit, params)
	}

	/**
	 * Helper method that submits the GlueUnit from a URL.<br/>
	 * Note: This will not retrieve the unit from the UnitRepository.
	 */
	@Override
	public String submitUnitAsUrl(URL unitUrl, Map<String,String> params) throws UnitSubmissionException {

		GlueUnit unit = glueUnitBuilder.build(unitUrl)
		submit(unit, params)
	}


	/**
	 * All submit methods delegate work to this method for the actual execution of a GlueUnit.<br/>
	 * A GlueUnitImpl instance is created from the ConfigObject (this represents the GlueUnit).
	 * <p/>
	 * This method:<br/>
	 * <ul>
	 *  <li> Creates a GlueUnitImpl instance from the ConfigObject</li>
	 *  <li> Creates a ParallelUnitExecutor instance from the GlueUnitImpl</li>
	 *  <li> Calls the execute method on the PrallelUnitExecutor </li>
	 *  <li> On any error during the previous stages a UnitSubmissionException is thrown</li>
	 *
	 * </ul>
	 *
	 * @param config ConfigObject representing the textual form of the GlueUnit
	 * @param params Map properties for the GlueUnit
	 * @return String the GlueUnit execution UUID
	 */
	protected String submit(GlueUnit unit,Map<String,String> params)
	throws UnitSubmissionException {

		//throw an exception if the server is shutting down
		if(isUnderShutdown.get()){
			throw new UnitSubmissionException("Server is shutting down")
		}

		try{
			//create a unique id that will represent this GlueUnit instance's execution
			String unitId=java.util.UUID.randomUUID().toString();

			GlueContext context = contextBuilder.build(unitId, unit, params)

			//validate that we can execute the
			//at this point a GlueUnitValidationException will be thrown if the
			//glue unit cannot run
			unitValidator?.validate unit, context

			UnitExecutor exec = unitExecutorProvider.get()

			exec.init unit, context

			unitContextList[unitId] = context
			unitList[unitId] = exec

			//start async execute of the GlueUnit's processes
			exec.execute(
					{
						//removes the unit executor from the unit list after execution.
						unitList.remove unitId
						unitContextList.remove unitId
						//we destroy and clear the context
						context.destroy()
					}
					)

			return unitId;
		}
		catch(UnitSubmissionException t) {
			throw t
		}catch(Throwable t) {
			throw new UnitSubmissionException("Error while parsing $config".toString(), t)
		}
	}
}
