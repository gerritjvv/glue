package org.glue.unit.repo.impl

import java.util.List;

import jmockmongo.MockMongo;

import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.junit.AfterClass;
import org.junit.BeforeClass
import org.junit.Test

import com.lordofthejars.nosqlunit.mongodb.InMemoryMongoDb

/**
 * 
 * Test the workings of the MongoDBGlueUnitRepository
 *
 */

class TestMongoDBRepository {
	
	static MockMongo mockMongo;
	
	@Test
	public void testInsert(){
		//Host 0.0.0.0 Port 2307
		//String userName, char[] pwd, String db, String collection, List<String> servers
		/*
		def repo = new MongoDBGlueUnitRepository(new DefaultGlueUnitBuilder(),
			new MongoDBGlueUnitRepository.Config(null, null, "testdb", "testcoll", 
				["localhost:2307"]))
		
		println "Test: " + repo.size()
		Thread.sleep(1000)
		*/
	}
	
	
	
	@BeforeClass
	public static void startup() throws Throwable{
//		mockMongo = new MockMongo()
//		mockMongo.start()
		
	}
	
	@AfterClass
	public static void shutdown() throws Throwable{
//		mockMongo.stop()
	}
	
}
