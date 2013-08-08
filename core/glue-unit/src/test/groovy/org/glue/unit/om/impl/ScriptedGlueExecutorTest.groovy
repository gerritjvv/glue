package org.glue.unit.om.impl

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
import org.junit.Test

class ScriptedGlueExecutorTest {
	static final Logger log = Logger.getLogger(ScriptedGlueExecutorTest.class)


	/*
	@Test
	public void testUnitExecutorScala() {

		
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

		
		String uid=exec.submitUnitAsName( "scalaworfklow", [:] )

		exec.waitFor uid

		assertEquals(GlueState.FINISHED, exec.getStatus(uid))
			
		moduleFactory.addModule("sql", new MockGlueModule())
		GlueContextImpl ctx = new GlueContextImpl()
		ctx.moduleFactory = moduleFactory
		
	}*/
	
	@Test
	public void testUnitExecutorJython() {

		
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

		
		String uid=exec.submitUnitAsName( "jythonworkflow", [:] )

		exec.waitFor uid

		assertEquals(GlueState.FINISHED, exec.getStatus(uid))
			
		moduleFactory.addModule("sql", new MockGlueModule())
		GlueContextImpl ctx = new GlueContextImpl()
		ctx.moduleFactory = moduleFactory
		
	}

	@Test
	public void testUnitExecutorClojure() {

		
		MapGlueModuleFactoryProvider moduleFactory = new MapGlueModuleFactoryProvider();
		moduleFactory.addModule("sql", new MockGlueModule())
		
		Provider<ProcessExecutor> processExecutorProvider = 
			new MockProcessExecutorProvider(errorInExec:false, processExecutorClosure:{ new ProcessExecutorImpl() })

		GlueUnitRepository repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			'src/test/resources/test-flow-repo'
		])

		Provider<UnitExecutor> unitExecutorProvider = new MockUnitExecutorProvider(processExecutorProvider:processExecutorProvider)
		ConfigObject execConfig = new ConfigObject();
		
		GlueExecutor exec = new GlueExecutorImpl(execConfig, repo,
				new DefaultGlueContextBuilder(moduleFactory),
				unitExecutorProvider,
				new DefaultGlueUnitBuilder()
				)

		
		String uid=exec.submitUnitAsName( "myclojure", [:] )

		exec.waitFor uid

		assertEquals(GlueState.FINISHED, exec.getStatus(uid))
		
	}

	
	@Test
	public void testUnitExecutorJRuby() {

		
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

		
		String uid=exec.submitUnitAsName( "jrubyworkflow", [:] )

		exec.waitFor uid

		assertEquals(GlueState.FINISHED, exec.getStatus(uid))
			
		moduleFactory.addModule("sql", new MockGlueModule())
		GlueContextImpl ctx = new GlueContextImpl()
		ctx.moduleFactory = moduleFactory
		
	}

}
