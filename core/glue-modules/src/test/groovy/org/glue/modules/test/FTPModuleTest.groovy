package org.glue.modules.test;

import static org.junit.Assert.*

import org.glue.modules.FTPModule
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.mockftpserver.fake.FakeFtpServer
import org.mockftpserver.fake.UserAccount
import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem


class FTPModuleTest{
	
	private static String HOME_DIR = "/";
	private static String USER = "test";
	private static String PWD = "test";
	private static FakeFtpServer fakeFtpServer;
	private static int port = 0;
	private static String URL;
	
	private static final String FILE = "/dir/sample.txt";
	private static final String CONTENTS = "abcdef 1234567890";

	@Test
	public void testModuleAddFile(){
		
		FTPModule module = getFTPModule();
		
		File file = File.createTempFile("test", "txt");
		file.deleteOnExit()
		file << "Hi"
		
		assertTrue( module.put(file.absolutePath, "mytest.txt") );
		
		String[] files = module.ls()
		println files
		assertEquals("/mytest.txt", files.find({it == '/mytest.txt'}) )
		
	}
	
	@Test
	public void testGetFile(){
		
		FTPModule module = getFTPModule();
		
		File file = File.createTempFile("testGetFile", "txt");
		file.deleteOnExit()
		file << "Hi"
		
		assertTrue( module.put(file.absolutePath, "testGetFile.txt") );
		//get file
	
		File dir = new File("target/test/fptmoduletest/testGetFile");
	 	dir.mkdirs();
		File copyFile = new File(dir, "testcopyfile"); 
		
		assertTrue(module.get( "testGetFile.txt", copyFile.absolutePath ))
		
		assertEquals(file.text, copyFile.text)
		
	}
	
	
	private FTPModule getFTPModule(){
		FTPModule module = new FTPModule();

		module.init(
			new ConfigSlurper().parse("""
		
		 servers{
			test1{
			  host="$URL"
			  user="$USER"
			  pwd="$PWD"
			  
			}
		 }
		
		"""))
		return module
	}
	
	
	@BeforeClass
	public static void setup(){
		fakeFtpServer = new FakeFtpServer();
		fakeFtpServer.setServerControlPort(0);  // use any free port

		UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new FileEntry(FILE, CONTENTS));
		
		fakeFtpServer.setFileSystem(fileSystem);
		
		UserAccount userAccount = new UserAccount(USER, PWD, HOME_DIR);
		fakeFtpServer.addUserAccount(userAccount);

		fakeFtpServer.start();
		port = fakeFtpServer.getServerControlPort();
     
		URL = "ftp://localhost:$port"
	}
	
	@AfterClass
	public static void shutdown(){
		if(fakeFtpServer){
			fakeFtpServer.stop();
		}
	}
	
	
}
