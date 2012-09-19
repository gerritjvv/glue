package org.glue.gluetest.test;

import static org.junit.Assert.*

import org.glue.gluetest.HDFSModuleBuilder
import org.glue.gluetest.PigModuleBuilder
import org.glue.gluetest.SimpleGlueServer
import org.glue.gluetest.util.GlueServerBootstrap
import org.glue.unit.exec.GlueState
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueContextBuilder
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.junit.Test

/**
 * 
 * Test the PigModuleBuilder interaction with the SimpleGlueServer
 *
 */
class PigModuleBuilderTest {


	/**
	 * Test that the PigModuleBuilder ads the PigModule to the GlueServer
	 */
	@Test
	public void testHDFSModuleBuilder(){

		SimpleGlueServer server = GlueServerBootstrap.createServer()
		server.addModuleBuilder(new HDFSModuleBuilder())
		server.addModuleBuilder(new PigModuleBuilder())
		try{
			server.start()

			File datTestFile = new File('src/test/resources/dat.test')
			String hdfsPath = '/tmp/PigModuleBuilderTest/testHDFSModuleBuilder'

			def unitText = """
			name='test'
			tasks{
			  processA{
				tasks={ context ->
				    context.hdfs.eachLine('$hdfsPath', {println it})
				    println context.hdfs.list('$hdfsPath', { println 'File ' + it})
					context.pig.run('''
						a = LOAD '$hdfsPath';
						g = GROUP a ALL;
						r = FOREACH g GENERATE COUNT(\$1);
						DUMP r;
					''', [:])
				}
			  }
			}
			"""

			GlueUnit unit = new DefaultGlueUnitBuilder().build(unitText)

			GlueContext ctx = new DefaultGlueContextBuilder().build("123", unit, [:])
			GlueModuleFactory moduleFactory = server.getModuleFactoryProvider().get(ctx)
			
			moduleFactory.getModule("hdfs").put(datTestFile.absolutePath, hdfsPath)

			println "hdfs each line !!!!!!!!!"			
			int count = 0
			moduleFactory.getModule("hdfs").eachLine (hdfsPath, { println it; count++ })
			assertTrue ( count > 3 )
			
			assertTrue(moduleFactory.getModule("hdfs").exist(hdfsPath))
			
			try{
				assertNotNull(moduleFactory.getModule("pig"))
				def unitId = server.exec.submitUnitAsText(unitText, [:])
				server.exec.waitFor(unitId)

				assertEquals(GlueState.FINISHED, server.unitStatusManager.getUnitStatus(unitId)?.status)
			}finally{
				moduleFactory.destroy()
			}
		}finally{
			server.stop()
		}
	}
}
