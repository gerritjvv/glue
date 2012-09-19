package org.glue.modules.test;

import static org.junit.Assert.*

import org.glue.modules.ClientModule
import org.glue.rest.Launcher
import org.glue.unit.exec.impl.WorkflowRunner
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class ClientModuleTest {

	
	static String execConfigPath='src/test/resources/conf/exec.groovy';
	static String moduleConfigPath="src/test/resources/conf/modules.groovy";
	static int port=8025;
	static String host = "localhost:$port";

	static Thread thread
	
	@Test
	public void testRunWorkflow(){
		
		println "----------- server ---------------"
		println """
			
			   server='$host'
			
			"""
		
//		WorkflowRunner.testMode = true
//		Launcher.testMode = true
//		
//		ClientModule clientModule = new ClientModule()
//		clientModule.init(
//			new ConfigSlurper().parse("""
//			
//			   server='$host'
//			
//			""")
//			)
//		
//		
//		boolean success = clientModule.start('test1', [:])
//		
//		assertTrue(success)
		
	}
	
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		//here we start a full blown work flow server
		
//		thread = Thread.start{
//			Launcher.main([
//				execConfigPath,
//				moduleConfigPath]
//			as String[])
//			
//		}
//		
//		//wait a 10 seconds for startup
//		Thread.sleep(10000)
	}
	

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

}
