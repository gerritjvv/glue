package org.glue.rest.test.resources;

import static org.junit.Assert.*

import org.glue.rest.resources.StatusResource
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
 * Tests the results returned by the StatusResource
 *
 */
class StatusResourceTest {

	static final ObjectMapper mapper = new ObjectMapper()

	MapUnitStatusManager statusManager
	MapGlueUnitRepository repo

	GlueUnit unit

	/*
	 * Contains a mapping of unitId to unit status instance created in setup and 
	 * set to the status manager
	 */
	Map<String, UnitStatus> statusMap = [:]

	/**
	 * Tests the response when there are not items in the status manager
	 */
	@Test
	public void testEmptyStatusManager(){

		MapUnitStatusManager emptyStatusManager = new MapUnitStatusManager()
		StatusResource resource = new StatusResource(emptyStatusManager, repo)

		Request request = new Request()

		request.setMethod(Method.GET)
		request.setResourceRef("status")

		resource.setRequest request
		Representation resp = resource.get()

		assertNotNull(resp)

		JsonNode node = mapper.readTree(resp.text)
		assertEquals(0, node.size())
	}

	/**
	 * Test the response returned by UnitStatusResource when all when ok.
	 */
	@Test
	public void testGetStatus(){

		//create status resource
		StatusResource resource = new StatusResource(statusManager, repo)

		//perform get request
		//no parameters are required
		Request request = new Request()

		request.setMethod(Method.GET)
		resource.setRequest request

		//we expect a response, default behaviour is to return
		//all status instances from today to five days ago.
		Representation resp = resource.get()
		assertNotNull(resp)

		JsonNode node = mapper.readTree(resp.text)

		//for each status check that all values are the same
		//as those set to the status manager
		int counter = 0
		node.each{ JsonNode statusNode ->
			counter++
			//get the status instance using the unitId
			assertNotNull(statusNode.unitId)
			UnitStatus unitStatus = statusMap[statusNode.unitId.getValueAsText()]
			assertEquals(unitStatus.name, statusNode.name?.getValueAsText())
			assertEquals(unitStatus.startDate.getTime(),
					Long.valueOf(statusNode.startDate?.getValueAsText()))
			assertEquals(unitStatus.endDate.getTime(),
					Long.valueOf(statusNode.endDate?.getValueAsText()))
			assertEquals(unitStatus.status,
					GlueState.valueOf(statusNode.status?.getValueAsText()))
			assertEquals(unitStatus.progress,
					Double.valueOf(statusNode.progress?.getValueAsText()), 1)
		}

		assertEquals(statusMap.size(), counter)
	}

	@Before
	public void setUp() throws Exception {

		statusManager = new MapUnitStatusManager()

		repo = new MapGlueUnitRepository()


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
		(1..10).each{
			String unitId = String.valueOf(System.currentTimeMillis())

			UnitStatus unitStatus = new UnitStatus(
					unitId:unitId,
					name:unit.name,
					startDate:new Date(),
					endDate:new Date(),
					status:GlueState.RUNNING,
					progress:0D
					)

			statusManager.setUnitStatus unitStatus
			statusMap[unitId] = unitStatus
		}
	}

	@After
	public void tearDown() throws Exception {
		statusManager.destroy()
	}
}
