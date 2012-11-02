
package org.glue.unit.status.impl.db

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.NoResultException
import javax.persistence.Query

import org.glue.unit.status.GlueUnitStatusManager
import org.glue.unit.status.ProcessStatus
import org.glue.unit.status.UnitStatus
import org.hibernate.ejb.Ejb3Configuration
import org.hibernate.tool.hbm2ddl.SchemaValidator


/**
 *
 *Tracks all unit executions status<br/>
 *
 *One static singleton EntityManagerFactory instance will be maintained.<br/>
 *For each method call to this module that requires access to the EntityManager a new EntityManager instance will be created along with a transaction,<br/>
 *both are closed in a finally statement after the method completed.
 * 
 */
@Typed
class DbUnitStatusManager implements GlueUnitStatusManager{

	private final Object LOCK = new Object()

	private EntityManagerFactory entityManagerFactory

	@Override
	void destroy(){
		shutdown()
	}


	public void shutdown(){
		synchronized (LOCK) {
			entityManagerFactory?.close()
		}
	}


	/**
	 * Starts the static jpa entity manager factory only once.
	 * If any error an Exception is thrown.<br/>
	 * The ConfigObject must have the hibernate type properties (without the hibernate prefix).<br/>
	 * e.g.<br/>
	 * <pre>
	 * 
	 statusManager{
	 host="jdbc:mysql://127.0.0.1:3306/glue" 
	 connection.username="glue"
	 connection.password="glue"
	 dialect="org.hibernate.dialect.MySQLDialect"
	 connection.driver_class="com.mysql.jdbc.Driver"
	 connection.url="jdbc:mysql://127.0.0.1:3306/glue"
	 hbm2ddl.auto="update"
	 connection.autocommit="false"
	 show_sql="true"
	 cache.use_second_level_cache="false"
	 cache.provider_class="org.hibernate.cache.NoCacheProvider"
	 cache.use_query_cache="false"
	 connection.provider_class="org.hibernate.connection.C3P0ConnectionProvider"
	 c3p0.min_size="5"
	 c3p0.max_size="100"
	 c3p0.timeout="1800"
	 c3p0.max_statements="500"
	 }
	 * </pre>
	 */
	@Override
	public void init(ConfigObject config) {

		if(!entityManagerFactory){

			synchronized (LOCK){
				if(!entityManagerFactory){
					Ejb3Configuration cfg = new Ejb3Configuration();
					cfg.addAnnotatedClass ProcessEntity.class
					cfg.addAnnotatedClass UnitEntity.class
					cfg.setProperties(config.toProperties('hibernate'))
					entityManagerFactory = cfg.buildEntityManagerFactory()
					new SchemaValidator(cfg.getHibernateConfiguration()).validate()
				}
			}
		}
	}


	/**
	 * Finds UnitStatus instances by date range inclusively
	 * @param workflowName
	 * @param rangeStart
	 * @param rangeEnd
	 * @return Collection of UnitStatus
	 */
	Collection<UnitStatus> findUnitStatus(String workflowName, Date rangeStart, Date rangeEnd){

		Collection<UnitStatus> list = []

		//choose dates so that startDate < endDate
		Date startDate, endDate

		if(rangeStart.time > rangeEnd.time){
			startDate = rangeEnd
			endDate = rangeStart
		}else{
			startDate = rangeStart
			endDate = rangeEnd
		}

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()
		try{

			Query q = em.createNamedQuery('UnitEntity.byNameAndDateRange')
			q.setParameter 'startDate', startDate
			q.setParameter 'endDate', endDate
			q.setParameter 'name', workflowName

			Collection<UnitEntity> results = q.getResultList()

			//convert each unit entity to a UnitStatus instance
			results?.each{ UnitEntity entity ->
				list << entity.toUnitStatus()
			}

			em.getTransaction().commit()
		}finally{
			em.close()
		}

		return list
	}
	
	UnitStatus getLatestUnitStatus(String workflowName){
		
		UnitStatus stat = null

		

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()
		try{

			Query q = em.createNamedQuery('UnitEntity.byLatestName')
			q.setParameter 'name', workflowName

			Collection<UnitEntity> list = q.getResultList()
			if(list.size() > 0)
				stat = ((UnitEntity)list[0]).toUnitStatus()
			
			em.getTransaction().commit()
		}finally{
			em.close()
		}

		return stat
	}
	
	/**
	 * Finds UnitStatus instances by date range inclusively
	 * @param rangeStart
	 * @param rangeEnd
	 * @return Collection of UnitStatus
	 */
	Collection<UnitStatus> findUnitStatus(Date rangeStart, Date rangeEnd){

		Collection<UnitStatus> list = []

		//choose dates so that startDate < endDate
		Date startDate, endDate

		if(rangeStart.time > rangeEnd.time){
			startDate = rangeEnd
			endDate = rangeStart
		}else{
			startDate = rangeStart
			endDate = rangeEnd
		}

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()
		try{

			Query q = em.createNamedQuery('UnitEntity.byDateRange')
			q.setParameter 'startDate', startDate
			q.setParameter 'endDate', endDate

			Collection<UnitEntity> results = q.getResultList()

			//convert each unit entity to a UnitStatus instance
			results?.each{ UnitEntity entity ->
				list << entity.toUnitStatus()
			}

			em.getTransaction().commit()
		}finally{
			em.close()
		}

		return list
	}


	/**
	 * @param unitId
	 * @return UnitStatus or null if the unit was not found
	 */
	UnitStatus getUnitStatus(String unitId){
		getUnit(unitId)?.toUnitStatus()
	}

	/**
	 * Gets a collection of unit ProcessStatus instances ofr the unitId
	 * @param unitId The unit id
	 * @return Collection of ProcessStatus or nullt if the unit was not found
	 */
	Collection<ProcessStatus> getUnitProcesses(String unitId){

		Collection<ProcessStatus> list = []

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()
		try{

			Query q = em.createNamedQuery('ProcessEntity.byUnitId')
			q.setParameter 'unitId', unitId

			Collection<ProcessEntity> results = q.getResultList()

			//convert each process entity to a ProcessStatus instance
			results?.each{ ProcessEntity entity ->
				list << entity.toProcessStatus()
			}

			em.getTransaction().commit()
		}finally{
			em.close()
		}

		return list
	}

	/**
	 * Gets the process status of a unit
	 * @param unitId
	 * @param processName The process name
	 * @return ProcesStatus null if not found
	 */
	ProcessStatus getProcessStatus(String unitId, String processName){
		getProcess(unitId, processName).toProcessStatus()
	}


	private UnitEntity getUnit(String unitId){
		UnitEntity unit = null

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()
		try{

			unit = em.find(UnitEntity.class, unitId)

			em.getTransaction().commit()
		}finally{
			em.close()
		}

		return unit
	}

	private ProcessEntity getProcess(String unitId, String processName){
		ProcessEntity process = null

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()
		try{

			process = em.find(ProcessEntity.class, new ProcessId(unitId:unitId, processName:processName))

			em.getTransaction().commit()
		}finally{
			em.close()
		}

		return process
	}

	@Override
	public void setProcessStatus(ProcessStatus processStatus) {

		if(!processStatus.unitId){
			throw new RuntimeException("The ProcessStatus must have a unitId associated")
		}

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()


		try{
			ProcessEntity entity = em.find(ProcessEntity.class, new ProcessId(unitId:processStatus.unitId, processName:processStatus.processName))
			if(!entity){
				entity = new ProcessEntity(processStatus)
			}else{
				entity.update(processStatus)
			}

			em.persist(entity)
			em.getTransaction().commit()
		}finally{
			em.close()
		}
		return
	}



	@Override
	public void setUnitStatus(UnitStatus unitStatus) {

		if(!unitStatus.unitId){
			throw new RuntimeException("The UnitStatus must have a unitId associated")
		}

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()

		try{

			UnitEntity entity = em.find(UnitEntity.class, unitStatus.unitId)

			if(!entity){
				entity = new UnitEntity(unitStatus)
			}else{
				entity.update(unitStatus)
			}

			em.persist(entity)
			em.getTransaction().commit()
		}finally{
			em.close()
		}
		return
	}
}
