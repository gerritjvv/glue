package org.glue.gluetest

import org.apache.hadoop.hdfs.MiniDFSCluster
import org.glue.modules.hadoop.impl.HDFSModuleImpl
import org.glue.unit.script.ScriptClassCache


/**
 * 
 * Setup an Hadoop Module that connects with the GlueServer.<br/>
 * This saves the user testing the workflow from having to pass ConfigObject's to<br/>
 * the glue server.
 * <p/>
 * All cluster names point to the same hdfs and mapred clusters.
 */
class HDFSModuleBuilder implements ModuleBuilder{


	String name = "hdfs"
	boolean singleton = true
	File clusterProperties

	Collection<String> clusterNames
	String defaultCluster
	Map config
	
	HDFSModuleBuilder(){
		clusterNames = [name]
		defaultCluster = name
	}

	HDFSModuleBuilder(String defaultCluster, Collection<String> clusterNames){
		this.defaultCluster = defaultCluster
		this.clusterNames = clusterNames
	}

	HDFSModuleBuilder(String defaultCluster, Collection<String> clusterNames, Map config){
		this.defaultCluster = defaultCluster
		this.clusterNames = clusterNames
		this.config = config
	}
	
	/**
	 * Return a new instance of the module
	 * @param glueServer
	 * @return ConfigObject
	 */
	ConfigObject buildModule(GlueServer glueServer){
		//ensure tdhat the dfs cluster has been started
		glueServer.startDFSCluster(config)
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
			
			isDefault = (clusterName == defaultCluster)
			
			sections += """
		
			   			$clusterName{
               				hdfsProperties='${clusterProperties.absolutePath}'
               				isDefault=$isDefault
           				}
			""" }

		//use script class cache to reduce class loading and prevent permgen errors.
		return ScriptClassCache.getDefaultInstance().parse("""
		
				className='${HDFSModuleImpl.class.name}'
				//must never be a singleton
				isSingleton=true
				config{
					clusters{
        				$sections
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
