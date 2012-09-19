package org.glue.modules.test;

import static org.junit.Assert.*

import org.glue.modules.DbStoreModule
import org.glue.unit.exec.ProcessExecutor
import org.glue.unit.exec.UnitExecutor
import org.glue.unit.exec.impl.DefaultProcessExecutorProvider
import org.glue.unit.exec.impl.DefaultUnitExecutorProvider
import org.glue.unit.exec.impl.GlueExecutorImpl
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.om.Provider
import org.glue.unit.om.impl.DefaultGlueContextBuilder
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.om.impl.MapGlueModuleFactoryProvider
import org.glue.unit.repo.impl.MapGlueUnitRepository
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 *
 * Tests that the DBStoreModule works as expected.<br/>
 * Two parts to test:<br/>
 * <ul>
 *   <li>Module events are saved</li>
 *   <li>Key value pairs are saved and retrieved correctly</li>
 * </ul>
 * <p/>
 * Events tested are:<br/>
 * <pre>
 * void onUnitStart(GlueUnit unit, GlueContext context);
 * void onUnitFinish(GlueUnit unit, GlueContext context);
 * void onUnitFail(GlueUnit unit, GlueContext context);
 * 
 * Boolean canProcessRun(GlueProcess process, GlueContext context);
 * void onProcessStart(GlueProcess process,GlueContext context);
 * void onProcessFinish(GlueProcess process, GlueContext context);
 * void onProcessFail(GlueProcess process, GlueContext context, Throwable t);
 * </pre>
 */
class DBStoreModuleTest {

	DbStoreModule dbStoreModule

	@Test
	public void testGetSetKeyValuePair(){

		List keys = ['1', '2' , '3', '4']
		def values  = ['1':'a', '2':'b', '3':'c', '4':'d']

		keys.each { k -> dbStoreModule.setValue(k, values[k]) }

		keys.each { k ->
			def v = values[k]
			assertEquals(v, dbStoreModule.getValue(k))
		}

		//test remove
		dbStoreModule.setValue '1', null

		assertNull(dbStoreModule.getValue('1'))

		//test left shift
		dbStoreModule << ['1', 'q']
		assertNotNull ( dbStoreModule['1'] )

		//test putAt
		dbStoreModule['2'] = 'p'
		assertNotNull ( dbStoreModule['2'] )


	}


	
	/**
	* Tests that the DBStoreModule works as expected while executing as part of a GlueUnit.
	*/
   @Test
   public void testWithGlueExecutorDBKeyValueStore(){

	   
	   String keyName = String.valueOf(System.currentTimeMillis())
	   String value = 'abc'
	   
	   GlueModuleFactoryProvider moduleFactoryProvider = new MapGlueModuleFactoryProvider(new ConfigSlurper().parse("""
	   dbStore{
		   className='org.glue.modules.DbStoreModule'
		   isSingleton=true
		   config{
			   connection.username="sa"
			   connection.password=""
			   dialect="org.hibernate.dialect.HSQLDialect"
			   connection.driver_class="org.hsqldb.jdbcDriver"
			   connection.url="jdbc:hsqldb:mem:DbStoreModuleTestWithGlueExecutorDBKeyValueStore"
			   hbm2ddl.auto="create"
			   connection.autocommit="false"
			   show_sql="true"
			   cache.use_second_level_cache="false"
			   cache.provider_class="org.hibernate.cache.NoCacheProvider"
			   cache.use_query_cache="false"
		   }
	   }
	   """))

	   //we create three processed that will all insert and change the same key concurrently
	   GlueUnit unit = new DefaultGlueUnitBuilder().build("""
	   
		   name='test'
		   tasks{
		   
			  pA{
				tasks={ context->
				    println context.dbStore
				   context.dbStore << ['$keyName', '$value']
				}
			  }
			
		   }
	   
	   """)
	   
	   
	   ConfigObject executorConfig = new ConfigSlurper().parse("")
	   MapGlueUnitRepository repo = new MapGlueUnitRepository()
	   repo << unit
	   GlueContextBuilder contextBuilder = new DefaultGlueContextBuilder(moduleFactoryProvider)
	   Provider<ProcessExecutor> processExecutorProvider = new DefaultProcessExecutorProvider()
	   Provider<UnitExecutor> unitExecutorProvider = new DefaultUnitExecutorProvider(processExecutorProvider)
	   GlueUnitBuilder unitBuilder = new DefaultGlueUnitBuilder()

	   GlueExecutorImpl exec = new GlueExecutorImpl(
			   executorConfig,
			   repo,
			   contextBuilder,
			   unitExecutorProvider,
			   unitBuilder)


	   String uid = exec.submitUnitAsName(unit.name, [:])

	   //wait for the unit to complete
	   exec.waitFor uid

	   //now we check the database to see that the values exist as expected.
	   GlueModule dbStore = moduleFactoryProvider.get().getModule("dbStore")
	   
	   assertNotNull(dbStore)
	   assertEquals(value,  dbStore.getValue(keyName))
	   
   }

	
	@Before
	public void setUp() throws Exception {

		dbStoreModule = new DbStoreModule()

		ConfigObject config = new ConfigSlurper().parse("""
		
                connection.username="sa"
                connection.password=""
                dialect="org.hibernate.dialect.HSQLDialect"
                connection.driver_class="org.hsqldb.jdbcDriver"
                connection.url="jdbc:hsqldb:mem:dbStoreModuleTest"
                hbm2ddl.auto="create"
                connection.autocommit="false"
                show_sql="true"
                cache.use_second_level_cache="false"
                cache.provider_class="org.hibernate.cache.NoCacheProvider"
                cache.use_query_cache="false"
        
		
		""")

		dbStoreModule.init config
	}


	@After
	public void tearDown() throws Exception {
		dbStoreModule.shutdown()
	}
	
}
