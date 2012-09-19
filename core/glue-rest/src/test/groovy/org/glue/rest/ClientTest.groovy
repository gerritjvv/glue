package org.glue.rest;

import static org.junit.Assert.*

import org.glue.rest.util.MockResource
import org.glue.rest.util.MockSubmitResource;
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.restlet.Component
import org.restlet.data.Protocol
import org.restlet.routing.Router

/**
 * 
 * This is not an integration test and uses a mock resource to catch and return results.<br/>
 * It verifies that the cli option parsing for the client is working correctly.
 *
 */
class ClientTest {

	static int testPort = 5079
	static Component component

	@Test
	public void testModules() {
		
		Client.error = null
		Client.response = null
		
		//tests that the submit command is accepted by client
		Client.main(['-modules'] as String[])
		
		assertNotNull(Client.response)
		assertNull(Client.error)
		
	}
	
	@Test
	public void testStop() {
		
		Client.error = null
		Client.response = null
		
		//tests that the submit command is accepted by client
		Client.main(['-stop'] as String[])
		
		assertNotNull(Client.response)
		assertNull(Client.error)
		
	}
	
	
	@Test
	public void testStatusWorkFlowNameProcessName() {
		
		Client.error = null
		Client.response = null
		
		//tests that the submit command is accepted by client
		Client.main(['-status', 'testworkflow', 'testProcess'] as String[])
		
		assertNotNull(Client.response)
		assertNull(Client.error)
		
	}
	
	@Test
	public void testStatusWorkFlowName() {
		
		Client.error = null
		Client.response = null
		
		//tests that the submit command is accepted by client
		Client.main(['-status', 'testworkflow'] as String[])
		
		assertNotNull(Client.response)
		assertNull(Client.error)
		
	}
	
	@Test
	public void testStatus() {
		
		Client.error = null
		Client.response = null
		
		//tests that the submit command is accepted by client
		Client.main(['-status'] as String[])
		
		assertNotNull(Client.response)
		assertNull(Client.error)
		
	}
	
	@Test
	public void testSubmit() {
		
		Client.error = null
		Client.response = null
		
		//tests that the submit command is accepted by client
		Client.main(['-submit', 'myUnit'] as String[])
		
		assertNotNull(Client.response)
		assertNull(Client.error)
		
	}

	@Test
	public void testSubmitNoName() {
		
		Client.error = null
		Client.response = null
		
		Client.main(['-submit'] as String[])
		assertNotNull(Client.error)
		
	}
	
	@AfterClass
	public static void shutdown() throws Exception{
		component.stop()
	}
	
	@BeforeClass
	public static void setup() throws Exception{

		Client.serverUrl = new URL("http://localhost:$testPort")
		
		ClientConfig config = ClientConfig.getInstance()
		
		Router router = new Router()
		
		router.attach('/stop', MockResource)
		router.attach('/submit', MockSubmitResource)
		router.attach('/shutdownResource', MockResource)
		router.attach('/status', MockResource)
		router.attach('/status/{unitId}', MockResource)
		router.attach('/status/{unitId}/{processName}', MockResource)
		router.attach('/modules', MockResource)
		
		component = new Component()
		component.getServers().add ( Protocol.HTTP, testPort);
		component.getDefaultHost().attach( router );
		component.start()
	}
}
