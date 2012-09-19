package org.glue.unit.process

import java.io.File;
import java.util.Set;

import org.glue.unit.om.Provider

/**
 * Is a helper class for a JavaProcess provider
 *
 */

class DefaultJavaProcessProvider extends Provider<JavaProcess>{

	String mainClass
	Set<String> classpath = []
	Set<String> javaOpts = []
	File workingDirectory
	

	/**
	 * Add the current java.class.path to the classpath variable, plus the paths:
	 * <ul> 
	 *  <li>target/classes</li>
	 *  <li>target/test-classes</li>
	 * </ul>
	 */
	void addCurrentClassPath(){
		String sysClassPath = System.getProperty("java.class.path")
		
		
		sysClassPath.split(File.pathSeparator).each { classpath << it }
		//we add the target/classes and test-classes to provide default
		//maven testing support
		classpath << new File("target/classes/").absolutePath
		classpath << new File("target/test-classes/").absolutePath
		
	}
	
	/**
	 * Returns a new instance of JavaProcess with the same configuration parameters<br/>
	 * that the DefaultJavaProcessProvider has.
	 */
	JavaProcess get(){
		
		def process = new JavaProcess(
			workingDirectory:workingDirectory,
			mainClass:mainClass, 
			javaOpts:javaOpts)	
		
		classpath?.each { item ->
			
			File file = new File(item)
			if(file.isDirectory()){
				process.addJars(file)
				process.classpath << item
			}else{
				process.classpath << item
			}
			
		}

		return process
	}
	
}
