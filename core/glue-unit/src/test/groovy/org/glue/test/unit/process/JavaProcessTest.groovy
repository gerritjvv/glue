package org.glue.test.unit.process
import static org.junit.Assert.*

import org.glue.unit.process.JavaProcess
import org.junit.Test

/**
 * 
 * Test that JavaProcess.<br/>
 * We test that:
 * It can run a simple java program.<br/>
 * The java class path and error reporting is done correctly.<br/>
 *
 */
class JavaProcessTest {

	/**
	 * Runs and then kills a program that will never end
	 */
	@Test(timeout=10000L)
	public void runKillProgram(){

		String classpath = System.getProperty("java.class.path")

		JavaProcess process = new JavaProcess()
		process.mainClass = NeverEndProgram.class.name

		classpath.split(File.pathSeparator).each { process.classpath << it }
		process.classpath << new File("target/classes/").absolutePath
		process.classpath << new File("target/test-classes/").absolutePath
		process.javaOpts << "-Xmx128m"

		process.classpath << "src/test/resources"

		Thread.start{
			process.run([])
		}

		Thread.sleep(1000L)
		assertTrue(process.isRunning())

		process.kill()

		Thread.sleep(2000L)
		assertFalse(process.isRunning())
	}


	/**
	 * Runs a program that is expected to throw an exception
	 */
	@Test(expected=RuntimeException)
	public void runFailProgram(){

		String classpath = System.getProperty("java.class.path")

		JavaProcess process = new JavaProcess()
		process.mainClass = FailProgram.class.name

		classpath.split(File.pathSeparator).each { process.classpath << it }
		process.classpath << new File("target/classes/").absolutePath
		process.classpath << new File("target/test-classes/").absolutePath
		process.javaOpts << "-Xmx128m"

		process.classpath << "src/test/resources"

		process.run([])
	}


	/**
	 * Runs a program that is expected to work
	 */
	@Test
	public void runSimpleProgram(){

		String classpath = System.getProperty("java.class.path")

		JavaProcess process = new JavaProcess()
		process.mainClass = OKProgram.class.name

		classpath.split(File.pathSeparator).each { process.classpath << it }
		process.classpath << new File("target/classes/").absolutePath
		process.classpath << new File("target/test-classes/").absolutePath
		process.javaOpts << "-Xmx128m"

		process.classpath << "src/test/resources"

		process.run([])

		assertEquals(0, process.exitValue)
	}
}
