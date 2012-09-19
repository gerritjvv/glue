package org.glue.unit.exec.impl;

import static org.junit.Assert.*

import org.glue.unit.log.impl.GlueExecLoggerImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass
import org.junit.Test

class GlueExecLoggerImplTest {

	static File baseDir

	@Test
	public void testWrite(){

		GlueExecLoggerImpl logger = new GlueExecLoggerImpl(baseDir)

		logger.out('Hi\n')
		logger.out('processA', 'Hi\n')

		logger.err('Hi\n')
		logger.err('processA', 'Hi\n')

		logger.close()
		
		File[] files = baseDir.listFiles()
		
		assertNotNull(files)
		assertEquals(2, files.size())

		files.each { File file ->

			def lineCount = 0
			file.eachLine { lineCount++ }
			assertEquals(2, lineCount)
		}
	}


	@BeforeClass
	public static void setup(){
		baseDir = new File('target/test/GlueExecLoggerImplTest')
		if(baseDir.exists()){
			baseDir.deleteDir()
		}
		
		baseDir.mkdirs()
		
	}

	@AfterClass
	public static void after(){
		baseDir.deleteDir()
	}
}
