package org.glue.trigger.test.service.hdfs

import static org.junit.Assert.*
import groovy.util.ConfigObject

import org.apache.commons.io.FileUtils
import org.glue.trigger.persist.db.DBTriggerStore
import org.glue.trigger.service.hdfs.HdfsTriggerWorker
import org.glue.trigger.test.util.MockGlueExecutor
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.om.impl.GlueModuleFactoryImpl
import org.glue.unit.repo.impl.MapGlueUnitRepository
import org.junit.Test

/**
 *
 * Test the HdfsTriggerModule notifies glue units correctly.<br/>
 * <p/>
 * <b>Tests</b><br/>
 * <u>Group A Standard Notification</u><br/>
 * <ul>
 *   <li>(1) Create 2 Files same directory, 2 GlueUnits, test on startup units notified</li>
 *   <li>(2) Create 2 Files different directories, 1 GlueUnit for both directories, test on startup units notified</li>
 *   <li>(3) Create 2 Files different directories, 2 GlueUnit for both directories, test on startup units notified</li>
 * </ul>
 * <u>Group B Standard Notification and GlueUnit iterating through files</u><br/>
 * <ul>
 *   <li>(1) Create 2 Files same directory, 1 GlueUnits that will iterate through the files, test that all files are seend by the GlueUnit</li>
 * </ul>
 * 
 */
class HdfsTriggerWorkerTest {

	File baseDir = new File("target/test/HdfsTriggerModuleTest")


	/**
	 * Test that the HdfsTriggerModule setup works as expected
	 * <p/>
	 * Group A:<br/>
	 * (3) Create 2 Files different directories, 2 GlueUnit for both directories, test on startup units notified
	 */
	@Test
	public void groupA_3(){

		//==== Setup directories
		File testBaseDir = new File(baseDir, "groupA_3")
		File[] dirs = createTestFiles(testBaseDir, 2, 1)

		GlueModuleFactory glueModuleFactory = new GlueModuleFactoryImpl()
		ConfigObject config = getConfig()

		def triggers = ""

		dirs.eachWithIndex {File dir, int index ->
			if(index != 0)
				triggers += ","

			triggers += "hdfs:${dir.absolutePath}"
		}


		GlueUnit unit1 = createGlueUnit("unit1", triggers)
		GlueUnit unit2 = createGlueUnit("unit2", triggers)

		//add both glue units to the unitRepository
		MapGlueUnitRepository unitRepository = new MapGlueUnitRepository();
		unitRepository << unit1
		unitRepository << unit2

		MockGlueExecutor executor = new MockGlueExecutor()

		//it does not matter if we create the trigger store with unit1 as the default unit name
		HdfsTriggerWorker triggerModule = new HdfsTriggerWorker(createTriggerStore('unit1'), config, executor, 2, 1000L)
		triggerModule.update(unitRepository.iterator())

		Thread.sleep(10000L)
		
		assertEquals(2, executor.getCount("unit2"))
		assertEquals(2, executor.getCount("unit1"))

	}

	/**
	 * Test that the HdfsTriggerModule setup works as expected
	 * <p/>
	 * Group A:<br/>
	 * (2) Create 2 Files different directories, 1 GlueUnit for both directories, test on startup units notified</li>
	 */
	@Test
	public void groupA_2(){

		//==== Setup directories
		File testBaseDir = new File(baseDir, "groupA_2")
		File[] dirs = createTestFiles(testBaseDir, 2, 1)

		GlueModuleFactory glueModuleFactory = new GlueModuleFactoryImpl()
		ConfigObject config = getConfig()

		def triggers = ""

		dirs.eachWithIndex {File dir, int index ->
			if(index != 0)
				triggers += ","

			triggers += "hdfs:${dir.absolutePath}"
		}


		GlueUnit unit1 = createGlueUnit("unit1", triggers)

		MapGlueUnitRepository unitRepository = new MapGlueUnitRepository();
		unitRepository << unit1


		MockGlueExecutor executor = new MockGlueExecutor()

		HdfsTriggerWorker triggerModule = new HdfsTriggerWorker(createTriggerStore('unit1'), config, executor, 1, 1000L)
		triggerModule.update(unitRepository.iterator())

		Thread.sleep(3000L)

		assertEquals(2, executor.getCount("unit1"))
	}

	/**
	 * Test that the HdfsTriggerModule setup works as expected
	 * <p/>
	 * Group A:<br/>
	 * (1) Create 2 Files same directory, 2 GlueUnits, test on startup units notified.
	 */
	@Test
	public void groupA_1(){

		//==== Setup directories
		File testBaseDir = new File(baseDir, "groupA_1")
		createTestFiles(testBaseDir, 1, 2)

		GlueModuleFactory glueModuleFactory = new GlueModuleFactoryImpl()
		ConfigObject config = getConfig()

		GlueUnit unit1 = createGlueUnit("unit1", "hdfs:${testBaseDir.absolutePath}")
		GlueUnit unit2 = createGlueUnit("unit2", "hdfs:${testBaseDir.absolutePath}")

		MapGlueUnitRepository unitRepository = new MapGlueUnitRepository();
		unitRepository << unit1
		unitRepository << unit2

		MockGlueExecutor executor = new MockGlueExecutor()

		HdfsTriggerWorker triggerModule = new HdfsTriggerWorker(createTriggerStore('unit1'), config, executor, 1, 1000L)
		triggerModule.update(unitRepository.iterator())

		Thread.sleep 3000L

		//2 files per directory but for both the exec should only be called once
		//per glue unit giving an execution count of 1 per glue unit
		assertEquals(1, executor.getCount("unit1"))
		assertEquals(1, executor.getCount("unit2"))
		assertNotNull executor.hasUnit("unit1")
		assertNotNull executor.hasUnit("unit2")

	}

	private DBTriggerStore createTriggerStore(String unitName){

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
		
		
		""")


		DBTriggerStore module = new DBTriggerStore();
		module.init config
		configureModule(module, unitName)

		return module
	}

	/**
	 * Helper method that configures the module to work with the unitName
	 * @param unitName
	 */
	public void configureModule(DBTriggerStore module, String unitName){


		
	}

	/**
	 * Creates a GlueUnit instance with the name and trigger provided.
	 * @param name The GlueUnit name
	 * @param triggerDef will be used as triggers="${triggerDef}" 
	 * @param glueModuleFactory
	 * @return GlueUnit
	 */
	private GlueUnit createGlueUnit(String name, String triggerDef){

		def text = """
		
			name="$name"
			triggers="${triggerDef}"
			tasks{
				myprocess{
					tasks={
						println "hi"
					}
				}
		    }
		
		"""
		new DefaultGlueUnitBuilder().build(text)
	}

	/**
	 * Creates directories (size == dirCount) and files in each directory.
	 * @param baseDir If the directory exist, it will be deleted first
	 * @param dirCount
	 * @param fileCount
	 * @return File[] returns the directories created
	 */
	private File[] createTestFiles(File baseDir, int dirCount, int fileCount){
		if(baseDir.exists()){
			FileUtils.deleteDirectory(baseDir)
		}

		FileUtils.forceMkdir(baseDir)

		def dirs = []

		(1 .. dirCount).each { int i ->

			File directory = new File(baseDir, "testdir_$i")
			directory.mkdir()
			dirs << directory

			addTestFiles(directory, fileCount)
		}

		return dirs
	}

	/**
	 * Add files (size == fileCount) to the directory.
	 * @param dir
	 * @param fileCount
	 * @return File[] the files created
	 */
	private File[] addTestFiles(File dir, int fileCount){
		def files = []
		(1 .. fileCount).each{
			def file = new File(dir, "testFile_${System.currentTimeMillis()}")
			file << "Test file\n"
			files << file
		}
		return files
	}


	/**
	 * Returns a ConfigObject containing two cluster config examples	
	 * @return
	 */
	private ConfigObject getConfig(){
		new ConfigSlurper().parse("""
	   
		clusters{
		   test1{
		   	  isDefault=true
			  hdfsProperties="src/test/resources/testcluster.properties"
		   }
		   
		   test2{
			  hdfsProperties="src/test/resources/testcluster.properties"
		   }
		   
		}
	   
	   """)
	}
}
