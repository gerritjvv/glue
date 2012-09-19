package org.glue.gluetest.util

import org.glue.gluetest.GlueServer
import org.glue.gluetest.SimpleGlueServer
import org.glue.gluetest.di.GlueServerDI
import org.glue.unit.om.impl.SpringGlueModuleFactoryProvider
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * 
 * Bootstraps the GlueServer with all its dependencies.
 *
 */
class GlueServerBootstrap{


	/**
	 * Creates a new instance of SimpleGlueServer
	 * @return GlueServer
	 */
	static GlueServer createServer(){
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()

		context.register GlueServerDI
		context.register SimpleGlueServer

		//register module factory
		//NOTE: The module factory will add all modules as beans to the spring bean factory
		def provider = new SpringGlueModuleFactoryProvider("", context.getDefaultListableBeanFactory())
		context.getDefaultListableBeanFactory().registerSingleton("moduleFactoryProvider", provider)

		//NOTE: After these methods we do not add any more beans.
		context.getDefaultListableBeanFactory().addBeanPostProcessor(provider.postProcessor)

		context.refresh()

		def server = context.getBean(SimpleGlueServer)
		
		assert server != null
		
		return server
	}
	
}