package org.glue.gluetest.test;

import static org.junit.Assert.*

import org.glue.gluetest.SQLModuleBuilder
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
 * Tests how the SQLModuleBuilder interacts with the SimpleGlueServer
 *
 */
class SQLModuleBuilderTest {


	/**
	 * Test that the PigModuleBuilder ads the PigModule to the GlueServer
	 */
	@Test
	public void testHDFSModuleBuilder(){

		SimpleGlueServer server = GlueServerBootstrap.createServer()
		def dbNames = ['mydb1', 'mydb2']
		server.addModuleBuilder(new SQLModuleBuilder(dbNames))
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
				assertNotNull(moduleFactory.getModule("sql"))
				
				GlueModule module = moduleFactory.getModule("sql")
				
				dbNames.each { dbName ->
					module.execSql(dbName, '''
					   CREATE table test1 ( name VARCHAR )
					''')
				
					module.execSql(dbName, 'INSERT INTO test1 (NAME) VALUES(\'hi\')')
					module.eachSqlResult ( dbName, 'select name from test1', {
						assertEquals('hi', it.name)
					})
				}
				
			}finally{
				moduleFactory.destroy()
			}
		}finally{
			server.stop()
		}
	}


	
}
