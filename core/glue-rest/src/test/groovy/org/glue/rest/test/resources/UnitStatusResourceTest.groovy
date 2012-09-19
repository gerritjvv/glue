package org.glue.rest.test.resources;

import static org.junit.Assert.*

import org.glue.rest.resources.UnitStatusResource
import org.glue.rest.util.MockGlueExecutor
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.exec.GlueState
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.repo.impl.MapGlueUnitRepository
import org.glue.unit.status.ProcessStatus
import org.glue.unit.status.UnitStatus
import org.glue.unit.status.impl.MapUnitStatusManager
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.node.TextNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.restlet.Request
import org.restlet.data.Method
import org.restlet.representation.Representation

/**
 * 
 * Tests the results returned by the UnitStatusResource
 *
 */
class UnitStatusResourceTest {

	static final ObjectMapper mapper = new ObjectMapper()

	MapUnitStatusManager statusManager
	MapGlueUnitRepository repo
	GlueExecutor exec

	GlueUnit unit

	UnitStatus unitStatus
	Map<String, ProcessStatus> processStatusMap = [:]

	/**
	 * Tests the response when the unit id cannot be found
	 */
	@Test
	public void testUnitNotFound(){


		UnitStatusResource resource = new UnitStatusResource(exec, statusManager, repo)

		Request request = new Request()

		request.setMethod(Method.GET)
		request.setAttributes([unitId:'doesnotexist'])
		request.setResourceRef("status")

		resource.setRequest request

		Representation resp = resource.get()


		assertNotNull(resp)

		JsonNode node = mapper.readTree(resp.text)
		assertNotNull(node.get('error'))
	}

	/**
	 * Test the response returned by UnitStatusResource when all when ok.
	 */
	@Test
	public void testGetStatus(){


		UnitStatusResource resource = new UnitStatusResource(exec, statusManager, repo)

		Request request = new Request()

		request.setMethod(Method.GET)
		request.setAttributes([unitId:unitStatus.unitId])
		request.setResourceRef("status")

		resource.setRequest request

		Representation resp = resource.get()

		assertNotNull(resp)

		JsonNode node = mapper.readTree(resp.text)

		//check unit status
		assertEquals(unitStatus.unitId, node.get('unitId').getValueAsText())
		assertEquals(unitStatus.name, node.get('name').getValueAsText())
		assertEquals(unitStatus.status, GlueState.valueOf(node.get('status').getValueAsText()))
		assertEquals(unitStatus.progress, node.get('progress').getNumberValue().doubleValue(), 1)

		//check processes
		JsonNode processes = node.get('processes')
		assertNotNull(processes)

		for(JsonNode processNode : processes){

			//check the process status
			String processName = processNode.get('processName').getValueAsText()
			ProcessStatus processStatus = processStatusMap[processName]

			assertNotNull(processStatus)

			assertEquals(processStatus.unitId, node.get('unitId').getValueAsText())
			assertEquals(processStatus.status, GlueState.valueOf(node.get('status').getValueAsText()))
			assertEquals(processStatus.progress, node.get('progress').getNumberValue().doubleValue(), 1)

			//check the dependencies
			GlueProcess glueProcess = unit.processes[processName]

			JsonNode dependencies = processNode.get('dependencies')
			if(glueProcess.dependencies){
				assertNotNull(dependencies)
				assertTrue(dependencies.isArray())

				//set up a quick set of the dependencies in the json
				Set dependencySet = []
				for(TextNode item in dependencies){
					dependencySet << item.getValueAsText()
				}

				glueProcess.dependencies.each { String depName ->
					//we check that each dependency in the glue process definition
					//is contains in the dependencies string returned

					assertTrue(dependencySet.contains(depName))


				}
			}
		}
	}

	@Before
	public void setUp() throws Exception {

		statusManager = new MapUnitStatusManager()

		repo = new MapGlueUnitRepository()

		exec = new MockGlueExecutor()

		unit = new DefaultGlueUnitBuilder().build("""
		
		  name='test1'
		  tasks{
		    pA{
		      tasks={
		        println 'Hi'
		      }
		    }
		    pB{
		      dependencies='pA'
		      tasks={
		        println 'Hi'
		      }
		    }
		  }
		
		""")

		repo << unit

		//set the status
		String unitId = String.valueOf(System.currentTimeMillis())

		unitStatus = new UnitStatus(
				unitId:unitId,
				name:unit.name,
				startDate:new Date(),
				endDate:new Date(),
				status:GlueState.RUNNING,
				progress:0D
				)

		statusManager.setUnitStatus unitStatus

		//for each process in the glue unit set the process status
		unit.processes.each { String name, GlueProcess process ->

			ProcessStatus processStatus = new ProcessStatus(
					unitId:unitId,
					processName:name,
					startDate:new Date(),
					endDate:new Date(),
					status:GlueState.RUNNING,
					error:'',
					progress:0D
					)
			processStatusMap[name] = processStatus
			statusManager.setProcessStatus processStatus
		}
	}

	@After
	public void tearDown() throws Exception {
		statusManager.destroy()
	}
}
