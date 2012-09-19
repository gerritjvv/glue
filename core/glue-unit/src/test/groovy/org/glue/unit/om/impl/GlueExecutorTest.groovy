package org.glue.unit.om.impl

import static org.junit.Assert.*

import org.glue.unit.exceptions.UnitValidationException
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.GlueState
import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.exec.impl.GlueExecutorImpl
import org.glue.unit.exec.impl.MockProcessExecutorProvider
import org.glue.unit.exec.impl.ProcessExecutorImpl
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.Provider
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.repo.impl.DirGlueUnitRepository
import org.apache.log4j.Logger
import org.junit.Test

class GlueExecutorTest {
	static final Logger log = Logger.getLogger(GlueExecutorTest.class)

	/**
	 * We test that a glue unit with required modules not found in the module factory is validated and an exception thrown.
	 */
	@Test(expected=UnitValidationException)
	public void testUnitExecutorFailRequiredModules() {

		GlueModuleFactory moduleFactory = new GlueModuleFactoryImpl();

		ConfigObject execConfig = new ConfigObject();
		//create a workflow that will never complete
		ConfigObject unitConfig =  new ConfigSlurper().parse("""
		   name='test'
		   requiredModules='hdfs,myModule'
		   tasks{
		   
		      pA{
		       tasks={ while(true){Thread.sleep(1000);}}
		      }
		   
		   	  pB{
		       tasks={ while(true){Thread.sleep(1000);}}
		      }
		   }
		
		""")

		Provider<ProcessExecutor> processExecutorProvider = new MockProcessExecutorProvider(errorInExec:false, processExecutorClosure:{ new ProcessExecutorImpl() })

		GlueUnitRepository repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			'src/test/resources/test-flow-repo'
		])
		Provider<UnitExecutor> unitExecutorProvider = new MockUnitExecutorProvider(processExecutorProvider:processExecutorProvider)

		GlueExecutor exec = new GlueExecutorImpl(execConfig, repo,
				new DefaultGlueContextBuilder(new MapGlueModuleFactoryProvider(null)),
				unitExecutorProvider,
				new DefaultGlueUnitBuilder(),
				new DefaultGlueUnitValidator()
				)

		//we expect a validation exception here
		exec.submitUnitAsConfig(unitConfig, ['it':'1'])

	}

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

		String uid=exec.submitUnitAsName( "testflow", [:] )

		exec.waitFor uid

		assertEquals(GlueState.FINISHED, exec.getStatus(uid))
	}


	@Test
	public void testUnitExecutor() {

		GlueModuleFactory moduleFactory = new GlueModuleFactoryImpl();

		ConfigObject execConfig = new ConfigObject();
		ConfigObject unitConfig =  getUnit();

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


		String uid=exec.submitUnitAsConfig(unitConfig, ['it':'1'])

		while(exec.getStatus(uid)!=GlueState.FINISHED && exec.getStatus(uid)!=GlueState.FAILED) {
			log.info "Progress: ${exec.getProgress(uid)}"
			Thread.sleep(100)
		}
	}

	/**
	 * We run two processes that will loop forever then call terminate on the executor to test<br/>
	 * that the process can terminate.<br/>
	 * We set a timeout of 5 seconds so that if terminate does not work this test fails in 5 seconds.<br/>
	 */
	@Test(timeout=5000L)
	public void testUnitExecutorTerminateAll() {

		GlueModuleFactory moduleFactory = new GlueModuleFactoryImpl();

		ConfigObject execConfig = new ConfigObject();
		//create a workflow that will never complete
		ConfigObject unitConfig =  new ConfigSlurper().parse("""
		   name='test'
		   tasks{
		   
		      pA{
		       tasks={ while(true){Thread.sleep(1000);}}
		      }
		   
		   	  pB{
		       tasks={ while(true){Thread.sleep(1000);}}
		      }
		   }
		
		""")

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


		String uid=exec.submitUnitAsConfig(unitConfig, ['it':'1'])

		Thread.sleep(1000L)

		exec.terminate uid

		assertEquals(GlueState.FINISHED.toString(), exec.getStatus(uid).toString() )
	}

	/**
	 * Returns a ConfigObject representing a GlueUnit for execution
	 * @return
	 */
	private ConfigObject getUnit(){
		new ConfigSlurper().parse('''
		name="test"
			tasks{
			process1{
				tasks = { context ->
					println "one, sleeping 2 secs"
					context.val = "greetings from process1, args is ${context.args.it}, unitId is ${context.unitId}"
					Thread.sleep 2
					
				}
				
				success = { context ->
				println "one finished"
				}
				
			}
			
			process2{
				dependencies="process1,process3"
				tasks = { context ->
					println "two"
				}
				
				error = { context, t ->
					println "two error"
					t.printStackTrace()
				}
				
				success = { context ->
					println "two success"
				}
			}

		process3{
			tasks= { context -> println "This process3 gonna run for 7 seconds"; Thread.sleep 7;
				}
		success= { context -> println "ok, process3 is finished now, you can run all the rest"}
			
		}

		process4{
		dependencies= "process2, process3"
				tasks = { context -> println "This one will run at last. ${context.val}" }
			}
		}
		''')
	}
}
