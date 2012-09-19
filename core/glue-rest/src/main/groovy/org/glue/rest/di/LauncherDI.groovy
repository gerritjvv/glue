package org.glue.rest.di

import groovy.util.ConfigObject

import org.glue.rest.config.LauncherConfig
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.impl.queue.QueuedExecServiceImpl
import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.log.impl.DefaultGlueExecLoggerProvider
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.om.GlueUnitValidator
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


/**
 * 
 * Defines the spring DI for Launcher
 * 
 *
 */
@Configuration
class LauncherDI {

	@Autowired(required = true)
	BeanFactory beanFactory;

	@Autowired(required = true)
	LauncherConfig config

	@Bean
	GlueUnitStatusManager glueUnitStatusManager(){

		Class<GlueUnitStatusManager> statusManagerClass =
				Thread.currentThread().getContextClassLoader().loadClass(config.statusManagerConfig.className)

		GlueUnitStatusManager statusManager = statusManagerClass.newInstance()
		statusManager.init config.statusManagerConfig.config

		return statusManager
	}

	@Bean
	GlueExecLoggerProvider glueExecLoggerProvider(){
		new DefaultGlueExecLoggerProvider(new File(config.processLogDir))
	}

	@Bean
	GlueExecutor glueExecutor(){

		ConfigObject configObject = config.execConfig

		GlueUnitRepository repo = beanFactory.getBean(GlueUnitRepository.class)
		GlueUnitBuilder unitBuilder = beanFactory.getBean(GlueUnitBuilder.class)
		GlueContextBuilder contextBuilder = beanFactory.getBean(GlueContextBuilder.class)

		GlueUnitValidator unitValidator = beanFactory.getBean(GlueUnitValidator.class)

		return new QueuedExecServiceImpl(
		config.executorMaxProcesses,
		config.processJavaOpts,
		config.processClassPath,
		repo,
		new DefaultGlueUnitBuilder(),
		config.processExecConfig,
		config.processModuleConfig,
		beanFactory.getBean(GlueExecLoggerProvider.class),
		contextBuilder
		)
	}

	@Bean
	GlueUnitRepository glueUnitRepository(){
		List<String> directories = config.execConfig.lookupPath.split("[,;]")
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
				beanFactory.getBean(GlueUnitStatusManager.class),
				beanFactory.getBean(GlueExecLoggerProvider.class)
				)
	}
}
