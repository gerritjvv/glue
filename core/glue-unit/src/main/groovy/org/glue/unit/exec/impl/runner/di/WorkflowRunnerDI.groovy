package org.glue.unit.exec.impl.runner.di

import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.exec.impl.DefaultProcessExecutorProvider
import org.glue.unit.exec.impl.DefaultUnitExecutorProvider
import org.glue.unit.exec.impl.GlueExecutorImpl
import org.glue.unit.exec.impl.WorkflowRunnerConfig
import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.log.impl.DefaultGlueExecLoggerProvider
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.om.GlueUnitValidator
import org.glue.unit.om.Provider
import org.glue.unit.om.impl.DefaultGlueContextBuilder
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.om.impl.DefaultGlueUnitValidator
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.repo.impl.DirGlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.streams.commons.zookeeper.ZConnection
import org.streams.commons.zookeeper.ZLock

/**
 * 
 * Spring DI for the Workflow runner
 *
 */
@Typed(TypePolicy.DYNAMIC)
@Configuration
class WorkflowRunnerDI {

	@Autowired(required = true)
	BeanFactory beanFactory;

	@Autowired(required = true)
	WorkflowRunnerConfig config

	
	@Bean
	org.glue.unit.om.ScriptRepl scriptRepl(){
		if(config.lang == "jython")
		   return new org.glue.unit.repl.jython.JythonRepl()
		else if(config.lang == "clojure")
		   return new org.glue.unit.repl.clojure.ClojureRepl()
		else 
		   return new org.glue.unit.repl.groovy.GroovyRepl()	
	}
	
	@Bean
	ZLock getZLock(){
		if(config.execConfig?.zkhost && config.execConfig?.zktimeout){
			
			int zktimeout = Integer.parseInt(config.execConfig.zktimeout)
			if(zktimeout < 1000) zktimeout = 1000
			
			return new ZLock(new ZConnection(config.execConfig.zkhost, zktimeout),
				"/glue-workflow-locks/",, zktimeout);
			
		}else{
			return null
		}
	}
	
	@Bean
	GlueExecLoggerProvider glueExecLoggerProvider(){
		def dir
		if(config.execConfig.processLogDir){
			dir = config.execConfig.processLogDir
		}else{
			dir = "/tmp/glue-logs"
		}

		new DefaultGlueExecLoggerProvider(new File(dir.toString()))
	}

	@Bean
	GlueUnitStatusManager glueUnitStatusManager(){

		Class<GlueUnitStatusManager> statusManagerClass =
				Thread.currentThread().getContextClassLoader().loadClass(config.statusManagerConfig.className as String)

		GlueUnitStatusManager statusManager = statusManagerClass.newInstance()
		statusManager.init config.statusManagerConfig.config

		return statusManager
	}

	@Bean
	GlueExecutor glueExecutor(){

		ConfigObject configObject = config.execConfig

		GlueUnitRepository repo = beanFactory.getBean(GlueUnitRepository.class)
		Provider<UnitExecutor> unitExecutorProvider = beanFactory.getBean("unitExecutorProvider")
		GlueUnitBuilder unitBuilder = beanFactory.getBean(GlueUnitBuilder.class)
		GlueContextBuilder contextBuilder = beanFactory.getBean(GlueContextBuilder.class)

		GlueUnitValidator unitValidator = beanFactory.getBean(GlueUnitValidator.class)

		new GlueExecutorImpl(configObject,
				repo,
				contextBuilder,
				unitExecutorProvider,
				unitBuilder,
				unitValidator)
	}


	@Bean
	Provider<UnitExecutor> unitExecutorProvider(){


		int executorPoolThreads = config.executorPoolThreads

		new DefaultUnitExecutorProvider(beanFactory.getBean('processExecutorProvider'), executorPoolThreads)
	}

	@Bean
	Provider<ProcessExecutor> processExecutorProvider(){
		new DefaultProcessExecutorProvider()
	}

	//DirGlueUnitRepository
	@Bean
	GlueUnitRepository glueUnitRepository(){
		List<String> directories = Arrays.asList(config.execConfig.lookupPath.split("[,;]"))
		new DirGlueUnitRepository(beanFactory.getBean(GlueUnitBuilder.class), directories)
	}

	@Bean
	GlueUnitBuilder glueUnitBuilder(){
		new DefaultGlueUnitBuilder()
	}
	@Bean
	GlueUnitValidator glueUnitValidator(){
		new DefaultGlueUnitValidator()
	}

	@Bean
	GlueContextBuilder glueContextBuilder(){
		new DefaultGlueContextBuilder(beanFactory.getBean("moduleFactoryProvider"),
				beanFactory.getBean(GlueUnitStatusManager),
				beanFactory.getBean(GlueExecLoggerProvider))
	}
}