package org.glue.test.log.impl;

import java.io.File

import org.glue.unit.log.impl.GlueExecLoggerImpl
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

/**
 * 
 * Tests that the GlueExecLoggImpl works as expected
 *
 */
public class TestGlueExecLoggerImpl {

	static File baseDir

	/**
	 * Test writting output then reading it.
	 */
	@Test
	public void testTail(){
		File dir = new File(baseDir, "testTail")

		def lines = 100

		def logger = new GlueExecLoggerImpl(dir)

		logger.setCurrentThreadProcessName("myprocesskey")
		//this should write to the current process key
		(1..lines).each {
			logger.getOutputStream().write ( "myprocesskey_$it\n".getBytes("UTF-8") )
		}

		logger.close();

		//assert that both files mail and myprocesskey exsits
		assertTrue(new File(dir, "myprocesskey").exists())

		//test input getting last 10 lines
		String str = logger.tailLog("myprocesskey", 10)
		assertNotNull(str)
		def splits = str.split('\n')
		assertEquals(10, splits.size())
		int testIndex = 91
		splits.each { line ->
			assertTrue(line.endsWith("_${testIndex}"))
			testIndex++
		}


	}

	/**
	 * Test writting output then reading it.
	 */
	@Test
	public void testGetLogText(){
		File dir = new File(baseDir, "testGetLogText")

		def lines = 100

		def logger = new GlueExecLoggerImpl(dir)

		logger.setCurrentThreadProcessName("myprocesskey")
		//this should write to the current process key
		(1..lines).each {
			logger.getOutputStream().write ( "myprocesskey_$it\n".getBytes("UTF-8") )
		}

		logger.close();

		//assert that both files mail and myprocesskey exsits
		assertTrue(new File(dir, "myprocesskey").exists())

		//test input gettings lines 10 - 20
		String str = logger.getLogText("myprocesskey", 10, 10)
		assertNotNull(str)
		def splits = str.split('\n')
		assertEquals(10, splits.size())
		int testIndex = 10
		splits.each { line ->
			assertTrue(line.endsWith("_${testIndex}"))
			testIndex++
		}


	}


	/**
	 * Test writting output then reading it.
	 */
	@Test
	public void testLogOutputInput(){
		File dir = new File(baseDir, "testLogOutputInput")

		def logger = new GlueExecLoggerImpl(dir)

		logger.setCurrentThreadProcessName("myprocesskey")
		//this should write to the current process key
		logger.getOutputStream().write ( "myprocesskey".getBytes("UTF-8") )


		logger.close();

		//assert that both files mail and myprocesskey exsits
		assertTrue(new File(dir, "myprocesskey").exists())
		assertEquals("myprocesskey", new File(dir, "myprocesskey").text)

		//test input
		def line = logger.getLogReader("myprocesskey").readLine()
		assertNotNull(line)
		assertEquals("myprocesskey", line)

	}



	/**
	 * Test setting the current thread process name
	 */
	@Test
	public void testLogCurrentProcessOutput(){
		File dir = new File(baseDir, "testLogCurrentProcessOutput")

		def logger = new GlueExecLoggerImpl(dir)

		logger.setCurrentThreadProcessName("myprocesskey")
		//this should write to the current process key
		logger.getOutputStream().write ( "myprocesskey".getBytes("UTF-8") )


		logger.close();

		//assert that both files mail and myprocesskey exsits
		assertTrue(new File(dir, "myprocesskey").exists())
		assertEquals("myprocesskey", new File(dir, "myprocesskey").text)
	}


	/**
	 * Test writing out log data to main and a process key
	 */
	@Test
	public void testLogOutput(){
		File dir = new File(baseDir, "testLogOutput")

		def logger = new GlueExecLoggerImpl(dir)
		logger.setCurrentThreadProcessName(null)

		logger.getOutputStream().write( "mainlog".getBytes("UTF-8") )
		logger.getOutputStream("myprocesskey").write ( "myprocesskey".getBytes("UTF-8") )


		logger.close();

		//assert that both files mail and myprocesskey exsits
		assertTrue(new File(dir, "main").exists())
		assertTrue(new File(dir, "myprocesskey").exists())
		assertEquals("mainlog", new File(dir, "main").text)
		assertEquals("myprocesskey", new File(dir, "myprocesskey").text)
	}


	@BeforeClass
	public static void setup(){

		baseDir = new File("target/tests/TestGlueExecLoggerImpl")
		baseDir.mkdirs()
	}

	@AfterClass
	public static void shutdown(){
		baseDir.deleteDir()
	}
}
