package org.glue.gluetest

import groovy.util.ConfigObject

import org.apache.hadoop.hdfs.MiniDFSCluster
import org.glue.modules.hadoop.PigModule
import org.glue.unit.script.ScriptClassCache

/**
 * 
 * Setup an Pig Module that connects with the GlueServer.<br/>
 * This saves the user testing the workflow from having to pass ConfigObject's to<br/>
 * the glue server.
 * 
 */
class PigModuleBuilder implements ModuleBuilder{

	String name = "pig"
	boolean singleton = true
	File clusterProperties

	Map extraPigProperties
	Collection<String> externalLibraries
	
	Collection<String> javaOpts = ['-Xmx256m']
	
	public PigModuleBuilder(){
		
	}
	public PigModuleBuilder(Map extraPigProperties, Collection<String> externalLibraries){
		this.extraPigProperties = extraPigProperties
		this.externalLibraries = externalLibraries	
	}
	
	void setNativeLibraryPath(String path){
		javaOpts << "-Djava.library.path=$path"
	}
	
	/**
	 * Return a new instance of the module
	 * @param glueServer
	 * @return ConfigObject
	 */
	ConfigObject buildModule(GlueServer glueServer){

		//ensure that the dfs cluster has been started
		glueServer.startDFSCluster()

		clusterProperties = File.createTempFile(name, ".properties")

		MiniDFSCluster dfs = glueServer.getMiniCluster()

		clusterProperties.withWriter { Writer writer ->

			dfs.getFileSystem().getConf().getProps().each { String key, String val ->
				writer.write("$key=$val")
				writer.println()
			}
			
			extraPigProperties?.each {key, val ->
				writer.write("$key=$val")
				writer.println()
			}
			
		}
		
		println "!!!!!Pig javaOps=  '${javaOpts?.join('\',\'')}'"
		
		//use script class cache to reduce class loading and prevent permgen errors.
		return ScriptClassCache.getDefaultInstance().parse("""
		
				className='${PigModule.class.name}'
				//must never be a singleton
				isSingleton=true

				config{
					clusters{
						   $name{
							   pigProperties='${clusterProperties.absolutePath}'
							   isDefault=true
						   }
					   }
				 classpath=[ ${getClassPathString()} ]
				 javaOpts = [ '${javaOpts?.join('\',\'')}' ]
				
				}
		   """)
	}

	/**
	 * Gets the jars from:<br/>
	 * <ul>
	 *  <li>the class path parameter java.class.path</li>
	 *  <li>target/classes</li>
	 *  <li>target/test-classes</li>
	 *  <li>src/test/resources</li>
	 * </ul>
	 *  
	 * @return String
	 */
	String getClassPathString(){
		String classpath = System.getProperty("java.class.path")

		def sep = ','

		String pathStr = ""
		classpath.split(File.pathSeparator).eachWithIndex { str, int i ->
			if(i != 0) pathStr += sep

			pathStr += "\'$str\'"
		}

		pathStr += "$sep\'${new File('target/classes').absolutePath}\'"
		pathStr += "$sep\'${new File('target/test-classes/').absolutePath}\'"
		pathStr += "$sep\'${new File('src/test/resources').absolutePath}\'"

		externalLibraries?.each{
			pathStr += "$sep\'$it\'"
		}
		
		return pathStr
	}

	/**
	 * Cleanup any resources
	 */
	void close(){
		clusterProperties?.delete()
	}
}
