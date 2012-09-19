package org.glue.unit.om.impl;

import static org.junit.Assert.*
import groovy.util.ConfigSlurper

import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.GlueState
import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.exec.impl.GParallizerUnitExecutor
import org.glue.unit.exec.impl.GlueExecutorImpl
import org.glue.unit.exec.impl.MockProcessExecutor;
import org.glue.unit.exec.impl.MockProcessExecutorProvider;
import org.glue.unit.exec.impl.ProcessExecutorImpl
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.Provider
import org.apache.log4j.Logger
import org.junit.Test

/**
 * Tests the GlueUnitParsing and a simple execution
 */
class GlueUnitImplTest {
	static final Logger log = Logger.getLogger(GlueUnitImplTest.class)

	@Test
	public void testGlueUnitParsing() {

		ConfigObject unitConfig =  new ConfigSlurper().parse('''
				name="test"
				triggers="hdfs:/myfile*"
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

		GlueModuleFactory mf = new GlueModuleFactoryImpl()
		GlueUnit unit = new DefaultGlueUnitBuilder().build(unitConfig)

		assertNotNull(unit.getTriggers())

		def triggers = unit.triggers

		assertEquals(1, triggers.size())
		assertEquals("hdfs", triggers[0].type)
		assertEquals("/myfile*", triggers[0].value)

		assertEquals("test", unit.name)

		assertEquals(4, unit.processes.size())
	}

	//@Test
	public void testGlueUnitImpl2() {
		log.info 'Start'

		GlueModuleFactory mf = new GlueModuleFactoryImpl()

		ConfigObject config =  new ConfigSlurper().parse('''
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
		def execList=[];
		for(i in 0..1){
			GlueUnit unit =  new DefaultGlueUnitBuilder().build(config)

			def processExecutorProvider = new MockProcessExecutorProvider(
					processExecutorClosure:{ // Set flag to induce error
						new ProcessExecutorImpl() }

					)

			//Create executor
			GParallizerUnitExecutor exec = new GParallizerUnitExecutor(
					processExecutorProvider
					)

			exec.init( unit, new DefaultGlueContextBuilder(mf).build("1", unit, ['it': i]))
			//Exec all processes

			log.info "Launching $i"
			exec.execute()

			execList.add exec
			Thread.sleep 3
		}

		boolean finished=false
		while( !finished) {
			finished=true;
			execList.each{ exec ->
				if(exec.getStatus()!=GlueState.FINISHED && exec.getStatus()!=GlueState.FAILED) finished=false
			}
			//log.info "Sleeping"
			Thread.sleep 1000
		}
		log.info 'Finished'
	}
}
