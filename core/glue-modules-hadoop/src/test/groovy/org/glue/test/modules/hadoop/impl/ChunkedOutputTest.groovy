package org.glue.test.modules.hadoop.impl;

import static org.junit.Assert.*

import org.glue.modules.hadoop.impl.ChunkedOutput
import org.junit.Test


class ChunkedOutputTest extends GroovyTestCase{

	@Test
	public void testGzip(){
		
		def dir = new File("target/chunkedoutputtest/" + System.currentTimeMillis())
		dir.mkdirs()
		
		def files = 0
		def output = new ChunkedOutput(dir, "part", 20, "gz", { files++ }) 
		
		(0 .. 1000000).each{
			output.write("Hi this is a test string")
		}
		
		output.close()

		assertEquals(6, files)
				
	}	
	
	
}
