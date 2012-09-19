package org.glue.gluetest.test;

import static org.junit.Assert.*
import groovy.sql.Sql

import java.sql.Connection
import java.sql.DriverManager

import org.apache.hadoop.fs.Path
import org.glue.gluetest.SimpleGlueServer
import org.glue.gluetest.util.GlueServerBootstrap
import org.glue.unit.exec.GlueState
import org.junit.Test

/**
 * 
 * Tests that the GlueServer is usable.
 *
 */
class GlueServerTest {

	/**
	 * Test that we can start a db server with multiple databases
	 */
	@Test
	public void testDBServer(){

		SimpleGlueServer server = GlueServerBootstrap.createServer()
		server.startDBServer(['mydb1', 'mydb2'])


		Thread.sleep(2000)
		String jdbc = server.getJDBCConnectionString("mydb1")
		try{
			def sql = Sql.newInstance(jdbc, "sa",
					"", server.getJDBDriverClassName())

			sql.execute("CREATE TABLE myTest ( name VARCHAR );")
			sql.execute("INSERT INTO myTest (name) VALUES('hi')")
			sql.close()
		}finally{
			server.stopDBServer()
		}
	}


	/**
	 * Test that we can start an HDFS Mini cluster
	 */
	@Test
	public void testDFS(){

		SimpleGlueServer server = GlueServerBootstrap.createServer()
		server.startDFSCluster()
		def fs = server.miniCluster.getFileSystem()

		assertNotNull(fs)

		//add file to test
		File file = new File('target/test/GlueServerTest/testDFS/test.txt')
		file.mkdirs()

		if(file.exists()){
			file.delete()
		}
		file.createNewFile()

		file.text = "HI"

		Path localFile = new Path(file.absolutePath)
		Path remoteFile = new Path('/tmp/GlueServerTest/testDFS/test.txt')

		try{
			fs.copyFromLocalFile localFile, remoteFile

			//assert that the file exists
			assertTrue(fs.exists(remoteFile))

			File remoteLocalCopyFile = new File('target/test/GlueServerTest/testDFS/copy.txt')
			remoteLocalCopyFile.delete()
			try{
				Path remoteLocalCopy = new Path(remoteLocalCopyFile.absolutePath)

				fs.copyToLocalFile(remoteFile, remoteLocalCopy)

				assertEquals("HI", remoteLocalCopyFile.text)
			}finally{
				remoteLocalCopyFile.delete()
			}
		}finally{
			file.delete()
		}
	}

	/**
	 * Test that we can start a Glue Server
	 */
	@Test
	public void testStartup(){

		SimpleGlueServer server = GlueServerBootstrap.createServer()
		server.start()
		server.stop()
	}

	/**
	 * Test that we can start a glue server and sumbmit a workflow
	 */
	@Test
	public void testSubmitGlueUnit(){

		SimpleGlueServer server = GlueServerBootstrap.createServer()
		server.start()

		def unitId = server.exec.submitUnitAsText(
				"""
			name='test'
			tasks{
			 processA{
			   tasks={ context ->
			     println 'Hi'
			   }
			 }
			}
			""" as String

				, [:])

		server.exec.waitFor('test')

		Thread.sleep(500)

		def status = server.unitStatusManager.getUnitStatus(unitId)
		assertNotNull(status)
		assertEquals(GlueState.FINISHED, status.status)

		server.stop()
	}
}
