package org.glue.unit.exec.impl;

import static org.junit.Assert.*

import org.glue.unit.exec.CyclicDependencyException
import org.glue.unit.exec.ExecNode
import org.glue.unit.exec.UndefinedProcessException;
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueUnitBuilder;
import org.glue.unit.om.impl.GlueModuleFactoryImpl
import org.glue.unit.om.impl.GlueUnitImpl
import org.junit.Test

/**
 *
 * Tests that the graph builder detects cyclic dependencies, and builds the correct graph.
 *
 */
class ExecutionGraphBuilderTest {

	/**
	* Test simple cyclic dependency A depends on B, B depends on A.
	*/
   @Test(expected=UndefinedProcessException)
   public void testUndefinedProcessException(){

	   def unitTxt = '''
		  name='test'
		  tasks{
			pA{
			  dependencies='pB'
			  tasks = {}
			}
			pB{
			  dependencies='pC'
			  tasks = {}
			}
		  }
	   '''


	   GlueUnit unit = createGlueUnit(unitTxt)

	   ExecutionGraphBuilder.getInstance().build(unit)

   }
	/**
	 * Test simple cyclic dependency A depends on B, B depends on A.
	 */
	@Test(expected=CyclicDependencyException)
	public void testCyclic1Dependency(){

		def unitTxt = '''
		   name='test'
		   tasks{
			 pA{
			   dependencies='pB'
			   tasks = {}
			 }
			 pB{
			   dependencies='pA'
			   tasks = {}
			 }
		   }
		'''


		GlueUnit unit = createGlueUnit(unitTxt)

		//expect cyclic dependency
		ExecutionGraphBuilder.getInstance().build(unit)

	}

	/**
	 * Tests a complex cyclic dependency.<br/> 
	 */
	@Test(expected=CyclicDependencyException)
	public void testCyclic2Dependency(){

		def unitTxt = '''
		   name='test'
		   tasks{
			 pA{
			   dependencies='pE'
			   tasks = {}
			 }
			 pB{
			   dependencies='pA'
			   tasks = {}
			 }
			 pC{
			   dependencies='pB'
			   tasks = {}
			 }
			 pD{
			   dependencies='pC'
			   tasks = {}
			 }
			 pE{
			   dependencies='pD'
			   tasks = {}
			 }
		   }
		'''


		GlueUnit unit = createGlueUnit(unitTxt)

		//expect cyclic dependency
		ExecutionGraphBuilder.getInstance().build(unit)


	}


	/**
	 * Test that the building process is done correctly.
	 */
	@Test
	public void testBuild(){

		def unitTxt = '''
		   name='test'
		   tasks{
		     pA{
		       tasks = {}
		     }
		     pB{
		       dependencies='pA'
		       tasks = {}
		     }
		   }
		'''


		GlueUnit unit = createGlueUnit(unitTxt)

		Map<String, ExecNode> execNodeMap = ExecutionGraphBuilder.getInstance().build(unit)

		assertEquals(2, execNodeMap.size())
		assertEquals('pA', execNodeMap['pA']?.name)
		assertNotNull(execNodeMap['pA']?.glueProcess)
		assertEquals('pA', execNodeMap['pA']?.glueProcess?.name)
		assertEquals('pB', execNodeMap['pB']?.name)
		assertEquals('pB', execNodeMap['pB']?.glueProcess?.name)
		
	}

	private GlueUnit createGlueUnit(String unitTxt){
		new DefaultGlueUnitBuilder().build unitTxt
	}
}
