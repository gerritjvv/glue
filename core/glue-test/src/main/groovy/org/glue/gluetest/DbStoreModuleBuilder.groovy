package org.glue.gluetest

import org.glue.modules.DbStoreModule
import org.glue.unit.script.ScriptClassCache


/**
 * This store expects the hsql server to have been started already 
 */
class DbStoreModuleBuilder implements ModuleBuilder{

	public static final String DB_NAME = 'dbstore'

	String name = DB_NAME

	boolean singleton = true

	String dbName = DB_NAME

	public DbStoreModuleBuilder(){
	}

	/**
	 * The database name started with the sql server
	 * @param dbName
	 */
	public DbStoreModuleBuilder(String dbName){
		this.dbName = dbName
	}

	/**
	 * Return a new instance of the module
	 * @param glueServer
	 * @return ConfigObject
	 */
	ConfigObject buildModule(GlueServer glueServer){

		def dbNames = Arrays.asList(glueServer.getHsqldbServer()?.getDBNameArray())

		if(!dbNames.contains(dbName)){
			throw new RuntimeException("The GlueServer must have its HsqlDbServer ($dbNames) started with a database $dbName")
		}

		def driver = glueServer.getJDBDriverClassName()
		def jdbc = glueServer.getJDBCConnectionString(dbName)

		//use script class cache to reduce class loading and prevent permgen errors.
		return ScriptClassCache.getDefaultInstance().parse("""
	   
			   className='${DbStoreModule.class.name}'
			   //must never be a singleton
			   isSingleton=true
			   config{
			    host='$jdbc' 
                connection.username='sa'
                connection.password=''
                dialect='org.hibernate.dialect.HSQLDialect'
                connection.driver_class='$driver'
                connection.url='$jdbc'
                hbm2ddl.auto="update"
                connection.autocommit="false"
                show_sql="true"
                cache.use_second_level_cache="false"
                cache.provider_class="org.hibernate.cache.NoCacheProvider"
                cache.use_query_cache="false"
                connection.provider_class="org.hibernate.connection.C3P0ConnectionProvider"
                c3p0.min_size="1"
                c3p0.max_size="100"
                c3p0.timeout="1800"
                c3p0.max_statements="500"
			   }
		  """)
	}

	/**
	 * Cleanup any resources
	 */
	void close(){
	}
}
