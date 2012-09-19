package org.glue.gluetest

import groovy.util.ConfigObject

import org.glue.trigger.service.hdfs.HdfsTriggerStoreModule
import org.glue.unit.script.ScriptClassCache

/**
 * 
 * Hdfs trigger module builder for testing with GlueServer
 *
 */
class HdfsTriggerStoreModuleBuilder implements ModuleBuilder{


	static String databaseName = 'triggerdb'

	String name = "hdfsTriggerStore"
	boolean singleton = true

	/**
	 * Return a new instance of the module
	 * @param glueServer
	 * @return ConfigObject
	 */
	ConfigObject buildModule(GlueServer glueServer){

		String user = "sa", pwd = ""
		String driver = glueServer.getJDBDriverClassName()
		String jdbc = glueServer.getJDBCConnectionString(databaseName)

		glueServer.startDBServer([databaseName])

		def dbNames = Arrays.asList(glueServer.getHsqldbServer()?.getDBNameArray())

		if(!dbNames.contains(databaseName)){
			throw new RuntimeException("The GlueServer must have its HsqlDbServer ($dbNames) started with a database $databaseName")
		}


		//use script class cache to reduce class loading and prevent permgen errors.
		return ScriptClassCache.getDefaultInstance().parse("""
		
				className='${HdfsTriggerStoreModule.class.name}'
				//must never be a singleton
				isSingleton=true
				config{
				  triggerStore{
					className='org.glue.trigger.persist.db.DBTriggerStore'
					config{
		                host='$jdbc' 
		                connection.username='$user'
		                connection.password='$pwd'
		                dialect="org.hibernate.dialect.HSQLDialect"
		                connection.driver_class='$driver'
		                connection.url='$jdbc'
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
			     }
		
	     	   }
           		
           """
		)
	}


	/**
	 * Cleanup any resources
	 */
	void close(){
		clusterProperties?.delete()
	}
}
