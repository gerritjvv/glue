
package org.glue.trigger.test.persist.db

import static org.junit.Assert.*

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Query

import org.glue.trigger.persist.db.TriggerFileUnitStatus
import org.glue.trigger.persist.db.TriggersFileEntity
import org.hibernate.ejb.Ejb3Configuration
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * 
 * Tests that the TriggersFileEntity CRUD operations act as expected.
 *
 */
class TriggersFileEntityTest {
	
	EntityManagerFactory entityManagerFactory
	
	/**
	* Test that we can correctly find entries by date range
	*/
   @Test
   public void testFindByDateRange(){
	   
	   String unitName1 = 'testFindByDateRange1'
	   String unitName2 = 'testFindByDateRange2'
	   
	   String path = 'mypath/testthis/filena*me2/{1,2}/{1..2}'
	   
	   Date dateStart = new Date()
	   Date date1 = dateStart + 1
	   Date date2 = dateStart + 2
		
	   TriggersFileEntity file1 = new TriggersFileEntity(
			   unitName:unitName1,
			   status:TriggersFileEntity.STATUS.READY,
			   path:path,
			   date:date1
			   )
	   
	   TriggersFileEntity file2 = new TriggersFileEntity(
			   unitName:unitName2,
			   status:TriggersFileEntity.STATUS.PROCESSED,
			   path:"$path/other",
			   date:date2
			   )
	   
	   withTx { EntityManager em ->
		   em.persist file1
		   em.persist file2
	   }
	   
	   //find 1 entry
	   withTx { EntityManager em ->
			def set = TriggersFileEntity.getByDateRange(em, date1, date1)
			assertNotNull(set)
			assertEquals(1, set.size())
	   }
	   
	   //find all entries
	   withTx { EntityManager em ->
		   def set = TriggersFileEntity.getByDateRange(em, dateStart, date2)
		   assertNotNull(set)
		   assertEquals(2, set.size())
	   }
   }
   
   
   @Test
   public void testFileStatus(){
	   
	   String unitName1 = 'testFindByDateRange1'
	   
	   String path = 'mypath/testthis/filena*me2/{1,2}/{1..2}'
	   
	   Date dateStart = new Date()
	   Date date1 = dateStart + 1
	   
	   TriggersFileEntity file1 = new TriggersFileEntity(
			   unitName:unitName1,
			   status:TriggersFileEntity.STATUS.READY,
			   path:path,
			   date:date1
			   )
	
	   file1.addFileUnitStatus('1234', TriggersFileEntity.STATUS.PROCESSED);   
	  
	   withTx { EntityManager em ->
		   em.persist file1
		}
	   
	   
	   List<TriggerFileUnitStatus> statusColl = null;
	   
	   //find 1 entry
	   withTx { EntityManager em ->
		 TriggersFileEntity foundEntity = TriggersFileEntity.getByName(em, unitName1)
		 statusColl = foundEntity.unitStatus;
		 
		 assertEquals(1, statusColl.size())
		 assertNotNull(statusColl.get(0))
		 assertEquals(statusColl.get(0).unitId, '1234')
		 assertEquals(statusColl.get(0).status, TriggersFileEntity.STATUS.PROCESSED)
		
	   }
	   
	   
   }
   
	
	/**
	* Test that we can correctly find entries by unit name and date range
	*/
   @Test
   public void testFindByNameDateRange(){
	   
	   String unitName = 'testFindByNameDateRange'
	   String path = 'mypath/testthis/filena*me2/{1,2}/{1..2}'
	   
	   Date dateStart = new Date()
	   Date date1 = dateStart + 1
	   Date date2 = dateStart + 2
	    
	   TriggersFileEntity file1 = new TriggersFileEntity(
			   unitName:unitName,
			   status:TriggersFileEntity.STATUS.READY,
			   path:path,
			   date:date1
			   )
	   
	   TriggersFileEntity file2 = new TriggersFileEntity(
			   unitName:unitName,
			   status:TriggersFileEntity.STATUS.PROCESSED,
			   path:"$path/other",
			   date:date2
			   )
	   
	   withTx { EntityManager em ->
		   em.persist file1
		   em.persist file2
	   }
	   
	   //find 1 entry
	   withTx { EntityManager em ->
			def set = TriggersFileEntity.getByNameAndDateRange(em, unitName, date1, date1)
			assertNotNull(set)
			assertEquals(1, set.size())
	   }
	   
	   //find all entries
	   withTx { EntityManager em ->
		   def set = TriggersFileEntity.getByNameAndDateRange(em, unitName, dateStart, date2)
		   assertNotNull(set)
		   assertEquals(2, set.size())
	   }
   }
   
   
	/**
	 * Test that we cannot insert duplicates.<br/>
	 * We expect a PersistenceException to be thrown.
	 */
	@Test
	public void testFindByNameStatus(){
		
		String unitName = 'test4'
		String path = 'mypath/testthis/filena*me2/{1,2}/{1..2}'
		
		TriggersFileEntity file1 = new TriggersFileEntity(
				unitName:unitName,
				status:TriggersFileEntity.STATUS.READY,
				path:path
				)
		
		TriggersFileEntity file2 = new TriggersFileEntity(
				unitName:unitName,
				status:TriggersFileEntity.STATUS.PROCESSED,
				path:"$path/other"
				)
		
		withTx { EntityManager em ->
			em.persist file1
			em.persist file2
		}
		
		//find files with status == READY
		withTx { EntityManager em ->
			Query query = em.createNamedQuery('TriggersFileEntity.byUnitNameAndStatus').setParameter('unitName', unitName).setParameter('status', TriggersFileEntity.STATUS.READY)
			assertNotNull( query.getSingleResult() )
		}
		
		//find files with status == PROCESSED
		withTx { EntityManager em ->
			Query query = em.createNamedQuery('TriggersFileEntity.byUnitNameAndStatus').setParameter('unitName', unitName).setParameter('status', TriggersFileEntity.STATUS.PROCESSED)
			assertNotNull( query.getSingleResult() )
		}
	}
	
	/**
	 * Test that we cannot insert duplicates.<br/>
	 * We expect a PersistenceException to be thrown.
	 */
//	@Test(expected=PersistenceException.class)
	public void testErrorOnDuplicates(){
	
		//@TODO we take this test out because duplicate checking has been disabled for the moment.
//		
//		String unitName = 'test3'
//		String path = 'mypath/testthis/filena*me2/{1,2}/{1..2}'
//		
//		TriggersFileEntity file = new TriggersFileEntity(
//				unitName:unitName,
//				status:TriggersFileEntity.STATUS.READY,
//				path:path
//				)
//		
//		withTx { EntityManager em ->
//			em.persist file
//		}
//		
//		//to make this object seem as a new one, set the id to null
//		file.id = null
//		
//		//insert duplicate
//		withTx { EntityManager em ->
//			em.persist file
//		}
	}
	
	
	/**
	 * Test that we can insert, update
	 */
	@Test
	public void testUpdateTriggersFileEntity(){
		
		String unitName = 'test2'
		String path = 'mypath/testthis/filena*me2/{1,2}/{1..2}'
		
		TriggersFileEntity file = new TriggersFileEntity(
				unitName:unitName,
				status:TriggersFileEntity.STATUS.READY,
				path:path
				)
		
		withTx { EntityManager em ->
			em.persist file
		}
		
		//check that we can update the item
		String path2 = "$path/updated_path"	      
		
		file.path = path2
		
		withTx { EntityManager em ->
			em.merge file
		}
		
		
		
		TriggersFileEntity foundFile
		
		withTx { EntityManager em ->
			Query query = em.createNamedQuery('TriggersFileEntity.byUnitName').setParameter('unitName', unitName)
			foundFile = query.getSingleResult()
		}
		
		assertNotNull foundFile
		assertEquals path2, foundFile.path
		assertEquals file.unitName, foundFile.unitName
		assertEquals file.status, foundFile.status
	}
	
	
	/**
	 * Test that we can insert a TriggersFileEntity
	 */
	@Test
	public void testStoreTriggersFileEntity(){
		
		TriggersFileEntity file = new TriggersFileEntity(
				unitName:'test1',
				status:TriggersFileEntity.STATUS.READY,
				path:'mypath/testthis/filename'
				)
		
		withTx { EntityManager em ->
			em.persist file
		}
		
		
		//check that the file item exists
		TriggersFileEntity foundFile
		
		withTx { EntityManager em ->
			Query query = em.createNamedQuery('TriggersFileEntity.byUnitName').setParameter('unitName', 'test1')
			foundFile = query.getSingleResult()
		}
		
		assertNotNull foundFile
		assertEquals file.path, foundFile.path
		assertEquals file.unitName, foundFile.unitName
		assertEquals file.status, foundFile.status
	}
	
	/**
	 * Helper method that executes to closure inside a transaction	
	 * @param closure
	 */
	private void withTx(Closure closure){
		
		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()
		try{
			closure(em)
			em.getTransaction().commit()
		}finally{
			em.close()
		}
	}
	
	@Before
	public void setup(){
		
		ConfigObject config = new ConfigSlurper().parse("""
		
                connection.username="sa"
                connection.password=""
                dialect="org.hibernate.dialect.HSQLDialect"
                connection.driver_class="org.hsqldb.jdbcDriver"
                connection.url="jdbc:hsqldb:mem:triggersFileEntityTest"
                hbm2ddl.auto="create-drop"
                connection.autocommit="false"
                show_sql="true"
                cache.use_second_level_cache="false"
                cache.provider_class="org.hibernate.cache.NoCacheProvider"
                cache.use_query_cache="false"
                connection.autocommit = true
		
		""")
		
		Ejb3Configuration cfg = new Ejb3Configuration();
		cfg.addAnnotatedClass TriggersFileEntity.class
		cfg.addAnnotatedClass TriggerFileUnitStatus.class
		
		cfg.setProperties(config.toProperties('hibernate'))
		entityManagerFactory = cfg.buildEntityManagerFactory()
	}
	
	@After
	public void shutdown(){
		
		entityManagerFactory?.close()
	}
}
