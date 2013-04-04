package org.glue.test.modules.hadoop.impl

import static org.junit.Assert.*

import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FSDataInputStream
import org.apache.hadoop.fs.FSDataOutputStream
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.hdfs.MiniDFSCluster
import org.glue.modules.hadoop.HDFSModule
import org.glue.modules.hadoop.impl.HDFSModuleImpl
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class HDFSModuleImplTest{
	
	static HDFSModule hdfsModule
	static MiniDFSCluster dfsCluster
	
	@Test
	public void testDownloadChunked(){
		
		String dir = "target/test/HFDSModuleImplTest/testDownloadChunked/findfiles/"
		String newFile = "$dir/newFile.txt"
		String localFile = "src/test/resources/testlog.txt"
		
		String localDir = "target/test/hdfsModuleImpTest/testDownoadChunked/localfiles"
		new File(localDir).mkdirs()
		
		//put file on hdfs
		hdfsModule.put localFile, newFile
		
		int files = 0
		//download the chunked files
		hdfsModule.downloadChunked dir, localDir, 100, "gz", {files++}
		
		assertEquals(1, files)
	}
	
	@Test
	public void testFindNewFiles(){
		
		String newFile = "target/test/HFDSModuleImplTest/hdfs/findfiles/newFile.txt"
		String localFile = "src/test/resources/testlog.txt"
		
		//put file on hdfs
		hdfsModule.put localFile, newFile
		
		//put other files
		(0..10).each { i ->
			hdfsModule.put localFile, "target/test/HFDSModuleImplTest/hdfs/findfiles/file${i}.txt"
		}
//		void findNewFiles(String clusterName, String file, Closure dirHasBeenModified, Closure fileIsNew, Closure closure){
		boolean foundFile = false
		hdfsModule.findNewFiles("target/test/HFDSModuleImplTest/hdfs", 
			{ FileStatus status -> status.path.toString().contains("findfiles")},
			{ FileStatus status -> if(status.path.toString().endsWith("newFile.txt")) foundFile = true; })
		
		assertTrue(foundFile)
		
	}
	 
	
	
	/**
	* Test that we can cat a file out
	*/
   @Test
   public void testCatSpecifyFileCreate(){
	   
	   String hdfsFile = "target/test/HDFSModuleImplTest/hdfs/${System.currentTimeMillis()}"
	   
	   String localFile = "src/test/resources/testlog.txt"
	   try{
	   int lineCount = 0
	   new File(localFile).eachLine { lineCount++ }
	   
	   String localCatFile = 'target/test/HDFSModuleImplTest/local/testCatSpecifyFileCreate.txt'
	   
	   //put file on hdfs
	   hdfsModule.put localFile, hdfsFile
	   
	   //run cat
	   String fileName = hdfsModule.cat(hdfsFile, localCatFile)
	   
	   assertTrue(new File(localCatFile).exists())
	   
	   //run count again
	   int verifyCount = 0
	   new File(localCatFile).eachLine { verifyCount++ }
	   
	   assertEquals(lineCount, verifyCount)
	   }finally{
	   	new File(hdfsFile).delete()
	   }
   }
	
	/**
	* Test that we can cat a file out
	*/
   @Test
   public void testCatDefaultFileCreate(){
	   
	   String localFile = "src/test/resources/testlog.txt"
	   
	   int lineCount = 0 
	   new File(localFile).eachLine { lineCount++ } 
	   
	   String hdfsFile = "target/test/HDFSModuleImplTest/hdfs/testCatDefaultFileCreate.txt"
	   //put file on hdfs
	   hdfsModule.put localFile, hdfsFile
	   
	   //run cat
	   String fileName = hdfsModule.cat(hdfsFile)
	   
	   assertTrue(new File(fileName).exists())
	   
	   //run count again
	   int verifyCount = 0
	   new File(fileName).eachLine { verifyCount++ }
	   
	   assertEquals(lineCount, verifyCount)
	   
   }
   
	
	/**
	 * Test that we can put a file without errors
	 */
	@Test
	public void testPut(){
		
		String localFile = "src/test/resources/testlog.txt"
		
		hdfsModule.put localFile, "target/test/HDFSModuleImplTest/hdfs/testlog.txt"
	}
	
	/**
	 * Test that we can put a file and the get.
	 */
	@Test
	public void testGet(){
		
		String localFile = "src/test/resources/testlog.txt"
		String localDest = "target/test/HDFSModuleImplTest/testGet"
		
		File localDestFile = new File(localDest)
		if(localDestFile.exists()){
			localDestFile.delete()
		}
		
		hdfsModule.put localFile, "target/test/HDFSModuleImplTest/hdfs/testlogTestGet.txt"
		hdfsModule.get "target/test/HDFSModuleImplTest/hdfs/testlogTestGet.txt", localDest
		
		File testFile = new File(localDest)
		
		assertTrue( testFile.exists() )
		
		assertTrue ( FileUtils.contentEquals(new File(localFile), localDestFile) )
	}
	
	/**
	 * Test that we can delete a file without errors
	 */
	@Test
	public void testDelete(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteFile = "target/test/HDFSModuleImplTest/hdfs/testlogTestDelete.txt"
		
		hdfsModule.put localFile, remoteFile
		
		hdfsModule.delete remoteFile
	}
	
	/**
	 * Test that we can mkdirs without errors
	 */
	@Test
	public void testMkdirs(){
		
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testlogTestMkdirs"
		
		hdfsModule.mkdirs remoteDir
	}
	
	/**
	 * Test isDir && isFile
	 */
	@Test
	public void testIsDirAndIsFile(){
		
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testlogTestMkdirs_testIsDirAndIsFile"
		
		hdfsModule.mkdirs remoteDir
		
		assertTrue( hdfsModule.isDirectory(remoteDir) )
		assertFalse( hdfsModule.isFile(remoteDir) )
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteFile = "target/test/HDFSModuleImplTest/hdfs/testIsDirAndIsFile.txt"
		
		hdfsModule.put localFile, remoteFile
		
		
		assertFalse(hdfsModule.isDirectory(remoteFile) )
		assertTrue( hdfsModule.isFile(remoteFile) )
	}
	
	
	
	/**
	 * Test that we can check for file exists
	 */
	@Test
	public void testExists(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteFile = "target/test/HDFSModuleImplTest/hdfs/testlogTestExists.txt"
		hdfsModule.put localFile, remoteFile
		
		assertTrue( hdfsModule.exist(remoteFile) )
		
		assertFalse( hdfsModule.exist("" + System.currentTimeMillis()) )
	}
	
	
	/**
	 * Test that we can read a file from hdfs
	 */
	@Test
	public void testOpen(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteFile = "target/test/HDFSModuleImplTest/hdfs/testlogTestExists.txt"
		hdfsModule.put localFile, remoteFile
		
		FSDataInputStream input = hdfsModule.open(remoteFile)
		
		BufferedReader remoteBuffIn = new BufferedReader(new InputStreamReader(input))
		BufferedReader localBuffIn = new BufferedReader(new FileReader(localFile))
		
		try{
			String localLine = null;
			String remoteLine = null
			while( (localLine = localBuffIn.readLine()) != null &&  (remoteLine = remoteBuffIn.readLine()) != null){
				assertEquals(localLine, remoteLine)
			}
		}finally{
			remoteBuffIn.close();
			localBuffIn.close();
		}
	}
	
	
	/**
	 * Test that we can read a file from hdfs using a closure
	 */
	@Test
	public void testOpenWithClosure(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteFile = "target/test/HDFSModuleImplTest/hdfs/testOpenWithClosure.txt"
		hdfsModule.put localFile, remoteFile
		
		
		BufferedReader localBuffIn = new BufferedReader(new FileReader(localFile))
		
		try{
			hdfsModule.open remoteFile, { FSDataInputStream input ->
				String localLine = null;
				String remoteLine = null
				BufferedReader remoteBuffIn = new BufferedReader(new InputStreamReader(input))
				
				while( (localLine = localBuffIn.readLine()) != null &&  (remoteLine = remoteBuffIn.readLine()) != null){
					assertEquals(localLine, remoteLine)
				}
			}
		}finally{
			localBuffIn.close();
		}
	}
	
	
	/**
	* Test reading each line of a text file
	*/
   @Test
   public void testEachLineBzip2Compressed(){
	   
	   String localFile = "src/test/resources/testlog.bz2"
	   String localTestFile = "src/test/resources/testlog.txt"
	   String remoteFile = "target/test/HDFSModuleImplTest/hdfs/testEachLine.bz2"
	   hdfsModule.put localFile, remoteFile
	   
	   BufferedReader localBuffIn = new BufferedReader(new FileReader(localTestFile))
	   
	   try{
		   def lines = []
		   
		   hdfsModule.eachLine remoteFile, { line -> println line; lines << line;  }
		
		   //test twice to use pooling
		   lines = []
		   hdfsModule.eachLine remoteFile, { line -> println line; lines << line;  }
		   
		   
		   int i = 0
		   String localLine
		   
		   while( (localLine = localBuffIn.readLine()) != null){
			   assertEquals(localLine, lines[i++])
		   }
	   }finally{
		   localBuffIn.close();
	   }
   }
   
	
	/**
	 * Test reading each line of a text file
	 */
	@Test
	public void testEachLine(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteFile = "target/test/HDFSModuleImplTest/hdfs/testEachLine.txt"
		hdfsModule.put localFile, remoteFile
		
		
		BufferedReader localBuffIn = new BufferedReader(new FileReader(localFile))
		
		try{
			def lines = []
			
			hdfsModule.eachLine remoteFile, { line -> lines << line  }
			
			int i = 0
			String localLine
			
			while( (localLine = localBuffIn.readLine()) != null){
				assertEquals(localLine, lines[i++])
			}
		}finally{
			localBuffIn.close();
		}
	}
	
	
	/**
	 * Test each line of files through a list of directories
	 */
	@Test
	public void testEachLineRecursive(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testEachLineRecursive"
		def remoteDirs = [
			"${remoteDir}/test1",
			"${remoteDir}/test2"
		]
		
		def remoteFiles = []
		int i = 0
		
		remoteDirs.each { dir ->
			remoteFiles << "${dir}/testlist-${i}.txt"
			remoteFiles << "${dir}/testlist2-${i}.txt"
			i++
		}
		
		def remoteFileNames = []
		remoteFiles.each { String name -> remoteFileNames << new File(name).name }
		
		remoteFiles.each { String fileName ->
			hdfsModule.put localFile, fileName
		}
		
		//count local lines
		int localLineCount = 0
		new File(localFile).eachLine { localLineCount++ }
		
		localLineCount *= (remoteDirs.size() * 2)
		
		int remoteLineCount = 0
		hdfsModule.eachLine remoteDir, { String line -> remoteLineCount++ }
		
		assertEquals(localLineCount, remoteLineCount)
	}
	
	/**
	 * Test each line of files through a list of directories
	 */
	@Test
	public void testEachLineRecursiveGlobing(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testEachLineRecursiveGlobing"
		def remoteDirs = [
			"${remoteDir}/test1",
			"${remoteDir}/test2"
		]
		
		String remoteDirGlob = "$remoteDir/test*/*.txt"
		
		def remoteFiles = []
		int i = 0
		
		remoteDirs.each { dir ->
			remoteFiles << "${dir}/testlist-${i}.txt"
			remoteFiles << "${dir}/testlist2-${i}.txt"
			i++
		}
		
		def remoteFileNames = []
		remoteFiles.each { String name -> remoteFileNames << new File(name).name }
		
		remoteFiles.each { String fileName ->
			hdfsModule.put localFile, fileName
		}
		
		//count local lines
		int localLineCount = 0
		new File(localFile).eachLine { localLineCount++ }
		
		localLineCount *= (remoteDirs.size() * 2)
		
		int remoteLineCount = 0
		hdfsModule.eachLine remoteDirGlob, { String line -> remoteLineCount++ }
		
		assertEquals(localLineCount, remoteLineCount)
	}
	
	/**
	 * Test listing files
	 */
	@Test
	public void testListFiles(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testListFilesDir1"
		def remoteFiles = [
			"${remoteDir}/testlist.txt",
			"${remoteDir}/testlist2.txt"
		]
		
		def remoteFileNames = []
		remoteFiles.each { String name -> remoteFileNames << new File(name).name }
		
		remoteFiles.each { String fileName ->
			hdfsModule.put localFile, fileName
		}
		
		
		hdfsModule.list remoteDir, { String file ->
			assertTrue( new File(file).name in remoteFileNames )
		}
	}
	/**
	 * Test list files using globing
	 */
	@Test
	public void testListFilesGlobing(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testListFilesGlobing"
		def remoteFiles = [
			"${remoteDir}/testlist.txt",
			"${remoteDir}/testlist2.txt"
		]
		
		def remoteFileNames = []
		remoteFiles.each { String name -> remoteFileNames << new File(name).name }
		
		remoteFiles.each { String fileName ->
			hdfsModule.put localFile, fileName
		}
		
		
		hdfsModule.list "remoteDir/*.txt", { String file ->
			assertTrue( new File(file).name in remoteFileNames )
		}
	}
	
	/**
	 * Test listing files
	 */
	@Test
	public void testListFilesRecursive(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testListFilesRecursive"
		def remoteDirs = [
			"${remoteDir}/test1/testa/123/",
			"${remoteDir}/test2/testb/123/"
		]
		
		def remoteFiles = []
		int i = 0
		
		remoteDirs.each { dir ->
			remoteFiles << "${dir}/testlist-${i}.txt"
			remoteFiles << "${dir}/testlist2-${i}.txt"
			i++
		}
		
		def remoteFileNames = []
		remoteFiles.each { String name -> remoteFileNames << new File(name).name }
		
		remoteFiles.each { String fileName ->
			hdfsModule.put localFile, fileName
		}
		
		
		hdfsModule.list remoteDir, true, { String file ->
			//		 println "file == $file"
			assertTrue( new File(file).name in remoteFileNames )
		}
	}
	
	/**
	 * Test listing files with lastUpdated parameter
	 */
	@Test
	public void testListFilesLastUpdated(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testListFilesLastUpdated"
		
		String remoteFile1 = "$remoteDir/test1.txt"
		String remoteFile2 = "$remoteDir/test2.txt"
		
		//add first file and wait a second for its modification time
		hdfsModule.put localFile, remoteFile1
		Thread.sleep(1000)
		
		
		//add second file and capture its modification time
		long modificationTime = System.currentTimeMillis()
		hdfsModule.put localFile, remoteFile2
		
		hdfsModule.list remoteDir, modificationTime, { String fileName ->
			
			//check that this is only remoteFile2
			assertEquals("test2.txt", new File(fileName).name)
			
		}
	}
	
	
	/**
	 * Test that we can create a file with an output stream
	 */
	@Test
	public void testCreate(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testCreate"
		String remoteFile = "$remoteDir/test1.txt"
		File file = new File(localFile)
		
		//write the local file to remoteFile using an output stream
		FSDataOutputStream out = hdfsModule.create(remoteFile)
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))
		try{
			
			
			file.eachLine { String line ->
				
				writer.println line
			}
		}finally{
			writer.close()
			out.close()
		}
		
		//check that the remote and local files are identical
		
		///1 get all lines from the remote file
		def remoteLines = []
		hdfsModule.eachLine remoteFile, { remoteLines << it }
		
		int i = 0;
		file.eachLine { String line ->
			assertEquals(line, remoteLines[i++])
		}
	}
	
	/**
	 * Test that we can create a file with an output stream passed to a closure
	 */
	@Test
	public void testCreateWithClosure(){
		
		String localFile = "src/test/resources/testlog.txt"
		String remoteDir = "target/test/HDFSModuleImplTest/hdfs/testCreateWithClosure"
		String remoteFile = "$remoteDir/test1.txt"
		File file = new File(localFile)
		
		//write the local file to remoteFile using an output stream
		
		hdfsModule.create remoteFile, { FSDataOutputStream fout -> 
			
			file.eachLine { String line ->
				fout << "$line\n"
			}
		}
		//check that the remote and local files are identical
		
		///1 get all lines from the remote file
		def remoteLines = []
		hdfsModule.eachLine remoteFile, { remoteLines << it }
		
		int i = 0;
		file.eachLine { String line ->
			assertEquals(line, remoteLines[i++])
		}
	}
	
	
	@BeforeClass
	public static void init(){
		
		File file = new File('target/test/HDFSModuleImplTest/hdfs/')
		if(file.exists()){
			file.delete()
		}
		file.mkdirs()
		
		Configuration conf = new Configuration()
		
		dfsCluster = new MiniDFSCluster(conf, 1, true, (String[])null)
		dfsCluster.waitActive()
		
		hdfsModule = getHDFSModule()
	}	
	
	@AfterClass
	public static void stop(){
		dfsCluster.shutdown()
		new File('target/test/HDFSModuleImplTest/hdfs/').delete()
	}
	
	static HDFSModule getHDFSModule(){
		
		
		
		String moduleClass = HDFSModuleImpl.class.name
		
		
		def configStr = """
		
			     clusters{
			        local{
			           isDefault=true
			           hdfsProperties='src/test/resources/localhdfsconfig.properties'
			        }
			     }
		
		
		"""
		
		ConfigObject hdfsConfig = new ConfigSlurper().parse(configStr)
		
		HDFSModule hdfsModule = new HDFSModuleImpl()
		hdfsModule.init(hdfsConfig)
		
		return hdfsModule
	}
}
