package org.glue.gluetest.test;

import static org.junit.Assert.*

import org.glue.gluetest.DbStoreModuleBuilder
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
 * Tests how the DBStoreModuleBuilder interacts with the SimpleGlueServer
 *
 */
class DBStoreModuleBuilderTest {

	/**
	 * Test that the DBStoreModuleBuilder ads the DbStoreModule to the GlueServer
	 */
	@Test
	public void testHDFSModuleBuilder(){

		SimpleGlueServer server = GlueServerBootstrap.createServer()
		def dbName = 'dbstoretest'
		server.startDBServer([dbName])
		server.addModuleBuilder(new DbStoreModuleBuilder(dbName))

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
				assertNotNull(moduleFactory.getModule("dbstore"))

				GlueModule module = moduleFactory.getModule("dbstore")

				module << ['myKey', '1']
				assertEquals('1', module.getAt('myKey'))

				module.destroy()
			}finally{
				moduleFactory.destroy()
			}
		}finally{
			server.stop()
		}
	}
}
