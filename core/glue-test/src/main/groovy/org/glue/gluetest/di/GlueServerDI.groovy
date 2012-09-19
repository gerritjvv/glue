package org.glue.gluetest.di

import javax.inject.Inject

import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.exec.impl.DefaultProcessExecutorProvider
import org.glue.unit.exec.impl.DefaultUnitExecutorProvider
import org.glue.unit.exec.impl.GlueExecutorImpl
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.om.GlueUnitValidator
import org.glue.unit.om.Provider
import org.glue.unit.om.impl.DefaultGlueContextBuilder
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.om.impl.DefaultGlueUnitValidator
import org.glue.unit.om.impl.MapGlueModuleFactoryProvider
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.repo.impl.MapGlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
import org.glue.unit.status.impl.MapUnitStatusManager
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.context.annotation.Bean

/**
 * Builds the GlueServer for the testing framework
 */
@Configurable
class GlueServerDI{

	@Inject
	BeanFactory beanFactory
	
	@Bean
	GlueExecutor glueExecutor(){


		GlueUnitRepository repo = beanFactory.getBean(GlueUnitRepository.class)
		Provider<UnitExecutor> unitExecutorProvider = beanFactory.getBean("unitExecutorProvider")
		GlueUnitBuilder unitBuilder = beanFactory.getBean(GlueUnitBuilder.class)
		GlueContextBuilder contextBuilder = beanFactory.getBean(GlueContextBuilder.class)

		GlueUnitValidator unitValidator = beanFactory.getBean(GlueUnitValidator.class)

		new GlueExecutorImpl(null,
				repo,
				contextBuilder,
				unitExecutorProvider,
				unitBuilder,
				unitValidator)
	}


	@Bean
	Provider<UnitExecutor> unitExecutorProvider(){


		int executorPoolThreads = 2

		new DefaultUnitExecutorProvider(beanFactory.getBean('processExecutorProvider'), executorPoolThreads)
	}

	@Bean
	Provider<ProcessExecutor> processExecutorProvider(){
		new DefaultProcessExecutorProvider()
	}

	@Bean
	MapGlueUnitRepository glueUnitRepository(){
		new MapGlueUnitRepository()
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
	MapUnitStatusManager statusManager(){
		return new MapUnitStatusManager()
	}
	
	@Bean
	GlueContextBuilder glueContextBuilder(){
		new DefaultGlueContextBuilder(beanFactory.getBean("moduleFactoryProvider"),
				beanFactory.getBean(GlueUnitStatusManager.class))
	}
}