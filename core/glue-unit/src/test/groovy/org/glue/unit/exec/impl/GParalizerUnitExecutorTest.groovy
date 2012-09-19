package org.glue.unit.exec.impl


import static org.junit.Assert.*

import org.glue.unit.exec.GlueState
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.om.impl.GlueContextImpl
import org.glue.unit.om.impl.GlueModuleFactoryImpl
import org.glue.unit.om.impl.MapGlueModuleFactoryProvider
import org.junit.Test

/**
 * 
 * Test that the GRParalizerUnitExector works as expected.
 *
 */
class GParalizerUnitExecutorTest {
	
	
    /**
     * Test behaviour during exception for what ever reason in the glue unit closure
     */
    @Test
    void testUnitExecutionWithExceptionGlueProcess(){
        GlueModuleFactory glueModuleFactory = new GlueModuleFactoryImpl()
		
        GlueContext context = new GlueContextImpl(moduleFactory:glueModuleFactory)
		
        GlueUnit unit = createUnit("""
	   
		   name='Test1'
		   tasks{
			  pA{
				tasks={
				  throw new RuntimeException("Induced Error")
				}
			  }
			  pB{
				dependencies='pA'
				tasks={
				  println "pB"
				}
			  }
		   }
	   
	   """)
		
		
        def processExecutorProvider = new MockProcessExecutorProvider(errorInExec:false, processExecutorClosure:{ new ProcessExecutorImpl() })
		
        //Create executor
        GParallizerUnitExecutor exec = new GParallizerUnitExecutor(
            processExecutorProvider
        )
		
        exec.init unit, context
        //Exec all processes
        exec.execute()
		
        exec.waitFor()
		
        //check that the status is failed
        assertEquals( GlueState.FAILED, exec.status)
		
        //check that only 1 error was thrown
        assertEquals(1, exec.errors.size() )
		
        //Test that processes pA, and pB did execute
        assertEquals( 2, exec.getProcessExecutors().size())
        assertNotNull(exec.getProcessExecutors()['pA'])
        assertNotNull(exec.getProcessExecutors()['pB'])
		
    }
	
    /**
     * Test behaviour during exception for what ever reason in the ProcessExecutor
     */
    @Test
    void testUnitExecutionWithExceptionInProcessExecutor(){
        GlueModuleFactory glueModuleFactory = new GlueModuleFactoryImpl()
		
        GlueContext context = new GlueContextImpl()
        GlueUnit unit = createUnit("""
	   
		   name='Test1'
		   tasks{
			  pA{
				tasks={
				  println "pA"
				}
			  }
			  pB{
				dependencies='pA'
				tasks={
				  println "pB"
				}
			  }
		   }
	   
	   """)
		
		
        def processExecutorProvider = new MockProcessExecutorProvider(errorInExec:true)
		
        //Create executor
        GParallizerUnitExecutor exec = new GParallizerUnitExecutor(
            processExecutorProvider
        )
		
        exec.init unit, context
		
        //Exec all processes
        exec.execute()
        exec.waitFor()
		
        assertEquals(GlueState.FAILED, exec.status)
		
        assertEquals(1, exec.errors.size() )
		
        //Test that processes pA, and pB did execute
        assertEquals( 2, exec.getProcessExecutors().size())
        assertNotNull(exec.getProcessExecutors()['pA'])
        assertNotNull(exec.getProcessExecutors()['pB'])
		
        assertEquals(1, ((MockProcessExecutor)exec.getProcessExecutors()['pA']).execCount)
        //we test that pB was not executed
        assertEquals(0, ((MockProcessExecutor)exec.getProcessExecutors()['pB']).execCount)
		
    }
	
	
    /**
     * Test that normal processes can execution with out problems.
     */
    @Test
    void testUnitExecution(){
		
        GlueModuleFactory glueModuleFactory = new GlueModuleFactoryImpl()
		
        GlueContext context = new GlueContextImpl(moduleFactory:glueModuleFactory)
        GlueUnit unit = createUnit("""
		
			name='Test1'
			tasks{
			   pA{
			     tasks={
			       println "pA"
			     }
			   }
			   pB{
			     dependencies='pA'
			     tasks={
			       println "pB"
			     }
			   }
			}
		
		""")
		
		
        def processExecutorProvider = new MockProcessExecutorProvider(errorInExec:false)
		
        //Create executor
        GParallizerUnitExecutor exec = new GParallizerUnitExecutor(
            processExecutorProvider
        )
		
        exec.init unit, context
		
        //Exec all processes
        exec.execute()
        exec.waitFor()
		
        assertEquals(exec.status, GlueState.FINISHED)
        //Test that processes pA, and pB did execute
        assertEquals( 2, exec.getProcessExecutors().size())
        assertNotNull(exec.getProcessExecutors()['pA'])
        assertNotNull(exec.getProcessExecutors()['pB'])
		
        assertEquals(1, ((MockProcessExecutor)exec.getProcessExecutors()['pA']).execCount)
        assertEquals(1, ((MockProcessExecutor)exec.getProcessExecutors()['pB']).execCount)
		
    }


    /**
     *
     */
    @Test
    void testUnitExecutionWithProcessDependencyFail(){
        GlueModuleFactory glueModuleFactory = new GlueModuleFactoryImpl()

        GlueContext context = new GlueContextImpl()
        GlueUnit unit = createUnit("""

	    name="test"
            tasks{
                A{
                    dependencies="B"
                    tasks = { context -> println "Process A" }
                    success = { context -> println "Process A is finished successfully" }
                }

                B{
                    dependencies="C"
                    tasks = { context -> println "Process B" }
                    success = { context -> println "Process B is finished successfully" }
                }

                C{
                    tasks= { context -> println "Process C"; throw new Exception("Causing a fail") }
                    success= { context ->  println "Process C finished succesfully" }
                }

            }

	   """)


        def processExecutorProvider = new MockProcessExecutorProvider(errorInExec:true)

        //Create executor
        GParallizerUnitExecutor exec = new GParallizerUnitExecutor(
            processExecutorProvider
        )

        exec.init unit, context

        //Exec all processes
        exec.execute()
        exec.waitFor()

        assertEquals(GlueState.FAILED, exec.status)

        assertEquals(1, exec.errors.size() )


        assertEquals(3, exec.getProcessExecutors().size())
        assertNotNull(exec.getProcessExecutors()['A'])
        assertNotNull(exec.getProcessExecutors()['B'])
        assertNotNull(exec.getProcessExecutors()['C'])

        assertEquals(1, ((MockProcessExecutor)exec.getProcessExecutors()['C']).execCount)
        assertEquals(0, ((MockProcessExecutor)exec.getProcessExecutors()['B']).execCount)
        assertEquals(0, ((MockProcessExecutor)exec.getProcessExecutors()['A']).execCount)

    }
	
    GlueUnit createUnit(String unitDef){
        new DefaultGlueUnitBuilder().build(unitDef)
    }
}
