package org.glue.gluetest

import groovy.util.ConfigObject

import org.apache.hadoop.hdfs.MiniDFSCluster
import org.glue.trigger.service.hdfs.HdfsTriggerModule
import org.glue.unit.script.ScriptClassCache

/**
 * 
 * Hdfs trigger module builder for testing with GlueServer
 *
 */
class HdfsTriggerModuleBuilder implements ModuleBuilder{


	static String databaseName = 'triggerdb'

	String name = "hdfsTriggers"
	boolean singleton = true
	File clusterProperties
	Collection<String> clusterNames
	String defaultClusterName

	public HdfsTriggerModuleBuilder(){
		clusterNames = [name]
		defaultClusterName = name
	}

	public HdfsTriggerModuleBuilder(String defaultClusterName, Collection<String> clusterNames){
		this.clusterNames = clusterNames
		this.defaultClusterName = defaultClusterName
	}

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


		//ensure that the dfs cluster has been started
		glueServer.startDFSCluster()

		clusterProperties = File.createTempFile(name, ".properties")

		MiniDFSCluster dfs = glueServer.getMiniCluster()

		clusterProperties.withWriter { Writer writer ->

			dfs.getFileSystem().getConf().getProps().each { String key, String val ->
				writer.write("$key=$val")
				writer.println()
			}
		}

		def sections = ""
		def isDefault = false
		clusterNames.each { clusterName ->


			isDefault = (clusterName == defaultClusterName)

			sections += """
		
			   			$clusterName{
               				hdfsProperties='${clusterProperties.absolutePath}'
               				isDefault=$isDefault
           				}
			"""
		}

		//use script class cache to reduce class loading and prevent permgen errors.
		return ScriptClassCache.getDefaultInstance().parse("""
		
				className='${HdfsTriggerModule.class.name}'
				//must never be a singleton
				isSingleton=true
				config{
					clusters{
        				$sections
           			}
           		
           		
           		pollingThreads='10'
           		pollingPeriod='1000'
           		repoPollingPeriod='5000'
    
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
		                show_sql="false"
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
