package org.glue.unit.exec.impl

import org.glue.unit.exec.GlueState;
import static groovyx.gpars.dataflow.DataFlow.start
import groovyx.gpars.actor.Actor
import groovyx.gpars.dataflow.DataFlowActorGroup

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

import org.apache.log4j.Logger
import org.glue.unit.exceptions.DependencyFailedException
import org.glue.unit.exec.ExecNode
import org.glue.unit.exec.GlueState
import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.Provider
import org.glue.unit.status.UnitStatus

/**
 *
 * Uses the GParallizer API to submit process execution using the actor based framework.<br/>
 * A new instance must be created for each GlueUnit execution.<br/>
 * Each instance share's the same execution thread pool, to allow any number of instances to run<br/>
 * without running out of resources.<br/>
 * <p/>
 * <b>Error Handling<b/><br/>
 * The map variable errors stores all errors during the process execution.<br/>
 * Errors are added to this map and execution halted when:<br/>
 * <ul>
 *  <li>The ProcessExecutor throws an Exception</li>
 *  <li>The ProcessExecutor status == GlueStatus.FAILED, if its error field is null a default exception is created.</li>
 *  <li>Any other error caught during process execution</li>
 * </ul>
 *
 */
@Typed(TypePolicy.DYNAMIC)
class GParallizerUnitExecutor implements UnitExecutor{

	static final Logger log = Logger.getLogger(GParallizerUnitExecutor.class)

	GlueUnit unit
	GlueContext context

	final Map<String, ProcessExecutor> processExecutors = new ConcurrentHashMap<String, ProcessExecutor>()

	final DataFlowActorGroup group

	UnitStatus unitStatus = new UnitStatus(status:GlueState.WAITING)

	/**
	 * Saves the errors during process execution.
	 * Key == process name
	 * Value == Throwable
	 */
	final Map<String, Throwable> errors = new ConcurrentHashMap<String, Throwable>()

	Provider<ProcessExecutor> processExecutorProvider

	Actor masterActor
	final List<Actor> actors = []

	public GParallizerUnitExecutor(Provider<ProcessExecutor> processExecutorProvider){
		this.processExecutorProvider = processExecutorProvider
		group = new DataFlowActorGroup(5)
	}

	public GParallizerUnitExecutor(Provider<ProcessExecutor> processExecutorProvider, DataFlowActorGroup group){
		this.processExecutorProvider = processExecutorProvider
		this.group = group
	}


	GlueState getStatus(){
		unitStatus.status
	}

	Date getStartDate(){
		unitStatus.getStartDate()
	}

	Date getEndDate(){
		unitStatus.getEndDate()
	}

	double getProgress(){
		unitStatus.getProgress()
	}


	void init(GlueUnit unit, GlueContext context){

		this.unit = unit
		this.context = context
		unitStatus.unitId = context.unitId
		unitStatus.name = unit.name

		//start by setting the unit status to waiting
		unitStatus.startDate = new Date()
		context.statusManager?.setUnitStatus(unitStatus)

		//we create the processExecutors map here.
		unit?.processes?.each {String name, GlueProcess process ->

			def processExecutor = processExecutorProvider.get()
			processExecutor.init(process, context)
			processExecutors[name] = processExecutor
		}
	}

	
	void terminate(){


		//kill all actors
		actors.each { Actor act ->
			try{
				act.terminate()
			}catch(Throwable t){
				println "[IGNORE] error killing actor: $t"
			}
		}
		try{
			masterActor.terminate()
		}catch(Throwable t){
			println "[IGNORE] error killing actor: $t"
		}

		try{
			context.destroy()
		}catch(Throwable t){
			log.error(t.toString(), t)
		}

		unitStatus.status = GlueState.KILLED
		context.statusManager?.setUnitStatus unitStatus
	}

	/**
	 * Waits for all process execution to complete
	 */
	void waitFor(){
		masterActor?.join()
	}

	/**
	 * Waits for the specified amount of time before time out
	 */
	void waitFor(long milliseconds, java.util.concurrent.TimeUnit timeUnit){
		masterActor?.join(milliseconds, timeUnit)
	}

	/**
	 * Runs all processes in the GlueUnit
	 * @param afterCompleteClosure Closure
	 */
	void execute(Closure afterCompleteClosure = null){
		unitStatus.startDate = new Date()
		unitStatus.endDate = startDate

		unitStatus.status=GlueState.RUNNING;

		context.getModuleFactory()?.onUnitStart unit, context
		context.statusManager?.setUnitStatus unitStatus

		//build execution graph
		final Map<String, ExecNode> execGraph = ExecutionGraphBuilder.getInstance().build(unit)

		//calculate the progress increment.
		final double progressIncrement = 100.0D/execGraph.size()


		//we iterate through each execution node in the graph and execution the glue process
		execGraph.each { final String name, final ExecNode execNode ->
            
			
			actors << group.actor {

				//--------------- Wait for parents
				execNode.waitForParents()

				//check for errors in parents
				final AtomicBoolean hasErrors = new AtomicBoolean(false)
				execNode?.parents?.each { ExecNode parent ->
					if(parent.hasError()){
						hasErrors.set(true)
					}
				}

				//we end execution here if any error was found above
				if(hasErrors.get()){
					execNode.setDone(new DependencyFailedException())
					return
				}

				//--------------- End Wait for parents
				//-------------- Process Execution
				
				//here we set the current process name to a ThreadLocal variable.
				//this will notify the GlueExecLogger of the current thread's process name.
				context.getLogger()?.setCurrentThreadProcessName(name)
				
				Throwable error = null
				final ProcessExecutor processExecutor = processExecutors[name]
				try{

					if(!processExecutor){
						throw new NullPointerException("Internal errors process executor cannot be null for $name")
					}

					//run process execution
					println "Running $name"
					processExecutor.execute()


				}catch(Throwable t){
					error = t
				}finally{
					unitStatus.progress += progressIncrement

					//errors are to propagated from the processExecutor to this class

					if(processExecutor.state == GlueState.FAILED){
						error = (processExecutor.error) ? processExecutor.error : new RuntimeException("Uknown Exception process $name failed")
					}

					if(error){
						errors[name] = error
					}

					unitStatus.endDate = new Date()

					context.statusManager?.setUnitStatus unitStatus

					//no execution after this method
					execNode.setDone(error)
					context.getLogger()?.setCurrentThreadProcessName(null)
				}
				return
			} //eof start

		
		}//eof execGraph.each

		masterActor = start{
			//wait for all actors to complete
			//wait for all actors to complete execution
			actors.each { l -> l.join() }

			unitStatus.progress = 1D
			unitStatus.endDate = new Date()

			//if any error was found print them out here
			if(errors){


				errors.each { String name, Throwable t ->
					log.error(name, t)
					//we add print lines here to ensure data is captured
					//by glue log modules
					println "error: $name $t"
					t?.printStackTrace()
				}

				//update status
				unitStatus.status = GlueState.FAILED
				context.statusManager?.setUnitStatus unitStatus
				//notify modules of unit failure
				context.getModuleFactory()?.onUnitFail unit, context


			}else{

				unitStatus.status = GlueState.FINISHED
				context.statusManager?.setUnitStatus unitStatus

				//notify modules of unit complete
				context.getModuleFactory()?.onUnitFinish unit, context
			}

			//call the after complete closure
			afterCompleteClosure?.call()

		}
	}
}
