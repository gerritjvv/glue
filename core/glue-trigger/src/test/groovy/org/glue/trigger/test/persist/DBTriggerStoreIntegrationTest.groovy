package org.glue.trigger.test.persist;


import static org.junit.Assert.*

import org.glue.trigger.persist.CheckPoint
import org.glue.trigger.persist.db.DBTriggerStore
import org.glue.trigger.persist.db.TriggersFileEntity
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * 
 * Check that the TriggerStoreModule's methods for handling checkpoints and files i.e. ready and processed files correctly.
 *
 */
class DBTriggerStoreIntegrationTest {
	
	DBTriggerStore module
	
	
	/**
	 * Check that we can delete files
	 */
	@Test
	public void testDeleteFiles(){
		
		String unitName = "testDeleteFiles"
		
		def paths = []
		
		int total = 100
		(1..total).each {
			String path = "testDeleteFiles/testpath/$it"
			paths << path
			module.markFileAsReady(unitName, path);
		}
		
		paths.each { String path -> module.deleteFile(unitName, path)}
		
		//check that all files were deleted
		int i = 0
		module.listAllFiles unitName, { String n, status, String path ->
			i++
		}
		
		assertEquals 0, i
	}
	
	/**
	 * Check that we can mark files as ready, mark them as processed and then remark them as ready.
	 */
	@Test
	public void testListAllFiles(){
		
		String unitName = "testListAllFiles"
		String paths = []
		
		int total = 100
		(1..total).each {
			String path = "testListAllFiles/testpath/$it" 
			paths << path
			module.markFileAsReady(unitName, path);
		}
		
		int i = 0
		module.listAllFiles unitName, { String n, status, String path ->
			assertEquals unitName, n
			assertEquals status, TriggersFileEntity.STATUS.READY.toString()
			i++
		}
		
		assertEquals total, i
	}
	
	
	@Test
	public void testListReadyLockUnLock(){
		
		String unitName = "testListReadyLockUnLock"
		String path = "testListReadyLockUnLock/testpath"
		String unitId = '222'
		//check mark as ready
		module.markFileAsReady unitName, path
		
		int count = 0
		module.listReadyFiles( unitName, { f ->
				count = count + 1
				println "Locking file $f"
		}, true)
		
			
		assertEquals 1, count
		
		module.markFileAsProcessed(unitName, '123', path)
		//here the file should be locked and we should not get any more ready files
		int secondcount = 0
		module.listReadyFiles(unitName, { f ->
				secondcount++				
		}, true)
		
		assertEquals(0, secondcount)
	
	}
	
	/**
	 * Check that we can mark files as processed, and retrieve them
	 */
	@Test
	public void testMarkFileAsProcessed(){
		
		String unitName = "testMarkFileAsProcessed"
		String path = "testMarkFileAsProcessed/testpath"
		
		//if the file does not exist it will be created.
		module.markFileAsProcessed unitName, '123', path
		
		//check that the file exists
		String foundUnitName, foundPath
		
		module.listProcessedFiles unitName, { String u, String p -> foundUnitName = u; foundPath = p;}
		
		assertEquals( unitName, foundUnitName )
		assertEquals( path, foundPath )
		
		//ensure that this file for the unit does not exist in read
		module.listReadyFiles unitName, { String u, String p -> assertNotSame(path, p)}
	}
	
	
	/**
	 * Check that we can mark files as ready, and retrieve them
	 */
	@Test
	public void testMarkFileAsReady(){
		
		String unitName = "testMarkFileAdReady" 
		String path = "testMarkFileAdReady/testpath"
		
		module.markFileAsReady unitName, path
		
		//check that the file exists
		String foundUnitName, foundPath
		
		module.listReadyFiles unitName, { String u, String p -> foundUnitName = u; foundPath = p;}
		
		assertEquals( unitName, foundUnitName )
		assertEquals( path, foundPath )
	}
	
	/**
	* Check that we can mark files as ready, and retrieve them
	*/
   @Test
   public void testMarkFilesAsReady(){
	   
	   String unitName = "testMarkFilesAdReady"
	   Collection<String> paths = ["testMarkFilesAsReady/testpath", "testMarkFilesAsReady/testpath2"]
	   
	   module.markFilesAsReady unitName, paths
	   
	   //check that the file exists
	   String foundUnitName
	   Collection<String> foundPaths = []
	   
	   module.listReadyFiles unitName, { String u, String p -> println "!!!!!!!!!!!!!1Found File $u $p";foundUnitName = u; foundPaths << p;}
	   
	   assertEquals( unitName, foundUnitName )
	   assertEquals( paths.sort(), foundPaths.sort() )
   }
   
   
	/**
	 * Check that we get null if we do a get checkpoint when none exists
	 */
	@Test
	public void testGetNullCheckpoint(){
		
		String unitName = "testGetNullCheckpoint"
		
		def checkpoint = module.getCheckPoint(unitName, 'test')
		assertEquals(0, checkpoint.fileCount)
	}
	
	
	/**
	 * Check that we can persist and retrieve a checkpoint
	 */
	@Test
	public void testStoreCheckPoint(){
		String unitName = "testStoreCheckPoint"
		
		Date checkpoint = new Date()
		String path = "testPath"
		
		module.storeCheckPoint unitName, path, new CheckPoint(date:checkpoint)
		
		//test that we can retrieve the same checkpoint
		def foundCheckpoint = module.getCheckPoint(unitName, path)
		
		assertNotNull(checkpoint)
		assertEquals checkpoint, foundCheckpoint.date
		
	}
	
	/**
	 * Test that we can save and retrieve multiple checkpoints for a unit
	 */
	@Test
	public void testGetCheckpoints(){
		
		String unitName = "testGetCheckpoints"
		
		//create checkpoints with paths
		Map<String, CheckPoint> checkpoints = [:]
		(1..10).each { int index -> checkpoints["$index"] = new CheckPoint(date:new Date())	}
		
		//persist checkpoints
		checkpoints.each { String path, CheckPoint checkpoint -> module.storeCheckPoint(unitName, path, checkpoint) }
		
		Map<String, CheckPoint> foundCheckpoints = module.getCheckPoints(unitName)
		
		assertNotNull( foundCheckpoints )
		assertTrue( foundCheckpoints.size() > 0)
		
		foundCheckpoints?.each { String path, CheckPoint checkpoint ->
			CheckPoint checkpointOrign = checkpoints[path]
			
			assertNotNull( checkpointOrign )
			assertEquals(checkpointOrign.date.getTime(), checkpoint.date.getTime() )
		}
	}
	
	/**
	 * Check that we can persist and retrieve a checkpoint
	 */
	@Test
	public void testRemoveCheckpoint(){
		
		String unitName = "testRemoveCheckpoint"
		
		Date checkpoint = new Date()
		String path = "testPath"
		
		module.storeCheckPoint unitName, path, new CheckPoint(date:checkpoint, fileCount:10)
		
		//test that we can retrieve the same checkpoint
		CheckPoint foundCheckpoint = module.getCheckPoint(unitName, path)
		
		assertNotNull(checkpoint)
		assertEquals checkpoint, foundCheckpoint.date
		
		//check that when we delete we cannot retrieve the checkpoint anymore
		module.removeCheckPoint unitName, path
		
		foundCheckpoint = module.getCheckPoint(unitName, path)
		println "FoundCheckPoint: $foundCheckpoint"
		assertEquals(0, foundCheckpoint.fileCount)
		
		
	}
	
	
	@Before
	public void setup(){
		
		ConfigObject config = new ConfigSlurper().parse("""
		
				connection.username="sa"
				connection.password=""
				dialect="org.hibernate.dialect.HSQLDialect"
				connection.driver_class="org.hsqldb.jdbcDriver"
				connection.url="jdbc:hsqldb:mem:triggerStoreModuleTest"
				hbm2ddl.auto="create"
				connection.autocommit="false"
				show_sql="false"
				cache.use_second_level_cache="false"
				cache.provider_class="org.hibernate.cache.NoCacheProvider"
				cache.use_query_cache="false"
				zkhost="localhost:3001"
				zktimeout="20000"
		
		""")
		
		
		
		
		module = new DBTriggerStore();
		module.init config
	}
	
	@After
	public void shutdown(){
		
		module.shutdown()
	}
}
