package org.glue.gluetest.test;

import static org.junit.Assert.*

import org.glue.gluetest.HDFSModuleBuilder
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
 * Test the HDFSTriggerModuleBuilder
 *
 */
class HDFSModuleBuilderTest {



	/**
	 * Test that the HDFSTriggerModuleBuilder ads the HDFSModule to the GlueServer and interacts with the hdfs cluster.
	 */
	@Test
	public void testHDFSModuleBuilder(){

		SimpleGlueServer server = GlueServerBootstrap.createServer()
		server.addModuleBuilder(new HDFSModuleBuilder())
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

			assertNotNull(moduleFactory.getModule("hdfs"))
			try{
				GlueModule hdfsModule = moduleFactory.getModule("hdfs")
			}finally{
				moduleFactory.destroy()
			}
		}finally{
			server.stop()
		}
	}
}
