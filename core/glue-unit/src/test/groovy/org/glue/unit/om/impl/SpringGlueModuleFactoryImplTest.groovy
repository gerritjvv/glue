package org.glue.unit.om.impl;

import static org.junit.Assert.*

import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactory
import org.junit.Test
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 *
 * Tests that singleton and non singleton instances of GlueModule are created correctly.
 *
 */
class SpringGlueModuleFactoryImplTest {

	String mockGlueModuleClass = MockGlueModule.class.getName()

	
	/**
	* Test that an the module's init method has been called
	*/
   @Test
   void testGlueModuleInitMethodCall(){
	   DefaultListableBeanFactory factory = new DefaultListableBeanFactory()

	   
		GlueContext context = new DefaultGlueContextBuilder().build('123', null, [:])
	   
	   def config = """
		
		 testModule {
			className="${mockGlueModuleClass}"
			isSingleton = "true"
			config{
				myProperty="myValue"
			}
		 }
		 
		 testModule2 {
			className="${mockGlueModuleClass}"
			isSingleton = "true"
			config{
				a="b"
			}
		 }
		 
	   """

	   //error should be thrown
	   SpringGlueModuleFactoryProvider provider = new SpringGlueModuleFactoryProvider(config, factory)
	   
	   GlueModule module = provider.get(context).getModule('testModule')
	   
	   assertNotNull(module)
	   assertNotNull(module.config)
	   assertNotNull(module.config.myProperty)
	   assertEquals("myValue", module.config.myProperty)
	   
	   GlueModule module2 = provider.get(context).getModule('testModule2')
	   
	   assertNotNull(module2)
	   assertNotNull(module2.config)
	   assertNotNull(module2.config.a)
	   assertEquals("b", module2.config.a)
	   
	   
	}
   
	/**
	* Test that an exception is thrown when the module config does not contain a class name property
	*/
   @Test(expected=ModuleConfigurationException)
   void testClassNotFoundForModule(){
	   DefaultListableBeanFactory factory = new DefaultListableBeanFactory()

	   def config = """
		
		 testModule {
		    className="notclassforthis"
			isSingleton = "true"
		 }
		 
	   """

	   //error should be thrown
	   SpringGlueModuleFactoryProvider provider = new SpringGlueModuleFactoryProvider(config, factory)
	}
	   
	/**
	* Test that an exception is thrown when the module config does not contain a class name property
	*/
   @Test(expected=ModuleConfigurationException)
   void testNoClassName(){
	   DefaultListableBeanFactory factory = new DefaultListableBeanFactory()

	   def config = """
		
		 testModule {
			isSingleton = "true"
		 }
		 
	   """

	   //error should be thrown
	   SpringGlueModuleFactoryProvider provider = new SpringGlueModuleFactoryProvider(config, factory)
	   
   }

	/**
	* Test that an exception is thrown when the module config is bad
	*/
   @Test(expected=ModuleConfigurationException)
   void testBadConfiguration(){
	   DefaultListableBeanFactory factory = new DefaultListableBeanFactory()

	   def config = """
		
		 testModule {
			className="${mockGlueModuleClass}"
			isSingleton = "true"
		 
	   """

	   //error should be thrown
	   SpringGlueModuleFactoryProvider provider = new SpringGlueModuleFactoryProvider(config, factory)
	   
   }
	
   
	/**
	* Test that if two modules have the same name no error is thrown.<br/>
	* this is due to how the ConfigObject works internally which uses a map.
	*/
   @Test
   void testDuplicateModuleNameNoError(){
	   DefaultListableBeanFactory factory = new DefaultListableBeanFactory()

	   def config = """
		
		 testModule {
			className="${mockGlueModuleClass}"
			isSingleton = "true"
		 }
		 
		 testModule {
			className="${mockGlueModuleClass}"
			isSingleton = "true"
		 }
		 
	   """

	   //no error should be thrown
	   SpringGlueModuleFactoryProvider provider = new SpringGlueModuleFactoryProvider(config, factory)
	   assertTrue(true)   
	  
   }

	/**
	 * Check that a prototype module instance will only exist once per GlueModuleFactoryInstance.
	 */
	@Test
	void testSingletonAndPrototypeModules(){

		def context = new AnnotationConfigApplicationContext()
		DefaultListableBeanFactory factory = context.getBeanFactory()
				
		def config = """
		 
		  testModule {
			 className="${mockGlueModuleClass}"
			 isSingleton = "true"
		  }
		  testPrototypeModule {
			 className="${mockGlueModuleClass}"
			 isSingleton = "false"
		  }
		"""

		SpringGlueModuleFactoryProvider provider = new SpringGlueModuleFactoryProvider(config, factory)

		context.refresh()
		
		GlueContext glueContext = new DefaultGlueContextBuilder().build('123', null, [:])
		
		GlueModuleFactory moduleFactory = provider.get(glueContext)

		assertNotNull(moduleFactory)
		assertEquals(2, moduleFactory.getAvailableModules().size())
		assertNotNull(moduleFactory.getModule('testModule'))


		//test that even though we get different module factory instances
		//the GlueModule is always the same instance
		//also test that the factory instances are not the same
		GlueModule instance1 = moduleFactory.getModule('testModule')

		GlueModuleFactory moduleFactory2 = provider.get(glueContext)
		GlueModule instance2 = moduleFactory.getModule('testModule')

		assertNotSame(moduleFactory, moduleFactory2)
		assertEquals(instance1, instance2)
		
		//now test that even if we call it 10 times the testPrototypeModule will
		//not be the same across GlueModuleFactory instances
		Collection<GlueModule> prototypeModules = []

		(1..10).each{
			moduleFactory = provider.get()
			GlueModule protoTypeInstance = moduleFactory.getModule('testPrototypeModule')
			assertNotNull(protoTypeInstance)
			//check that the instance is unique
			assertNull(prototypeModules.find{ it.is ( protoTypeInstance ) } )

			prototypeModules << protoTypeInstance
		}
	}

	/**
	 * Test that a singleton module instance is the same instance for different GlueModuleFactory instance.
	 */
	@Test
	void testSingletonModule(){
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory()

		def config = """
		 
		  testModule {
		     className="${mockGlueModuleClass}"
		     isSingleton = "true"
		  }
		  
		"""

		SpringGlueModuleFactoryProvider provider = new SpringGlueModuleFactoryProvider(config, factory)

		GlueContext glueContext = new DefaultGlueContextBuilder().build('123', null, [:])
		
		GlueModuleFactory moduleFactory = provider.get(glueContext)

		assertNotNull(moduleFactory)
		assertEquals(1, moduleFactory.getAvailableModules().size())
		assertNotNull(moduleFactory.getModule('testModule'))


		//test that even though we get different module factory instances
		//the GlueModule is always the same instance
		//also test that the factory instances are not the same
		GlueModule instance1 = moduleFactory.getModule('testModule')

		GlueModuleFactory moduleFactory2 = provider.get(glueContext)
		GlueModule instance2 = moduleFactory.getModule('testModule')

		assertNotSame(moduleFactory, moduleFactory2)
		assertEquals(instance1, instance2)

	}
}
