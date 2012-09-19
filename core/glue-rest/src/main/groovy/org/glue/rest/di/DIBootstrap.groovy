
package org.glue.rest.di


import org.glue.rest.config.LauncherConfig
import org.glue.unit.om.impl.SpringGlueModuleFactoryProvider
import org.glue.unit.om.impl.spring.GlueModulePostProcessor;
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 *
 * This class is the entry point to the DI.<br/>
 * Used to pre register any beans and externally created DI object.<br/>
 * This class is an annotated Configuration object but does act as one,
 * so that its architectually valid to define new beans inside and specify bean names.<br/>
 * <p/.
 */
class DIBootstrap {

	AnnotationConfigApplicationContext context

	DIBootstrap(LauncherConfig config){
		context = new AnnotationConfigApplicationContext()

		
		//register LauncherConfig singleton
		context.getDefaultListableBeanFactory().registerSingleton("launcherConfig", config)
      println "Registered launcherConfig";
		//register Configuration objects
		context.register LauncherDI.class
		context.register RestDI.class

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
