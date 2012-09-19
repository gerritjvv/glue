package org.glue.trigger.test.persist.db;

import static org.junit.Assert.*

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Query

import org.glue.trigger.persist.db.TriggerFileUnitStatus
import org.glue.trigger.persist.db.TriggersCheckPointEntity
import org.glue.trigger.persist.db.TriggersCheckPointEntityId
import org.hibernate.ejb.Ejb3Configuration
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * 
 * Tests that the TriggersCheckPointEntity CRUD operations act as expected.
 *
 */
class TriggersCheckPointEntityTest {
	
	EntityManagerFactory entityManagerFactory
	
	
	/**
	 * Test that we can insert a TriggersFileEntity
	 */
	@Test
	public void testStoreTriggersFileEntity(){
		
		TriggersCheckPointEntity checkpoint = new TriggersCheckPointEntity(
			id:new TriggersCheckPointEntityId(unitName:'test1', path:'test'),
			checkpoint:new Date()
			
			)
		
		withTx { EntityManager em ->
			
			em.persist(checkpoint)
			
		}
		
		TriggersCheckPointEntity foundCheckpoint
		withTx { EntityManager em ->
			Query query = em.createNamedQuery('TriggersCheckPointEntity.byUnitName').setParameter('unitName', checkpoint.unitName)
			foundCheckpoint = query.getSingleResult()
		}
		
		assertNotNull( foundCheckpoint )
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
                connection.url="jdbc:hsqldb:mem:triggersCheckPointEntityTest"
                hbm2ddl.auto="create"
                connection.autocommit="false"
                show_sql="true"
                cache.use_second_level_cache="false"
                cache.provider_class="org.hibernate.cache.NoCacheProvider"
                cache.use_query_cache="false"
        
		
		""")
		
		Ejb3Configuration cfg = new Ejb3Configuration();
		cfg.addAnnotatedClass TriggersCheckPointEntity.class
		cfg.addAnnotatedClass TriggerFileUnitStatus.class
		
		cfg.setProperties(config.toProperties('hibernate'))
		entityManagerFactory = cfg.buildEntityManagerFactory()
	}
	
	@After
	public void shutdown(){
		
		entityManagerFactory?.close()
	}
}
