package org.glue.unit.exec.impl.queue

import groovy.util.ConfigObject

import java.net.URL
import java.util.List;
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import org.apache.log4j.Logger
import org.glue.unit.exceptions.UnitSubmissionException
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.GlueState
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.exec.impl.WorkflowRunner
import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.process.DefaultJavaProcessProvider
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.status.UnitStatus
import org.glue.unit.exec.WorkflowsStatus

/**
 * 
 * Implements the GlueExecutor and provides a queue to in which all workflows will be executed.
 *
 */
@Typed
class QueuedExecServiceImpl implements GlueExecutor, WorkflowsStatus{


	static final Logger log = Logger.getLogger(QueuedExecServiceImpl.class)

	/**
	 * Lists the GlueUnit(s) that are actively in execution.
	 */
	final Map<String, GlueContext> executingUnits = new ConcurrentHashMap<String, GlueContext>()

	/**
	 * only used for testing
	 */
	Map<String, Throwable> errors = new ConcurrentHashMap<String, Throwable>()
	
	
	/**
	 * key = name
	 */
	final ConcurrentHashMap<String, GlueContext> runningWorkflows = new ConcurrentHashMap<String, GlueContext>()
	
	/**
	 * Set to true for errors to be logged
	 */
	boolean retainErrors = false

	/**
	 * Atomic variable to indicate shutdown
	 */
	AtomicBoolean isUnderShutdown = new AtomicBoolean(false)

	CountDownLatch shutdownLatch = new CountDownLatch(1)

	/**
	 * Abstract concept to how and where the GlueUnit definitions are stored.
	 */
	GlueUnitRepository glueUnitRepository

	GlueUnitBuilder glueUnitBuilder

	/**
	 * Actor from which each glue workflow is exected as a separate java process
	 */
	WorkflowExecActor execActor

	int maxGlueProcesses

	/**
	 * Injected. Allows this class to generate a context for the workflow before execution.
	 * The workflow will get executed in another java process with its own context.
	 */
	GlueContextBuilder contextBuilder;
	
	ConfigObject config;
	
	/**
	 * 
	 * @param maxGlueProcesses the max number of glue processes allowed to run at any time
	 * @param glueUnitRepository
	 * @param glueUnitBuilder
	 */
	@Typed(TypePolicy.MIXED)
	public QueuedExecServiceImpl(int maxGlueProcesses, Map<String, String> config, Collection<String> javaOpts, Collection<String> classPath, GlueUnitRepository glueUnitRepository,
	GlueUnitBuilder glueUnitBuilder,
	String execConf,
	String moduleConf,
	GlueExecLoggerProvider logProvider = null, GlueContextBuilder contextBuilder = null) {
		this.maxGlueProcesses = maxGlueProcesses
		this.glueUnitRepository = glueUnitRepository
		this.glueUnitBuilder = glueUnitBuilder
		this.contextBuilder = contextBuilder

		//create the default java process provider
		DefaultJavaProcessProvider provider = new DefaultJavaProcessProvider(config)

		if(classPath){
			classPath.each{ provider.classpath << it }
		}else{
			provider.addCurrentClassPath()
		}

		javaOpts.each { provider.javaOpts << it }

		provider.mainClass = WorkflowRunner.class.name

		//create the WorkflowExecActor that will control the glue workflow process execution
		//each process launches an instance of the Workflowrunner
		execActor = new WorkflowExecActor(
				maxGlueProcesses,
				provider,
				execConf,
				moduleConf,
				logProvider
				)

		def closure = { executingUnits.remove(it.uuid); runningWorkflows.remove(it.name); }
		execActor.onErrorListener = { wf, t ->
			runningWorkflows.remove(wf.name)
			executingUnits.remove(wf.uuid)
			
			if(retainErrors){
				errors[wf.uuid] = t
			}
		}
		execActor.onExecCompletedListener = closure

		execActor.start()

	}
	
	void terminate(String unitId){
		execActor.killProcess(unitId)
	}


	List<GlueContext> runningWorkflows(){
		executingUnits.values().collect { it } as List
	}
	
	Set<String> queuedWorkflows(){
		executingUnits.values().findAll { GlueContext ctx -> !execActor?.isWorkflowExecuting(ctx?.unit?.name) }.collect { GlueContext ctx -> ctx.unit.name } as Set
	}
	
	
	GlueState getStatus(String unitId){
		//via this method we cannot find more information other than running of finished
		GlueContext ctx = executingUnits[unitId]
		GlueState state = ctx?.statusManager?.getUnitStatus(unitId)?.status

		return (state)?  state: GlueState.FINISHED
	}
	
	public Map<String, UnitExecutor> getUnitList() { [:] }

	/**
	 * Returns the glue context
	 * @param unitId
	 * @return
	 */
	GlueContext getContext(String unitId){
		executingUnits[unitId]
	}


	double getProgress(String unitId){
		(executingUnits[unitId]) ? 0.5D : 1D
	}

	void waitFor(String unitId){
		execActor.waitForProcess(unitId)
	}

	void waitFor(String unitId, long time, java.util.concurrent.TimeUnit timeUnit){
		execActor.waitForProcess(unitId)
	}

	/**
	 * Notifies the execution threads to complete and shutdown.
	 * This method does not wait for shutdown and returns.
	 */
	public void shutdown(){
		//do shutdown
		isUnderShutdown.set true;
		shutdownLatch.countDown()
		execActor.stop()
	}

	/**
	 * Start shutdown procedure and wait for all GlueUnits to complete processing
	 */
	public void waitUntillShutdown(){
		println "Waiting for shutdown"
		shutdownLatch.await()
		execActor.awaitTermination(10, TimeUnit.SECONDS)
		println "End Waiting for shutdown"
	}

	/**
	 * Submits the GlueUnit based on its logical name, e.g. the file name used to write the glue unit.<br/>
	 *
	 */
	@Override
	public String submitUnitAsName(String unitName,Map<String,Object> params, String unitId = null)
	throws UnitSubmissionException {
		String unitFileName=unitName
		if(isUnderShutdown.get()) {
			throw new UnitSubmissionException("Cannot accept jobs, server is scheduled for shutdown")
		}

		if(glueUnitRepository == null){
			throw new UnitSubmissionException("No repository was defined for this GlueExecutor therefore GlueUnit(s) cannot be submit by name ")
		}

		//query the GlueUnitRepository for the unit
		GlueUnit unit = glueUnitRepository.find( unitName )

		if(!unit) {
			throw new UnitSubmissionException("Could not find $unitName in repository " + glueUnitRepository)
		}


		return this.submit(unit, params, unitId)
	}

	/**
	 * Helper method that submits the GlueUnit as a ConfigObject.<br/>
	 * Note: This will not retrieve the unit from the UnitRepository.
	 */
	@Override
	public String submitUnitAsConfig(ConfigObject config,Map<String,Object> params)
	throws UnitSubmissionException {
		GlueUnit unit = glueUnitBuilder.build(config)
		submit(unit, params)
	}


	/**
	 * Helper method that submits the GlueUnit as a text String.<br/>
	 * Note: This will not retrieve the unit from the UnitRepository.
	 */
	@Override
	public String submitUnitAsText(String unitText,Map<String,Object> params)
	throws UnitSubmissionException {
		GlueUnit unit = glueUnitBuilder.build(unitText)
		submit(unit, params)
	}

	/**
	 * Helper method that submits the GlueUnit from a URL.<br/>
	 * Note: This will not retrieve the unit from the UnitRepository.
	 */
	@Override
	public String submitUnitAsUrl(URL unitUrl, Map<String,Object> params) throws UnitSubmissionException {
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
	 * ThreadSafety.
	 * This method needs to be synchronized and kept thread safe.
	 * Decisions like should the work flow run when serial etc needs to be done in a serial fashion. 
	 *
	 * @param config ConfigObject representing the textual form of the GlueUnit
	 * @param params Map properties for the GlueUnit
	 * @return String the GlueUnit execution UUID
	 */
	protected synchronized String submit(GlueUnit unit,Map<String,Object> params, String unitId = null)
	throws UnitSubmissionException {

		//throw an exception if the server is shutting down
		if(isUnderShutdown.get()){
			throw new UnitSubmissionException("Server is shutting down")
		}

		if(unit.isSerial() && execActor.isWorkflowExecuting(unit.name)){
			//reject execution and leave no trace except for a 0 uid
			println "Rejecting execution request. Workflow ${unit.name} has been marked as serial and is still running"
			return "0";
		}

		if(!unitId){
			//create a unique id that will represent this GlueUnit instance's execution
			unitId=java.util.UUID.randomUUID().toString();
		}
		
		def qw = new QueuedWorkflow(unit.name, unitId, params, unit.priority);
		def context = contextBuilder.build(unitId, unit, params)
		
		if(!execActor.canAdd(qw) || runningWorkflows.putIfAbsent(unit.name, context) != null) {
			println "Workflow ${unit.name} already in queue to run"
			return "0";
		}
		
		//set an initial status
		UnitStatus unitStatus = new UnitStatus(status:GlueState.WAITING)
		unitStatus.unitId = unitId
		unitStatus.name = unit.name
		//start by setting the unit status to waiting
		unitStatus.startDate = new Date()
		context.statusManager?.setUnitStatus(unitStatus)
		context.logger?.out unitId

		try{

			//if the workflow is a trigger workflow any files needs to be set here.
			executingUnits[unitId] = context
			
			execActor.add(qw)
			
			
			return unitId;
		}
		catch(UnitSubmissionException t) {
			throw t
		}catch(Throwable t) {

			unitStatus.status = GlueState.FAILED
			context.statusManager?.setUnitStatus(unitStatus)

			throw new UnitSubmissionException("Error while parsing ${unit?.name}", t)
		}
	}
}