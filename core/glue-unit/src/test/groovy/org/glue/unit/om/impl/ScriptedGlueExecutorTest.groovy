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


	@Test
	public void testUnitExecutorUsingRepository() {

		
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
		
		GlueContext ctx1 = DefaultGlueContextBuilder.buildStaticGlueContext(ctx)
		println("Generator context: ctx1:" + ctx1)
//		PyObject obj = new PyObjectDerived(PyType.fromClassSkippingInners(ctx.getClass(), new HashSet()))
//		obj.javaProxy = ctx;
//
//		println("Obj: " + obj)
//		def pyObject = Py.java2py(ctx);
//		
//		def cls1 = MyTestClass.class
//		
//		try{
//		for(cls in cls1.getInterfaces()){
//			println("interface: " + cls)
//			println("cls: " + cls.getClasses())
//		}}
//		catch(Throwable t){
//			t.printStackTrace()
//		}
		
	}

}
