package org.glue.rest.test.resources;


import static org.junit.Assert.*

import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.node.TextNode
import org.glue.rest.resources.ProcessStatusResource
import org.glue.unit.exec.GlueState
import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.log.impl.DefaultGlueExecLoggerProvider
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.om.impl.MapGlueModuleFactoryProvider
import org.glue.unit.repo.impl.MapGlueUnitRepository
import org.glue.unit.status.ProcessStatus
import org.glue.unit.status.UnitStatus
import org.glue.unit.status.impl.MapUnitStatusManager
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
class ProcessStatusResourceTest {

	static final ObjectMapper mapper = new ObjectMapper()

	MapUnitStatusManager statusManager
	MapGlueUnitRepository repo
	GlueModuleFactoryProvider moduleFactory
	GlueExecLoggerProvider logProvider
	
	GlueUnit unit

	UnitStatus unitStatus
	Map<String, ProcessStatus> processStatusMap = [:]

	/**
	 * Tests the response when the unit id cannot be found
	 */
	@Test
	public void testUnitIdNotFound(){
		
		ProcessStatusResource resource = new ProcessStatusResource(statusManager, repo, moduleFactory, logProvider)

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
	 * Tests the response when the process name cannot be found
	 */
	@Test
	public void testProcessNameNotFound(){

		ProcessStatusResource resource = new ProcessStatusResource(statusManager, repo, moduleFactory, logProvider)

		Request request = new Request()

		request.setMethod(Method.GET)
		request.setAttributes([unitId:unitStatus.unitId, processName:'doesnotexist'])
		request.setResourceRef("status")

		resource.setRequest request

		Representation resp = resource.get()


		assertNotNull(resp)

		JsonNode node = mapper.readTree(resp.text)
		assertNotNull(node.get('error'))
	}

	/**
	 * Tests if we query a process for which its status has not been set.
	 * We expect the status to be WAITING
	 */
	@Test
	public void testGetStatusNoProcessStatusSet(){

		//We use process pB which has one dependency on process pB
		String processName = 'pC'
		ProcessStatus processStatus = processStatusMap[processName]

		ProcessStatusResource resource = new ProcessStatusResource(statusManager, repo, moduleFactory, logProvider)

		Request request = new Request()

		request.setMethod(Method.GET)
		request.setAttributes([unitId:unitStatus.unitId, processName:processName])

		resource.setRequest request

		Representation resp = resource.get()

		assertNotNull(resp)

		JsonNode node = mapper.readTree(resp.text)

		//check process status
		assertEquals(unitStatus.unitId, node.get('unitId').getValueAsText())
		assertEquals(processName, node.get('processName').getValueAsText())
		assertEquals(GlueState.WAITING, GlueState.valueOf(node.get('status').getValueAsText()))
		assertEquals(processStatus.progress, node.get('progress').getNumberValue().doubleValue(), 1)

		//check depdenencies
		JsonNode dependencies = node.get('dependencies')

		assertNotNull(dependencies)
		assertTrue(dependencies.isArray())

		//set up a quick set of the dependencies in the json
		Set dependencySet = []
		for(TextNode item in dependencies){
			dependencySet << item.getValueAsText()
		}

		GlueProcess glueProcess = unit.processes[processName]

		glueProcess.dependencies.each { String depName ->
			//we check that each dependency in the glue process definition
			//is contains in the dependencies string returned
			assertTrue(dependencySet.contains(depName))
		}
	}

	/**
	 * Tests when the process status is set
	 */
	@Test
	public void testGetStatusProcessStatusSet(){

		//We use process pB which has one dependency on process pA
		String processName = 'pB'
		ProcessStatus processStatus = processStatusMap[processName]

		processStatus.progress = 1D
		processStatus.status = GlueState.RUNNING

		//we set the new process status
		statusManager.setProcessStatus processStatus


		ProcessStatusResource resource = new ProcessStatusResource(statusManager, repo, moduleFactory, logProvider)

		Request request = new Request()

		request.setMethod(Method.GET)
		request.setAttributes([unitId:unitStatus.unitId, processName:processName])

		resource.setRequest request

		Representation resp = resource.get()

		assertNotNull(resp)

		JsonNode node = mapper.readTree(resp.text)

		//check process status
		assertEquals(unitStatus.unitId, node.get('unitId').getValueAsText())
		assertEquals(processName, node.get('processName').getValueAsText())
		assertEquals(GlueState.RUNNING, GlueState.valueOf(node.get('status').getValueAsText()))
		assertEquals(processStatus.progress, node.get('progress').getNumberValue().doubleValue(), 1)

		//check depdenencies
		JsonNode dependencies = node.get('dependencies')

		assertNotNull(dependencies)
		assertTrue(dependencies.isArray())

		//set up a quick set of the dependencies in the json
		Set dependencySet = []
		for(TextNode item in dependencies){
			dependencySet << item.getValueAsText()
		}

		GlueProcess glueProcess = unit.processes[processName]

		glueProcess.dependencies.each { String depName ->
			//we check that each dependency in the glue process definition
			//is contains in the dependencies string returned
			assertTrue(dependencySet.contains(depName))
		}
	}

	@Before
	public void setUp() throws Exception {

		File logDir = new File("target/tests/ProcessStatusResourceTest/logs")
		logDir.mkdirs()
		
		logProvider = new DefaultGlueExecLoggerProvider(logDir)
		
		statusManager = new MapUnitStatusManager()

		repo = new MapGlueUnitRepository()

		moduleFactory = new MapGlueModuleFactoryProvider(null)
		
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
		    pC{
		      dependencies='pB'
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
				status:GlueState.WAITING,
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
					status:GlueState.WAITING,
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
