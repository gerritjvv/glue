package org.glue.unit.exec.impl

import java.nio.channels.FileChannel
import java.nio.channels.FileLock

import org.apache.log4j.Logger
import org.glue.unit.exceptions.ProcessStopException
import org.glue.unit.exceptions.UnitSubmissionException
import org.glue.unit.exec.GlueState
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.exec.impl.runner.WorkflowRunnerBootstrap
import org.glue.unit.log.impl.DistributedOutputStream
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitValidator
import org.glue.unit.om.Provider
import org.glue.unit.repo.GlueUnitRepository
import org.streams.commons.zookeeper.ZGroup
import org.streams.commons.zookeeper.ZLock


/**
 * 
 * Runs a single workflow. 
 * Represents a workflow process.
 */
class WorkflowRunner {

	private static final Logger LOG = Logger.getLogger(WorkflowRunner)

	//used for testingjvms
	static Throwable exception
	static boolean testMode = false

	//used for serial execution
	static RandomAccessFile file = null;
	static FileChannel f = null;
	static FileLock lock = null;
	static ZGroup zgroup = null;

	static main(args) {
		WorkflowRunnerBootstrap di
		try{
			//Parse command line arguments
			def config = WorkflowRunnerConfig.getInstance(args)
			//start DI Framework
			di = new WorkflowRunnerBootstrap(config)


			GlueModuleFactoryProvider moduleFactoryProvider = di.getBean("moduleFactoryProvider")
			moduleFactoryProvider.get(null)?.availableModules?.each{ println "Module: ${it}" }
			run(config, di)


		}catch(Throwable t){
			WorkflowRunner.exception = t
			LOG.error(t.toString(), t)
			if(!testMode){
				System.exit(-1)
			}
		}

		di?.shutdown()


		if(!testMode){
			System.exit(0)
		}
	}

	/**
	 * Tries to create a lock on the file name == glue.unitName
	 * If none can be obtained throw a ProcessStopException
	 * @param unit
	 * @return
	 */
	private static final trySerialLock(GlueUnit unit){

		String tmpdir = System.getProperty("java.io.tmpdir");
		File lockfile = new File(tmpdir, unit.getName());
		file = new RandomAccessFile(lockfile, "rw");
		f = file.getChannel();

		lock = f.tryLock();
		if(lock == null){
			throw new ProcessStopException("Another instance of " + unit.getName() + " is already running");
		}

		lockfile.deleteOnExit();
	}

	private static final trySerialLock(String unitName, ZLock zlock){
		if(!zlock.lock(unitName)){
			throw new ProcessStopException("Another instance of " + unitName + " is already running");
		}
	}

	private static final tryReleaseLock(ZLock zlock, String unitName){
		zlock.unlock unitName
	}

	/**
	 * If a lock exists the lock is released.
	 * @return
	 */
	private static final tryReleaseLock(){

		//if any lock was held destroy
		if (lock != null && lock.isValid())
			lock.release();
		if (file != null)
			file.close();

	}

	/**
	 * Run the workflow and does not exit until the workflow has completed
	 * @param config WorkflowRunnerConfig
	 * @param di WorkflowRunnerBootstrap
	 */
	static void run(WorkflowRunnerConfig config, WorkflowRunnerBootstrap di){

		ZLock zlock =  di.getBean(ZLock);
		String unitId=config.uuid
		GlueUnitRepository repo = di.getBean(GlueUnitRepository)

		GlueUnit unit = repo.find(config.workflow)


		def out = System.out
		def err = System.err
		GlueContext context

		try{

			if(unit == null){
				throw new UnitSubmissionException("Error running ${config.workflow} was not found")
			}

			//if the glue workflow is serial we use a file lock implementation
			//to ensure the workflow is running without another instance running
			if(unit.isSerial()){
				if(zlock){
					trySerialLock(unit.name, zlock)
				}else{
					trySerialLock(unit);
				}
			}

			//Build a context for the current execution
			GlueContextBuilder contextBuilder = di.getBean(GlueContextBuilder)

			GlueUnitValidator unitValidator = di.getBean(GlueUnitValidator)
			Provider<UnitExecutor> unitExecutorProvider = di.getBean('unitExecutorProvider')

			context = contextBuilder.build(unitId, unit, config.params)

			if(context?.logger){
				//create an output stream that uses the GlueContext's GlueExecLogger
				def pout = new PrintStream(new DistributedOutputStream(context.logger));
				System.setOut(pout)
				System.setErr(pout)
			}

			UnitExecutor exec = unitExecutorProvider.get()

			//validate that we can execute the
			//at this point a GlueUnitValidationException will be thrown if the
			//glue unit cannot run

			unitValidator?.validate unit, context
			exec.init unit, context

			//start and wait for execution completion
			exec.execute()
			exec.waitFor()



			println "Execute status: ${exec.status}"
			//check that the status is failed
			if( GlueState.FAILED == exec.status){
				throw new UnitSubmissionException("Error running ${config.workflow} ${exec.getErrors()}")
			}
		}
		catch(UnitSubmissionException t) {
			throw t
		}catch(Throwable t) {
			println "Error ==>" + config.workflow
			throw new UnitSubmissionException("Error while parsing $config".toString(), t)
		}finally{

			context?.destroy()
			System.setOut(out)
			System.setErr(err)

			if(zlock && unit){
				trySerialLock(unit.name, zlock)
			}else{
				tryReleaseLock();
			}

		}
		return
	}
}
