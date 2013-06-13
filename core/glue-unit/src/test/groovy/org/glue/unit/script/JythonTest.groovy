package org.glue.unit.script;


import static org.junit.Assert.*

import org.apache.log4j.Logger
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.GlueState
import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.exec.impl.GlueExecutorImpl
import org.glue.unit.exec.impl.MockProcessExecutorProvider
import org.glue.unit.exec.impl.ProcessExecutorImpl
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.Provider
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.repo.impl.DirGlueUnitRepository
import org.glue.unit.om.impl.*
import org.junit.Test

/**
 *
 * Test that the ScriptClassCache does cache the scripts correctly.
 * Unix platform test only
 */
class JythonTest {

	
	@Test
	public void specialCharacterEncoding(){
		
				def provider = new MapGlueModuleFactoryProvider(null)
				provider.addModule("enc", new MockGlueUnicodeModule())
				
				ConfigObject execConfig = new ConfigObject();
		
				Provider<ProcessExecutor> processExecutorProvider = new MockProcessExecutorProvider(errorInExec:false, processExecutorClosure:{ new ProcessExecutorImpl() })
		
				GlueUnitRepository repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
					'src/test/resources/test-flow-repo'
				])
		
				Provider<UnitExecutor> unitExecutorProvider = new MockUnitExecutorProvider(processExecutorProvider:processExecutorProvider)
		
				GlueExecutor exec = new GlueExecutorImpl(execConfig, repo,
						new DefaultGlueContextBuilder(provider),
						unitExecutorProvider,
						new DefaultGlueUnitBuilder()
						)
		
		
				String uid=exec.submitUnitAsName( "jythonworkflowencoding", [:] )
		
				exec.waitFor uid
		
				assertEquals(GlueState.FINISHED, exec.getStatus(uid))
	}

	/**
	 * Test issue 52
	 *
	@Test
	public void runSubprocess(){
		GlueModuleFactory moduleFactory = new GlueModuleFactoryImpl();

		ConfigObject execConfig = new ConfigObject();

		Provider<ProcessExecutor> processExecutorProvider = new MockProcessExecutorProvider(errorInExec:false, processExecutorClosure:{ new ProcessExecutorImpl() })

		GlueUnitRepository repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			'src/test/resources/test-flow-repo'
		])

		Provider<UnitExecutor> unitExecutorProvider = new MockUnitExecutorProvider(processExecutorProvider:processExecutorProvider)

		GlueExecutor exec = new GlueExecutorImpl(execConfig, repo,
				new DefaultGlueContextBuilder(new MapGlueModuleFactoryProvider(null)),
				unitExecutorProvider,
				new DefaultGlueUnitBuilder()
				)


		String uid=exec.submitUnitAsName( "jythonsubprocessworkflow", [:] )

		exec.waitFor uid

		assertEquals(GlueState.FINISHED, exec.getStatus(uid))

		moduleFactory.addModule("sql", new MockGlueModule())
		GlueContextImpl ctx = new GlueContextImpl()
		ctx.moduleFactory = moduleFactory
	}
	*/
}
