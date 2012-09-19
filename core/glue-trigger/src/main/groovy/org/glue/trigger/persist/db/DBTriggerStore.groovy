package org.glue.trigger.persist.db

import groovy.lang.Closure
import groovy.util.ConfigObject

import java.util.Date
import java.util.Map
import java.util.Properties
import java.util.Set
import java.util.concurrent.atomic.AtomicBoolean

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

import org.glue.trigger.persist.CheckPoint
import org.glue.trigger.persist.TriggerStore
import org.glue.unit.repo.PathDateExtractor
import org.hibernate.ejb.Ejb3Configuration
import org.hibernate.tool.hbm2ddl.SchemaValidator
import org.streams.commons.zookeeper.ZConnection
import org.streams.commons.zookeeper.ZLock


/**
 * 
 * Implements the TriggerStore interface to save all trigger events to a RDBMS.<br/>
 * All events are saved through a JPA hibernate connection.<br/>
 * The entities  TriggersFileEntity and TriggersCheckPointEntity are used to represent the<br/> 
 * different elements of the database.<br/>
 *
 */
@Typed
class DBTriggerStore extends TriggerStore{

	private static final Object LOCK = new Object()

	private static EntityManagerFactory entityManagerFactory

	ZLock zlock;

	/**
	 * When the withTransaction method is called a new instance of DBTriggerStore <br/>
	 * is created with an instance of EntityManager set to this instance variable.<br/>
	 * This instance can also be used to set a global transaction externally.
	 */
	EntityManager instanceEntityManager

	void destroy(){
		shutdown()
	}


	/**
	 * All operations inside the closure done with the TriggerStore is within the same transaction.
	 * @param closure a transactional TriggerStore instance is passed as parameter
	 */
	@Override
	void withTransaction(Closure closure){

		EntityManager entityManager = entityManagerFactory.createEntityManager()
		entityManager.getTransaction().begin()
		try{

			closure(new DBTriggerStore(instanceEntityManager:entityManager,zlock:zlock))

			entityManager.getTransaction().commit()
		}finally{
			entityManager.close()
		}
	}


	/**
	 * Store checkpoint variable for the current configured unit name.<br/>
	 * @param unitName
	 * @param path This is the directory path to which the trigger was registered.
	 * @param checkpoint
	 */
	public void storeCheckPoint(String unitName, String path, CheckPoint checkPoint){
		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		withTx { EntityManager em ->

			TriggersCheckPointEntity checkpointEntity = TriggersCheckPointEntity.getByUnitNameAndPath(em, unitName, path)
			if(!checkpointEntity){
				checkpointEntity = new TriggersCheckPointEntity(id:new TriggersCheckPointEntityId(unitName:unitName, path:path))
			}
			checkpointEntity.fill(checkPoint)

			em.persist checkpointEntity
		}
	}

	/** Store checkpoint variable for the current configured unit name.<br/>
	 * @param unitName
	 * @param path This is the directory path to which the trigger was registered.
	 * @param checkpoint
	 */
	public void storeCheckPoints(String unitName, Map<String, CheckPoint> checkPoints){
		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		withTx { EntityManager em ->

			checkPoints.each { String path, CheckPoint checkPoint ->
				TriggersCheckPointEntity checkpointEntity = TriggersCheckPointEntity.getByUnitNameAndPath(em, unitName, path)
				if(!checkpointEntity){
					checkpointEntity = new TriggersCheckPointEntity(id:new TriggersCheckPointEntityId(unitName:unitName, path:path))
				}
				checkpointEntity.fill(checkPoint)

				em.persist checkpointEntity
			}
			
		}
		
	}


	/**
	 * Removes a checkpoint variable for the current configured unit name.<br/>
	 * @param unitName default is null, if null the onUnitStart method must have been called and the unit name of the GlueUnit passed will be used.
	 * @param path This is the directory path to which the trigger was registered.
	 */
	public void removeCheckPoint(String unitName, String path){
		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}


		withTx { EntityManager em ->
			
			TriggersCheckPointEntity.removeByNameAndPath em, unitName, path
		}
	}

	/**
	 * Returns a map of key = string, value = date<br/>
	 * The key is the path for which the trigger was listening.
	 * @param unitName
	 * @return Map
	 */
	public Map<String, CheckPoint> getCheckPoints(String unitName){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		Map<String, CheckPoint> checkpointDates = [:]

		withTx { EntityManager em ->

			TriggersCheckPointEntity.getByUnitName(em, unitName)?.each { TriggersCheckPointEntity entity ->
				checkpointDates[entity.id.path] = entity.asCheckPoint()
			}
		}

		return checkpointDates
	}

	/**
	 * Get the check point date for the path.
	 * @param unitName
	 * @param path
	 * @return CheckPoint
	 */
	public CheckPoint getCheckPoint(String unitName, String path){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		CheckPoint[] checkpointArr = new CheckPoint[1]

		withTx { EntityManager em ->

			checkpointArr[0] = TriggersCheckPointEntity.getByUnitNameAndPath(em, unitName, path)?.asCheckPoint()
		}
		println "Checkpoint for $path/$unitName is ${checkpointArr[0]}";
		return (checkpointArr[0]) ? checkpointArr[0] : new CheckPoint(date:new Date())
	}

	void updateReadyFiles(String unitName, Closure closure){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		withTx { EntityManager em ->

			TriggersFileEntity.getByNameAndStatus(em, unitName, TriggersFileEntity.STATUS.READY)?.each { TriggersFileEntity entity ->
				if(closure(entity)){
					em.persist entity
				}
			}
		}
	}

	/**
	 * List all files that where updated as READY by the trigger<br/>
	 * The closure is called with (entityName:String, filePath:String)<br/>
	 * The closure will be called within the scope of a database transaction.<br/>
	 * @param unitName
	 * @param closure
	 */
	public void listReadyFiles(String unitName, Closure closure, boolean lock = true){

		
		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		Closure clsToCall = closure

		//we check here that we can call a closure with 0 args, 1 arg etc.
		if(closure.getMaximumNumberOfParameters() == 1){
			clsToCall = { n, p ->
				closure(p)
			}
		}




		if(!lock){
			withTx { EntityManager em ->
				
				TriggersFileEntity.getByNameAndStatus(em, unitName, TriggersFileEntity.STATUS.READY)?.each { TriggersFileEntity entity ->
					
					clsToCall(entity.unitName, entity.path)
				}

			}
		}else{

			def paths = []
			
			withTx { EntityManager em ->
				TriggersFileEntity.getByNameAndStatus(em, unitName, TriggersFileEntity.STATUS.READY)?.each { TriggersFileEntity entity -> 
					paths << entity.path 
				}
			}


			//foreach path only call the closure with the paths that are not locked
			paths.each { String path ->
				if(zlock(unitName, path)){
					
					clsToCall(unitName, path)
				}
			}

		}

	}

	/**
	 * Tries to lock a trigger file. if the lock cannot be acquired false is returned
	 * @param unintName
	 * @param path
	 * @return
	 */
	private boolean zlock(String unitName, String path){
		return (!zlock)? true : zlock?.lock(unitName);
	}

	/**
	 * List all files that were found by the trigger<br/>
	 * The closure is called with (entityName:String, status:String, filePath:String)<br/>
	 * The closure will be called withing the scope of a database transaction.<br/>
	 * @param unitName
	 * @param closure
	 */
	public void listAllFiles(String unitName, Closure closure){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		Closure clsToCall = closure

		//we check here that we can call a closure with 0 args, 1 arg etc.
		if(closure.getMaximumNumberOfParameters() == 1){
			clsToCall = { n, p -> closure(p) }
		}else if(closure.getMaximumNumberOfParameters() == 2){
			clsToCall = { n, s, p -> closure(s, p) }
		}

		withTx { EntityManager em ->

			TriggersFileEntity.getByName(em, unitName)?.each { TriggersFileEntity entity ->
				clsToCall(entity.unitName, entity.status?.toString(), entity.path)
			}
		}
	}

	/**
	 * List all files that were updated as PROCESSED by the work flows<br/>
	 * The closure is called with (entityName:String, filePath:String)<br/>
	 * The closure will be called withing the scope of a database transaction.<br/>
	 * @param unitName 
	 * @param closure
	 */
	public void listProcessedFiles(String unitName, Closure closure){


		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		Closure clsToCall = closure

		//we check here that we can call a closure with 0 args, 1 arg etc.
		if(closure.getMaximumNumberOfParameters() == 1){
			clsToCall = { n, p -> closure(p) }
		}

		withTx { EntityManager em ->

			TriggersFileEntity.getByNameAndStatus(em, unitName, TriggersFileEntity.STATUS.PROCESSED)?.each { TriggersFileEntity entity ->
				clsToCall(entity.unitName, entity.path)
			}
		}
	}

	/**
	 * Marks a file as processed
	 * @param unitName
	 * @param path
	 */
	public void deleteFile(String unitName, String path){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		withTx { EntityManager em ->

			def file = TriggersFileEntity.getByNameAndPath(em, unitName, path)
			if(file){
				em.remove(file)
			}
		}
	}

	/**
	 * Gets the status of a file, if it does not exist null is returned
	 * @param unitName
	 * @param path
	 * @return boolean
	 */
	public String getStatus(String unitName, String path){

		return withTx { EntityManager em ->

			return TriggersFileEntity.getByNameAndPath(em, unitName, path)?.status?.toString()

		}

	}


	/**
	 * True if the file has already been marked as processed
	 * @param unitName
	 * @param path
	 * @return boolean
	 */
	public boolean isFileProcessed(String unitName, String path){

		AtomicBoolean bool = new AtomicBoolean(false)

		withTx { EntityManager em ->

			def file = TriggersFileEntity.getByNameAndPath(em, unitName, path)
			bool.set (file && file.status == TriggersFileEntity.STATUS.PROCESSED)
		}

		return bool.get()
	}

	/**
	 * Marks a file as processed
	 * @param unitName
	 * @param path
	 */
	public void markFilesAsProcessed(String unitName, String unitId, paths){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		def pathSet = paths as Set
		updateReadyFiles unitName, { TriggersFileEntity unit ->
			if(pathSet.contains(unit.path)){

				unit.status=TriggersFileEntity.STATUS.PROCESSED;

				unit.addFileUnitStatus(unitId, TriggersFileEntity.STATUS.PROCESSED)
				try{
					zlock?.unlock(unitName)
				}catch(Throwable t){
					t.printStackTrace();
				}

				return true;
			}
			else{ return false}
		}

	}

	/**
	 * Marks a file as processed
	 * @param unitName
	 * @param path
	 */
	public void markFileAsProcessed(String unitName, String unitId, String path){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}


		withTx { EntityManager em ->

			def file = TriggersFileEntity.getByNameAndPath(em, unitName, path)
			if(!file){
				file = new TriggersFileEntity(
						unitName:unitName,
						path:path,
						status:TriggersFileEntity.STATUS.PROCESSED
						)
			}

			file.addFileUnitStatus( unitId, TriggersFileEntity.STATUS.PROCESSED)
			file.setStatus TriggersFileEntity.STATUS.PROCESSED
			em.persist file

			try{
				zlock?.unlock(unitName)
			}catch(Throwable t){
				t.printStackTrace();
			}


		}
	}

	/**
	 * Marks a file as processed
	 * @param unitName
	 * @param path
	 */
	public void markFilesAsReady(String unitName, paths){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		def pathSet = paths as Set
		withTx { EntityManager em ->

			paths.each { path ->

				def file = TriggersFileEntity.getByNameAndPath(em, unitName, path as String)
				if(!file){
					file = new TriggersFileEntity(
							unitName:unitName,
							path:path,
							status:TriggersFileEntity.STATUS.READY
							)
				}

				em.persist file

			}
		}

	}

	/**
	 * Marks a file as ready, if the file doesn't already exist it will be created, else the entry is updated.<br/>
	 * Sets the date of the file entry equal to either the current date or that of the date fiels in the path.
	 * @param unitName 
	 * @param path
	 */
	public void markFileAsReady(String unitName, String path){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		//extract the date from the path
		Date fileDate = PathDateExtractor.extractDate(path)
		if(!fileDate){
			fileDate = new Date()
		}

		withTx { EntityManager em ->

			def file = TriggersFileEntity.getByNameAndPath(em, unitName, path)
			if(!file){
				//if no file we add a new one
				file = new TriggersFileEntity(
						unitName:unitName,
						path:path,
						date:fileDate,
						status:TriggersFileEntity.STATUS.READY
						)

			}else{
				//file.status = TriggersFileEntity.STATUS.READY
			}

			em.persist file
		}
	}

	/**
	 * Helper function that runs the closure inside a transaction
	 * @param entityManager if this entity manager is not null no new transaction is created.
	 * @param closure
	 */
	private Object withTx(EntityManager entityManager = null, Closure closure){

		Object ret = null

		if(instanceEntityManager){
			//this is a special instance of DBTriggerStore part of a global transaction
			ret = closure(instanceEntityManager)
		}else{
			EntityManager em = entityManagerFactory.createEntityManager()
			try{
				em.getTransaction().begin()

				ret = closure(em)

				em.flush();
				em.getTransaction().commit()

			}finally{
				em.close();
			}
		}

		return ret
	}

	/**
	 * Starts the static jpa entity manager factory only once.
	 * If any error an Exception is thrown.<br/>
	 * The ConfigObject must have the hibernate type properties (without the hibernate prefix).<br/>
	 * e.g.<br/>
	 * <pre>
	 * className='org.glue.trigger.persist.TriggerStoreModule'
	 isSingleton=false
	 config{
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
					Properties properties = config.toProperties('hibernate');
					properties.stringPropertyNames()?.each{ prop -> if(prop.contains("zk")){ properties.remove(prop);} }

					Ejb3Configuration cfg = new Ejb3Configuration();
					cfg.addAnnotatedClass TriggersFileEntity.class
					cfg.addAnnotatedClass TriggersCheckPointEntity.class
					cfg.addAnnotatedClass TriggerFileUnitStatus.class

					cfg.setProperties(properties)
					entityManagerFactory = cfg.buildEntityManagerFactory()

					//validate schema
					new SchemaValidator(cfg.getHibernateConfiguration()).validate()
				}

				if(!zlock){
					if(config.zkhost){
						println "Using zookeeper locking"
						long timeout =  (config.zktimeout) ? Long.parseLong(config.zktimeout.toString()) : 10000L
						zlock = new ZLock(new ZConnection(config.zkhost.toString(), timeout),
								"/glue-dbtriggerstore/", timeout);
					}else{
					    println "$config"
						println "No zookeeper locking is used"
					}
				}

			}
		}
	}

	public void shutdown(){
		synchronized (LOCK) {
			entityManagerFactory?.close()
		}
	}
}
