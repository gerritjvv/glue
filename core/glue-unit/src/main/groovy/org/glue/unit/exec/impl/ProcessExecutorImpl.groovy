package org.glue.unit.exec.impl;


import org.glue.unit.exec.GlueState
import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueProcess
import org.glue.unit.status.ProcessStatus
import org.apache.log4j.Logger

/**
 * The ProcessExecutor implementation.
 */
@Typed
class ProcessExecutorImpl implements ProcessExecutor {
	static final Logger log = Logger.getLogger(ProcessExecutorImpl.class)

	GlueProcess glueProcess;
	GlueContext context;

	Throwable error

	ProcessStatus status = new ProcessStatus(status:GlueState.WAITING)

	public ProcessExecutorImpl() {
		status.startDate=new Date()
		status.endDate=new Date()
	}


	void init(GlueProcess glueProcess, GlueContext context){
		this.glueProcess=glueProcess
		this.context = context

		status.unitId = context.unitId
		status.processName = glueProcess.name

		status.startDate = new Date()
		context.statusManager?.setProcessStatus status
	}

	Date getEndDate(){
		status.endDate
	}

	Date getStartDate(){
		status.startDate
	}

	@Override
	public void execute() {
		//set state to RUNNING
		status.status = GlueState.RUNNING;
		status.startDate=new Date();
		status.progress = 0.5D

		context.statusManager?.setProcessStatus status

		GlueModuleFactory glueModuleFactory = context.getModuleFactory()

		try{
			//---- NOTIFY modules onProcessStart
			//				println("Calling onProcessStarters, ${context.getModules()}")
			glueModuleFactory.onProcessStart(glueProcess, context)



			//----- RUN process
			def tasks = glueProcess.getTask(); // this method returns a Closure
			if(tasks) tasks(context ) // run the process by calling the Closure
			log.debug "${glueProcess.getName()} tasks executed"
			def success = glueProcess.getSuccess();

			//----- COMPLETED process check status
			if(success) {
				log.debug "Executing success for ${glueProcess.getName()}"
				success(context)
				println "Success!"
			}
			log.debug "${glueProcess.getName()} ran successfully"

			//set state to FINISHED
			status.status=GlueState.FINISHED;
			status.endDate=new Date();
			status.progress = 1.0D
			context.statusManager?.setProcessStatus status

			glueModuleFactory.onProcessFinish(glueProcess, context);

		}
		catch(Throwable t) {
			//---- ERROR on any error get the ERROR Closure and run.
			println t;
			t.printStackTrace();
			println "Error!"
			log.error(t)
			this.error = t

			status.status=GlueState.FAILED
			status.progress = 1.0D
			status.endDate=new Date();

			status.error = t.toString()+'\n'+t.getStackTrace().collect { "at ${it.toString()}" }.join('\n');
			//

			context.statusManager?.setProcessStatus status

			def errorClosure = glueProcess.getError();
			
			try{
				if(errorClosure) errorClosure(context,t)
			}catch(Throwable t2) {
				this.error = t2
			}finally{
				glueModuleFactory.onProcessFail(glueProcess, context, t);
			}
			
		}
		
		
		return
	}

	@Override
	public Double getProgress() {
		switch(this.getState()){
			case GlueState.WAITING: return 0.0; break;
			case GlueState.FINISHED: return 1.0; break;
			default:
				return 0.5;
		}
	}

	@Override
	public GlueState getState() {
		return status.status;
	}

	@Override
	public GlueProcess getProcess() {
		return this.glueProcess;
	}
}
