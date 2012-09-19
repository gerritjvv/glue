package org.glue.trigger.test.service.hdfs;

import org.glue.trigger.persist.db.DBTriggerStore;
import org.glue.trigger.service.hdfs.HDFSModuleTriggers
import org.glue.trigger.service.hdfs.HdfsTriggerWorker
import org.glue.unit.om.GlueModuleFactory;
import org.glue.unit.om.impl.DefaultGlueUnitBuilder;
import org.glue.unit.om.impl.GlueModuleFactoryImpl;
import org.glue.unit.repo.GlueUnitRepository;
import org.glue.unit.repo.impl.DirGlueUnitRepository;
import org.junit.Test

import static org.junit.Assert.*;

/**
 *
 * Test the HdfsTriggerModule
 *
 */
class HdfsTriggerModuleConfigurationTest {

	/**
	 * Test that the HdfsTriggerModule setup works as expected
	 */
	@Test
	public void testConfigurationDefaultClusterNameWithoutSpecification(){

		ConfigObject config = new ConfigSlurper().parse("""
	   
		clusters{
		   test1{
			  hdfsProperties="src/test/resources/testcluster.properties"
		   }
		   
		   test2{
			  hdfsProperties="src/test/resources/testcluster.properties"
		   }
		   
		}
	   
	   """)

		GlueModuleFactory glueModuleFactory = new GlueModuleFactoryImpl()

		HdfsTriggerWorker triggerModule = new HdfsTriggerWorker(createTriggerStore('unit1'), config, null)

		assertEquals("test1", triggerModule.defaultClusterName)
		triggerModule.stop()
	}

	/**
	 * Test that the HdfsTriggerModule setup works as expected
	 */
	@Test
	public void testConfiguration(){

		ConfigObject config = getConfig()

		GlueModuleFactory glueModuleFactory = new GlueModuleFactoryImpl()
		//we point the repo at two glue workflows
		//one does not specify in the triggers definition the cluster its pointing to
		//the other (test2.groovy) does point to the test2 cluster
		GlueUnitRepository repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			'src/test/resources/testrepo'
		])


		HdfsTriggerWorker triggerModule = new HdfsTriggerWorker(createTriggerStore('unit1'), config, null)

		assertEquals("test1", triggerModule.defaultClusterName)

		//update the triggerModule with the repo values
		triggerModule.update(repo.iterator())

		assertEquals(2, triggerModule.triggersMap.size())

		//test that test2.groovy and test3.groovy are registered for the test2 cluster
		HDFSModuleTriggers triggersTest2 = triggerModule.triggersMap["test2"]
		assertNotNull(triggersTest2)

		List<String> unitNames2 = triggersTest2.getGlueUnitNames("mypath/*")

		assertEquals(2, unitNames2.size())

		unitNames2.each {
			assertTrue( it == "test2"  || it == "test3")
		}

		//test that test1.groovy is registered for the default cluster test1
		HDFSModuleTriggers triggersTest1 = triggerModule.triggersMap["test1"]
		List<String> unitNames1 = triggersTest1.getGlueUnitNames("mypath/*")
		assertNotNull(unitNames1)
		assertEquals(1, unitNames1.size())

		unitNames1.each { assertTrue( it == "test1" ) }

		triggerModule.stop()
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

		return module
	}
}
