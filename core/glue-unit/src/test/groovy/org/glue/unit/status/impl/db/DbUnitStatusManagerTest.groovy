package org.glue.unit.status.impl.db;

import static org.junit.Assert.*

import org.glue.unit.exec.GlueState
import org.glue.unit.status.ProcessStatus
import org.glue.unit.status.UnitStatus
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

/**
 *
 * We test the the DbunitStatusManager works as expected
 *
 */
class DbUnitStatusManagerTest {

	static DbUnitStatusManager manager
	
	@Test
	public void testGetProcessStatusList(){
		
		Set<ProcessStatus> processSet = []
		
		String unitId = String.valueOf(System.currentTimeMillis())
		
		//save 10 processes
		(1..10).each{ int i ->
			
			def status = new ProcessStatus(
				unitId:unitId,
				processName:"process_$i",
				startDate: new Date(),
				endDate:new Date(),
				status:GlueState.RUNNING
				)
			try{
			manager.setProcessStatus status
			}catch(t){
				t.printStackTrace()
			}
			processSet << status
		}
	
		
		//verify that they all exist
		Collection<ProcessStatus> foundProcesses = manager.getUnitProcesses(unitId)
		
		assertNotNull(foundProcesses)
		assertEquals(processSet.size(), foundProcesses.size())
	
	}
	
	@Test
	public void testSetGetUnitStatusByNameAndDateRange(){
		
		Set<UnitStatus> statusList = []
		
		Date endDate = new Date()
		Date startDate = endDate - 2
		String name = "testSetGetUnitStatusByNameAndDateRange"
		
		//add 5 status instances for today - 2DaysAgo
		(1..5).each {
			String unitId = java.util.UUID.randomUUID()
			UnitStatus status = new UnitStatus(
			unitId:unitId,
			name:name,
			startDate:startDate,
			endDate:endDate,
			status:GlueState.RUNNING
			)
			
			manager.setUnitStatus status
			statusList << status
		}
		
		//add another 5 starting 3 days ago
		(1..5).each {
			String unitId = java.util.UUID.randomUUID()
			UnitStatus status = new UnitStatus(
			unitId:unitId,
			name:name,
			startDate:startDate-3,
			endDate:endDate,
			status:GlueState.RUNNING
			)
			
			manager.setUnitStatus status
		}
		
		//test that we can see the 5 jobs from 2 days ago
		
		Collection<UnitStatus> foundStatusList = manager.findUnitStatus(name, startDate, endDate)
		assertEquals(statusList.size(), foundStatusList.size())
		
		foundStatusList.each { UnitStatus foundStatus ->
			 assertTrue(statusList.contains(foundStatus))
		}
		
	
	}
	
	@Test
	public void testSetGetUnitStatusByDateRange(){
		
		
		
		Set<UnitStatus> statusList = []
		
		Date endDate = new Date()
		Date startDate = endDate - 2
		
		//add 5 status instances for today - 2DaysAgo
		(1..5).each {
			String unitId = java.util.UUID.randomUUID()
			UnitStatus status = new UnitStatus(
			unitId:unitId,
			name:'testSetGetUnitStatusByDateRange',
			startDate:startDate,
			endDate:endDate,
			status:GlueState.RUNNING
			)
			
			manager.setUnitStatus status
			statusList << status
		}
		
		//add another 5 starting 3 days ago
		(1..5).each {
			String unitId = java.util.UUID.randomUUID()
			UnitStatus status = new UnitStatus(
			unitId:unitId,
			name:'testSetGetUnitStatusByDateRange',
			startDate:startDate-3,
			endDate:endDate,
			status:GlueState.RUNNING
			)
			
			manager.setUnitStatus status
		}
		
		//test that we can see the 5 jobs from 2 days ago
		
		Collection<UnitStatus> foundStatusList = manager.findUnitStatus(startDate, endDate)
		assertEquals(statusList.size(), foundStatusList.size())
		
		foundStatusList.each { UnitStatus foundStatus ->
			 assertTrue(statusList.contains(foundStatus))
		}
		
	
	}
	
	@Test
	public void testSetGetUnitStatus(){
		
		String unitId = String.valueOf(System.currentTimeMillis())
		
		def statusList = []
		
		(1..10).each {
			UnitStatus status = new UnitStatus(
			unitId:unitId,
			name:'test1',
			startDate:new Date(),
			endDate:new Date(),
			status:GlueState.RUNNING
			)
			
			manager.setUnitStatus status
			statusList << status
		}
		
	
		statusList.each { UnitStatus status ->
			
			UnitStatus foundStatus = manager.getUnitStatus(status.unitId)
			assertNotNull(foundStatus)
			assertEquals(status.unitId, foundStatus.unitId)
		
		}
	}
	
	@Test
	public void testSetGetUnitProcessStatus(){
		
		
		ProcessStatus status = new ProcessStatus(
			unitId:'1020',
			processName:'pA',
			startDate: new Date(),
			endDate:new Date(),
			status:GlueState.RUNNING,
			)
		
		manager.setProcessStatus status
		
		
		ProcessStatus foundStatus = manager.getProcessStatus(status.unitId, status.processName)
		assertNotNull(foundStatus)
		
		
		assertEquals(status, foundStatus)
		
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		manager = new DbUnitStatusManager()
		manager.init(new ConfigSlurper().parse("""
		config{
				connection.username="sa"
				connection.password=""
				dialect="org.hibernate.dialect.HSQLDialect"
				connection.driver_class="org.hsqldb.jdbcDriver"
				connection.url="jdbc:hsqldb:mem:DbUnitStatusManagerTest"
				hbm2ddl.auto="create"
				connection.autocommit="false"
				show_sql="true"
				cache.use_second_level_cache="false"
				cache.provider_class="org.hibernate.cache.NoCacheProvider"
				cache.use_query_cache="false"
			}
		""").config)
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		manager.destroy()
	}

	
}
