package org.glue.rest.di;

import static org.junit.Assert.*

import org.glue.rest.config.LauncherConfig
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.impl.SpringGlueModuleFactoryProvider
import org.junit.Test
import org.springframework.context.ApplicationContext

/**
 *
 * Ensures that the DI framework works correctly e.g. that post processors are correctly set.<br/>
 * Singleton beans are singleton etc.
 *
 */
class DIBootstrapTest {

	/**
	 * This tests that the GlueModulePostProcessor is being set correctly
	 */
	@Test
	public void testPostProcessors(){


		String execConfigPath='src/test/resources/conf/exec.groovy';
		String moduleConfigPath="src/test/resources/conf/modules.groovy";
		int port=8225;
		String host = "localhost:$port";

		LauncherConfig config = LauncherConfig.getInstance([
			execConfigPath,
			moduleConfigPath,
			port]
		as String[])

		def out = System.out
		def err = System.err

		DIBootstrap bootstrap = new DIBootstrap(config)

		SpringGlueModuleFactoryProvider provider = bootstrap.getBean("moduleFactoryProvider")


		Collection processors = bootstrap.getContext().getDefaultListableBeanFactory().getBeanPostProcessors()

		def obj = processors.find {  it.is(provider.postProcessor) }

		System.out = out
		System.err = err

		assertNotNull(obj)

		bootstrap.shutdown()
	}

	/**
	 * Test that all the singleton variables + the modules have been added as beans.
	 */
	@Test
	public void testSingletons(){


		String execConfigPath='src/test/resources/conf/exec.groovy';
		String moduleConfigPath="src/test/resources/conf/modules.groovy";
		int port=8225;
		String host = "localhost:$port";

		LauncherConfig config = LauncherConfig.getInstance([
			execConfigPath,
			moduleConfigPath,
			port]
		as String[])

		def out = System.out
		def err = System.err

		DIBootstrap bootstrap = new DIBootstrap(config)


		System.out = out
		System.err = err

		//--- test for launcher config and module factory provider
		ApplicationContext context = bootstrap.getContext()

		def launcherConfig = bootstrap.getBean("launcherConfig")
		assertNotNull(launcherConfig)
		assertTrue( launcherConfig instanceof LauncherConfig )
		assertTrue(context.isSingleton("launcherConfig"))

		GlueModuleFactoryProvider provider = bootstrap.getBean("moduleFactoryProvider")
		assertNotNull(provider)
		assertTrue( provider instanceof SpringGlueModuleFactoryProvider )
		assertTrue(context.isSingleton("moduleFactoryProvider"))

		//-- test for modules
		config.moduleFactoryConfig.each {String name, ConfigObject moduleConfig ->

			//check that bean exists
			def moduleBean = bootstrap.getBean(name)
			assertNotNull(moduleBean)

			Class cls = Thread.currentThread().getContextClassLoader().loadClass(moduleConfig.className)

			//check that the beans class is that of the module
			assertTrue( moduleBean.class.isAssignableFrom(cls) )

			println "Checking module: $moduleBean"
			//check to see if the bean should be prototype of singleton
			boolean isSingleton = (moduleConfig.isSingleton) ? Boolean.valueOf(moduleConfig.isSingleton) : true

			if(isSingleton) {
				assertTrue(context.isSingleton(name))
			}else{
				assertFalse(context.isSingleton(name))
			}
		}

		bootstrap.shutdown()
	}
}
