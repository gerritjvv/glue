package org.glue.trigger.test.service.hdfs

import static org.junit.Assert.*

import org.glue.trigger.persist.db.DBTriggerStore
import org.glue.trigger.test.util.MockGlueExecutor
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.repo.impl.MapGlueUnitRepository
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * 
 * Most of the testing is done by the HdfsTriggerWorkerTest.<br/>
 * This tests goes over the configuration options and ensures that with the options defined in this test<br/>
 * works without errors from the HdfsTriggerModule
 *
 */
class HdfsTiggerModuleTest {

	GlueExecutor executor = new MockGlueExecutor()
	GlueUnitRepository repo = new MapGlueUnitRepository()
	HdfsTriggerModule module

	
	/**
	 * Test that a change to the triggers in a glue unit is seen
	 */
	@Test
	public void testRepoGlueUnitUpdatesSeen(){

		File file = new File('src/test/resources/testrepo')

		//setup a glue unit with one trigger
		GlueUnit unit = new DefaultGlueUnitBuilder().build("""
		
			name='test1'
			triggers='hdfs:${file.getAbsolutePath()}'
			tasks{
			   pA{
			     
			     tasks={
			        println "Hi"
			     }
			   }
			}
		""")



		repo << unit

		Thread.sleep(2000)
		assertEquals(module.getInfo()['unitRepositoryUpdatedCount'], 1)

		//we change only the file definition
		File updateFile = new File('src/test/resources/')
		GlueUnit updatedUnit = new DefaultGlueUnitBuilder().build("""
			
				name='test1'
				triggers='hdfs:${updateFile.getAbsolutePath()}'
				tasks{
				   pA{
					 
					 tasks={
						println "Hi"
					 }
				   }
				}
			""")
	
		 repo << updatedUnit
		 
		 //wait 10 seconds most for the update to be seen
		 //if not seen we count this as an error
		 int waitCount = 0
		 int waitCountLimit = 10
		 while( module.getInfo()['unitRepositoryUpdatedCount'] < 2  && waitCount < waitCountLimit){
			 Thread.sleep(1000)
			 waitCount++ 
		 }
		 
		 assertEquals(module.getInfo()['unitRepositoryUpdatedCount'], 2)
		 
	}

	/**
	 * Test that the update thread works
	 */
	@Test
	public void testRepoLastUpdated(){

		Date lastUpdatedDate = new Date()

		Thread.sleep(3000)

		assertTrue( module.info['repoLastUpdated'] > lastUpdatedDate)
	}

	@After
	public void shutdown() throws Exception{

		module.destroy()
	}

	@Before
	public void setup() throws Exception{

		module = new HdfsTriggerModule();
		module.glueExecutor = executor
		module.unitRepository = repo


		//define configuration
		def configStr = """
		
			clusters{
				test1{
				   hdfsProperties='src/test/resources/testcluster.properties'
				   isDefault=true
				}
			}
			
			pollingThreads='1'
			pollingPeriod='60000'
			repoPollingPeriod='10000'
			
			triggerStore{
			   
			   className='${DBTriggerStore.class.name}'
			   
			   config{
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
			   }
			
			}
			
		
		"""


		//run configuration
		module.init(new ConfigSlurper().parse(configStr))

	}
}
