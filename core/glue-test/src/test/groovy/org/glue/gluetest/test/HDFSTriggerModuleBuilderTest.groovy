package org.glue.gluetest.test;

import static org.junit.Assert.*

import org.glue.gluetest.HdfsTriggerModuleBuilder
import org.glue.gluetest.SimpleGlueServer
import org.glue.gluetest.util.GlueServerBootstrap
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueContextBuilder
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.junit.Test

class HDFSTriggerModuleBuilderTest {

	/**
	*
	*/
   @Test
   public void testHDFSTriggerModuleBuilder(){

	   SimpleGlueServer server = GlueServerBootstrap.createServer()
	   server.addModuleBuilder(new HdfsTriggerModuleBuilder())
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

		   println "!!!!!!!!! == ${moduleFactory.getModule(HdfsTriggerModuleBuilder.name)}"
		   assertNotNull(moduleFactory.getModule("hdfsTriggers"))
		   moduleFactory.destroy()
	   }finally{
		   server.stop()
	   }
   }
		
}
