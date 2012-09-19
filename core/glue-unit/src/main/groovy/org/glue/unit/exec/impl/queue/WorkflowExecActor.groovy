package org.glue.unit.exec.impl.queue

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue

import org.apache.log4j.Logger
import org.glue.unit.exec.ThreadedActor
import org.glue.unit.log.GlueExecLogger
import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.om.Provider
import org.glue.unit.process.JavaProcess

/**
 * 
 * Run each workflow as a separate java process<br/>
 * The execConf and moduleConf variables must be given a non null value.
 *
 */
@Typed
class WorkflowExecActor extends ThreadedActor<QueuedWorkflow>{

	private static final Logger LOG = Logger.getLogger(WorkflowExecActor)

	Provider<JavaProcess> provider

	String execConf
	String moduleConf

	GlueExecLoggerProvider logProvider

	//key is unit id
	Map<String, JavaProcess> executingProcesses = new ConcurrentHashMap<String, JavaProcess>()
	//key is unit id and value is name
	Map<String, String> executingWorkflowNames = new ConcurrentHashMap<String, String>()

	/**
	 * The provider must have its javaopts and classpath correctly defined.
	 * The main class is assumed to be the WorkflowRunner but is not checked.
	 * @param threads the number of threads to listen against the queue
	 * @param provider
	 * @param execConf the execution configuration expected by the WorkflowRunner
	 * @param moduleConf the module configuration expected by the WorkflowRunner
	 */
	WorkflowExecActor(int threads, Provider<JavaProcess> provider, String execConf, String moduleConf, GlueExecLoggerProvider logProvider = null){

		///Here we provide a priority blocking queue.
		//The queue uses the QueuedWorkflow priority property

		super(threads, Executors.newCachedThreadPool(),
			new PriorityBlockingQueue<QueuedWorkflow>(10, QueuedWorkflow.PRIORITY_COMPARATOR)
		)
		this.provider = provider
		this.execConf = execConf
		this.moduleConf = moduleConf
		this.logProvider = logProvider
	}

	boolean isWorkflowExecuting(String workflowName){

		synchronized (executingWorkflowNames){
			for(String v : executingWorkflowNames.values()){
				if(workflowName == v) return true
			}
		}
		
		return false;
	}

	void killProcess(String uuid){
		executingProcesses[uuid]?.kill()
	}

	void waitForProcess(String uuid){
		executingProcesses[uuid]?.waitFor()
	}

	void start(){
		if(!execConf){
			throw new RuntimeException("The execConf variable must be defined")
		}
		if(!moduleConf){
			throw new RuntimeException("The moduleConf variable must be defined")
		}

		super.start()
	}

	void react(QueuedWorkflow qwf){

		LOG.debug("running workflow ${qwf.name} ${qwf.uuid}")

		synchronized (executingWorkflowNames) {
			executingWorkflowNames[qwf.uuid] = qwf.name
		}
		

		JavaProcess process = provider.get()

		def args = [
			'-execConf',
			execConf,
			'-moduleConf',
			moduleConf
		]
		args << '-workflow'
		args << qwf.name
		args << '-uuid'
		args << qwf.uuid

		qwf.params?.each{ String key, String val ->
			args << "-D${key}=${val}"
		}

		executingProcesses[qwf.uuid] = process
		GlueExecLogger logger =  (logProvider) ? logProvider.get(qwf.uuid) : null
		try{
			
			if(logger){
				process.run(args, { logger.out("$it\n") } )
			}else{
				process.run(args)
			}
			
		}finally{
			executingProcesses.remove(qwf.uuid)
			synchronized (executingWorkflowNames){
				executingWorkflowNames.remove(qwf.uuid)
			}
			
			logger?.close()
		}
		return
	}
}
