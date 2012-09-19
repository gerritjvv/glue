package org.glue.unit.exec.impl.runner

import org.glue.unit.exec.impl.WorkflowRunnerConfig
import org.glue.unit.exec.impl.runner.di.WorkflowRunnerDI
import org.glue.unit.om.impl.SpringGlueModuleFactoryProvider
import org.springframework.context.annotation.AnnotationConfigApplicationContext


/**
 * 
 * Bootstraps the workflow runner.
 *
 */
@Typed
class WorkflowRunnerBootstrap {

	AnnotationConfigApplicationContext context

	WorkflowRunnerBootstrap(WorkflowRunnerConfig config){

		context = new AnnotationConfigApplicationContext()


		//register LauncherConfig singleton
		context.getDefaultListableBeanFactory().registerSingleton("config", config)

		//register Configuration objects
		context.register WorkflowRunnerDI.class

		//register module factory
		//NOTE: The module factory will add all modules as beans to the spring bean factory
		def provider = new SpringGlueModuleFactoryProvider(config.moduleFactoryConfig, context.getDefaultListableBeanFactory())
		context.getDefaultListableBeanFactory().registerSingleton("moduleFactoryProvider", provider)


		//NOTE: After these methods we do not add any more beans.
		context.getDefaultListableBeanFactory().addBeanPostProcessor(provider.postProcessor)


		context.refresh()
	}

	AnnotationConfigApplicationContext getContext(){
		context
	}

	def getBean(String name){
		context.getBean name
	}

	def getBean(String name, Class requiredType){
		context.getBean name, requiredType
	}


	def getBean(Class requiredType){
		context.getBean requiredType
	}

	def shutdown(){
		context.close()
	}
}