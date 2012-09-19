package org.glue.modules;


import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

import org.glue.modules.db.KeyValueEntity
import org.glue.persist.KeyValueStore
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.hibernate.ejb.Ejb3Configuration
import org.hibernate.tool.hbm2ddl.SchemaValidator

/**
 *This module provides two features.<br/>
 *Acts as a global key value store to any GlueModule or Workflow<br/>
 *The initial implementation will be via an RDBMs abstracted via hibernate. The implementation is not so important as to the service this provides to all workflows and to glue itself.<br/>
 *Glue workflows can store state in a generic way without caring about db or other store setups. Modules written for glue can at any time use this store to provide persistent state. This creates a sort of central storage solution for glue where the setup and configuration is managed by one simple module.<br/>
 *Note that in no way does the implementation need to be centralised, we can create implementations for distributed key value stores, or just use a replicated rdbms, but for glue the interface and usage does not change.<br/>
 *<p/>
 *One static singleton EntityManagerFactory instance will be maintained.<br/>
 *For each method call to this module that requires access to the EntityManager a new EntityManager instance will be created along with a transaction,<br/>
 *both are closed in a finally statement after the method completed.
 * 
 */
@Typed
class DbStoreModule implements GlueModule, KeyValueStore{

	private static final Object LOCK = new Object()

	private static EntityManagerFactory entityManagerFactory

	private ConfigObject config=null;

	void destroy(){
		shutdown()
	}

	void onProcessKill(GlueProcess process, GlueContext context){
	}

	@Override
	public Map getInfo() {
		return config;
	}

	@Override
	public Boolean canProcessRun(GlueProcess process, GlueContext context) {
		return true
	}

	public void shutdown(){
		synchronized (LOCK) {
			entityManagerFactory?.close()
		}
	}

	@Override
	public String getName() {
		return 'dbstore';
	}

	/**
	 * Starts the static jpa entity manager factory only once.
	 * If any error an Exception is thrown.<br/>
	 * The ConfigObject must have the hibernate type properties (without the hibernate prefix).<br/>
	 * e.g.<br/>
	 * <pre>
	 * className='org.glue.modules.DbStoreModule'
	 isSingleton=true
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
		this.config=config;
		if(!entityManagerFactory){

			synchronized (LOCK){
				if(!entityManagerFactory){
					Ejb3Configuration cfg = new Ejb3Configuration();
					cfg.addAnnotatedClass KeyValueEntity.class
					cfg.setProperties(config.toProperties('hibernate'))
					entityManagerFactory = cfg.buildEntityManagerFactory()

					new SchemaValidator(cfg.getHibernateConfiguration()).validate()
				}
			}
		}
	}

	public String getAt(String key){
		return getValue(key)
	}
	public String getAt(Object key){
		return getValue(key)
	}

	public void putAt(String key, Object value){
		setValue(key, value)
	}

	public void putAt(Object key, Object value){
		setValue(key, value)
	}

	public void leftShift(Object key, Object value){
		setValue(key, value)
	}

	/**
	 * Stores a key and value pair.<br/>
	 * To remove a key value pair just set the value == null
	 * @param key
	 * @param value
	 * @return
	 */
	public void setValue(Object key, Object value){

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()
		try{
			KeyValueEntity keyValue = em.find(KeyValueEntity.class, key?.toString())

			if(value){

				if(!keyValue){
					keyValue = new KeyValueEntity(key:key?.toString(), value:value?.toString())
				}

				println "Storing key: $key"
				em.persist keyValue
			}else{
				//if value is null we remove it
				if(keyValue){
					em.remove keyValue
				}
			}
			em.getTransaction().commit()
		}finally{
			em.close()
		}
	}


	/**
	 * Retrieves value for the key specified
	 * @param key
	 * @param value
	 * @return String
	 */
	public String getValue(Object key){
		KeyValueEntity keyValue = null

		EntityManager em = entityManagerFactory.createEntityManager()
		em.getTransaction().begin()
		try{

			keyValue = em.find(KeyValueEntity.class, key?.toString())
			
			em.getTransaction().commit()
		}finally{
			em.close()
		}

		return keyValue?.value
	}



	@Override
	public void onUnitFail(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
	}
	@Override
	public void configure(String unitId, ConfigObject config) {
	}

	@Override
	public void onProcessFail(GlueProcess process, GlueContext context,	Throwable t) {
	}

	@Override
	public void onProcessFinish(GlueProcess process, GlueContext context) {
	}

	@Override
	public void onProcessStart(GlueProcess process, GlueContext context) {
	}
}
