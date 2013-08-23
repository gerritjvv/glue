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
import org.glue.unit.om.ScriptRepl
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.om.Provider
import org.glue.unit.repo.GlueUnitRepository
import org.streams.commons.zookeeper.ZGroup
import org.streams.commons.zookeeper.ZLock


/**
 * 
 * Runs a single workflow. 
 * Represents a workflow process.
 */
class ReplRunner {

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
	 * Run the repl
	 * @param config WorkflowRunnerConfig
	 * @param di WorkflowRunnerBootstrap
	 */
	static void run(WorkflowRunnerConfig config, WorkflowRunnerBootstrap di){

		String unitId=config.uuid
		GlueUnitRepository repo = di.getBean(GlueUnitRepository)

		//build dummy workflow
		GlueUnit unit = di.getBean(GlueUnitBuilder).build("tasks{ }")
		unit.name = config.workflow
		println "ReplRunner.run : unit.name " + unit?.name + " config.worfklow " + config?.workflow
		GlueContext context

		try{


			//Build a context for the current execution
			GlueContextBuilder contextBuilder = di.getBean(GlueContextBuilder)
			context = contextBuilder.build(unitId, unit, config.params)

			println "ReplRunner: using repl: " + di.getBean(ScriptRepl)
			
			def cmds = []
			if(config.workflow == "testflow-exitontest"){
				if(config.lang == "groovy")
				 cmds << "exit"
				else if(config.lang == "jython")
				 cmds << 'exit()'
				else if(config.lang == "clojure")
				 cmds << '(exit)'
			}
				
			di.getBean(ScriptRepl).run(context, cmds as String[])
		
			
		}finally{

			context?.destroy()

		}
		return
	}
}
