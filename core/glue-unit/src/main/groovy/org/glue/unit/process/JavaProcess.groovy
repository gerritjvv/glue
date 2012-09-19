package org.glue.unit.process

import java.util.concurrent.atomic.AtomicBoolean

import org.glue.unit.process.util.ClosureAppendable

/**
 * 
 * Represents a JavaProcess.
 * Each java process can have:<br/>
 * Main Class<br/>
 * Working directory<br/>
 * Java options<br/>
 * Class path<br/>
 * <p/>
 * This class encapsulates the work needed to run a Java process from another.
 *
 */
@Typed
class JavaProcess {

	File workingDirectory

	/**
	 * Contains the classpath items, each item is seperated with : or ; depending on the OS.
	 */
	Set<String> classpath = []
	/**
	 * Contains the java options e.g. -Xmx:1024M ect.
	 */
	Set<String> javaOpts = []

	/**
	 * A class that implements the main(args) method, as required by java.
	 */
	String mainClass

	Process process

	int exitValue = 0

	private AtomicBoolean isKilled = new AtomicBoolean(false)

	/**
	 * Add jars recursively in directory $dir to the class path 
	 * @param dir
	 */
	def addJars(File dir){

		dir.eachFileRecurse { File file ->

			if(file.isFile() && file.name.endsWith('.jar')){
				classpath << file.absolutePath
			}
		}
	}

	/**
	 * True if the process is still running
	 * @return boolean 
	 */
	boolean isRunning(){
		try{
			process.exitValue()
			return false
		}catch(IllegalThreadStateException e){
			//following the javadoc process will throw an exception
			//if its still running.
			return true
		}
	}

	void kill(){
		isKilled.set true
		process?.destroy()
	}

	void run(Collection<String> args) throws Throwable{
		def cl = {println it}
		run(args, cl)
	}

	@Typed(TypePolicy.DYNAMIC)
	void run(Collection<String> args, Closure stdout)throws Throwable{

		if(mainClass == null){
			throw new RuntimeException("JavaProcess cannot run without a main class")
		}


		//get the java command
		String javaCmd = getJavaCommand()

		def processCmd = [
			javaCmd,
		]

		javaOpts.each { processCmd << it.toString() }

		String  sep = File.pathSeparator
		
		String clsPathStr = classpath.join(sep)
		
		if(clsPathStr?.trim()?.size() > 0){
			processCmd << "-cp"
			processCmd << clsPathStr
		}
		
		processCmd << mainClass
		
		args?.each { processCmd << it.toString() }
		
		println processCmd
		def pb = new ProcessBuilder(processCmd)

		pb.redirectErrorStream(true)

		if(workingDirectory)
			pb.directory(workingDirectory)


		def appendable = new ClosureAppendable(stdout)

		//start process and wait until exit
		process = pb.start()
		process.waitForProcessOutput(appendable as OutputStream, appendable as OutputStream)
		
		//only process if not killed
		if(!isKilled.get()){
			exitValue = process.exitValue()
			//any value lower than 0 is considered an error
			if(exitValue != 0){
				throw new RuntimeException("Error ($exitValue) in command: ${mainClass}")
			}
		}
	}

	void waitFor(){
		process.waitFor()	
	}
	
	/**
	 * Tries to get the java command from the java home
	 * If the java command cannot be found an exception is thrown
	 * @return String
	 */
	private String getJavaCommand(){

		def cmd = "java"
		try{
			//we do not check the return code because if java does not exist as a process
			//an exception will be thrown
			cmd.execute().waitFor()
		}catch(IOException t){
			//try finding java from java home
			def JAVA_HOME = System.getenv('JAVA_HOME')
			cmd = "$JAVA_HOME/bin/java"

			cmd.execute().waitFor()

		}

		return cmd
	}
}
