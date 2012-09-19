package org.glue.gluetest

import groovy.util.ConfigObject

import org.glue.modules.SqlModule
import org.glue.unit.script.ScriptClassCache

class SQLModuleBuilder implements ModuleBuilder{

	String name = "sql"
	boolean singleton = false
	Collection<String> databases

	public SQLModuleBuilder(String... databases){
		this(Arrays.asList(databases))
	}

	public SQLModuleBuilder(Collection<String> databases){
		this.databases = databases
		if(databases.size() < 1){
			throw new RuntimeException("The databases collection must contain at least one database name")
		}
	}

	/**
	 * Return a new instance of the module
	 * @param glueServer
	 * @return ConfigObject
	 */
	ConfigObject buildModule(GlueServer glueServer){

		//ensure that the dfs cluster has been started
		glueServer.startDBServer(databases)

		//check that database names exist
		//this is very important to check, because the hsqldb server migh have been
		//started by some other process.
		def dbNames = Arrays.asList(glueServer.getHsqldbServer()?.getDBNameArray())
		
		if(!dbNames.containsAll(databases as Object[])){
			throw new RuntimeException("The GlueServer must have its HsqlDbServer ($dbNames) started with a databases $databases")
		}


		def dbSections = ""

		def user ="sa", pwd = ""
		def driver = glueServer.getJDBDriverClassName()


		databases.each { dbName ->
			def jdbc = glueServer.getJDBCConnectionString(dbName)

			dbSections += """
			
			  $dbName{
			    host='$jdbc'
			    user='$user'
			    pass='$pwd'
			    driver='$driver'
			  }
			
			"""
		}


		//use script class cache to reduce class loading and prevent permgen errors.
		return ScriptClassCache.getDefaultInstance().parse("""
	   
			   className='${SqlModule.class.name}'
			   //must never be a singleton
			   isSingleton=false
			   config{
			     db{
			       $dbSections
			     }
			   }
		  """)
	}

	/**
	 * Cleanup any resources
	 */
	void close(){
	}
}
