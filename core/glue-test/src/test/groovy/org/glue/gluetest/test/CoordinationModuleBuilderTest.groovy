package org.glue.gluetest.test;

import static org.junit.Assert.*

import org.glue.gluetest.CoordinationModuleBuilder
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
 * Test the CoordinationModuleBuilder interaction with the SimpleGlueServer
 *
 */
class CoordinationModuleBuilderTest {


	/**
	 * Test that the CoordinationModuleBuilder ads the CoordinationModule to the GlueServer
	 */
	@Test
	public void testCoordinationModuleBuilder(){

		SimpleGlueServer server = GlueServerBootstrap.createServer()
		server.addModuleBuilder(new CoordinationModuleBuilder())
		try{
			server.start()

			def unitText = """
			name='test'
			tasks{
			  processA{
				tasks={ context ->
						def hasLock = context.coordination.tryLock('test1', 10000)
						
						if(!hasLock){
						  throw new RuntimeException('Error could not obtain test1')
						}
						
						context.coordination.unlock('test1')
						
				}
			  }
			}
			"""

			GlueUnit unit = new DefaultGlueUnitBuilder().build(unitText)
			def unitId = server.exec.submitUnitAsText(unitText, [:])
			server.exec.waitFor(unitId)

			assertEquals(GlueState.FINISHED, server.unitStatusManager.getUnitStatus(unitId)?.status)
		}finally{
			server.stop()
		}
	}
}
