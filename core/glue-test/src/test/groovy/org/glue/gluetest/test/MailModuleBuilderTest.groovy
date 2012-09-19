package org.glue.gluetest.test;

import static org.junit.Assert.*

import org.glue.gluetest.MailModuleBuilder
import org.glue.gluetest.SimpleGlueServer
import org.glue.gluetest.util.GlueServerBootstrap
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueContextBuilder
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.junit.Test

/**
 * 
 *
 *
 */
class MailModuleBuilderTest {

	/**
	* Test that the MailModuleBuilder ads a MockMailModule to the GlueServer
	*/
   @Test
   public void testMailModuleBuilder(){

	   SimpleGlueServer server = GlueServerBootstrap.createServer()
	   server.addModuleBuilder(new MailModuleBuilder())

	   try{
		   server.start()

		   GlueUnit unit = new DefaultGlueUnitBuilder().build("""
		  name='test'
		  tasks{
			processA{
			  tasks={ context ->
			  }
			}
		  }
		  """)

		   GlueContext ctx = new DefaultGlueContextBuilder().build("123", unit, [:])
		   GlueModuleFactory moduleFactory = server.getModuleFactoryProvider().get(ctx)
		   try{
			   assertNotNull(moduleFactory.getModule("mail"))

			   GlueModule module = moduleFactory.getModule("mail")
			   module.mail(['test'] as String[], 'test subject', 'test body')
			   
			   assertEquals(1, module.counter.get())
			   
			   module.destroy()
		   }finally{
			   moduleFactory.destroy()
		   }
	   }finally{
		   server.stop()
	   }
   }
}
