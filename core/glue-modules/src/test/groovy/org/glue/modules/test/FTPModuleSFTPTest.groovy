package org.glue.modules.test;

import static org.junit.Assert.*

import org.apache.sshd.SshServer
import org.apache.sshd.server.Command
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.auth.UserAuthNone
import org.apache.sshd.server.command.ScpCommandFactory
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory
import org.glue.modules.FTPModule
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

/**
 * Tests the FTPModule with SFTP
 * 
 *
 */
class FTPModuleSFTPTest extends GroovyTestCase{
	private static boolean setupDone = false;
	
	private static String HOME_DIR = "/";
	private static String USER = "test";
	private static String PWD = "test";
	private static SshServer sshd
	private static int port = 3000;
	private static String URL;
	
	private static final String FILE = "/dir/sample.txt";
	private static final String CONTENTS = "abcdef 1234567890";

	@Test
	public void testExists(){
		
		if(!setupDone)
			setup()
		
		FTPModule module = getFTPModule();
		
		File file = File.createTempFile("test", "txt");
		file.deleteOnExit()
		file << "Hi"
		module.put(file.absolutePath, "mytest.txt") ;
		
		
		assertTrue(module.exists("mytest.txt"))
		
	}
	
	
	@Test
	public void testModuleAddFile(){
		
		if(!setupDone)
			setup()
		
		FTPModule module = getFTPModule();
		
		File file = File.createTempFile("test", "txt");
		file.deleteOnExit()
		file << "Hi"
		module.put(file.absolutePath, "mytest.txt") ;
		String[] files = module.ls('')
		
		files.each { println "Files from ls $it" }
		assertNotNull(files.find({it.endsWith('mytest.txt') }) )
	}
	
	@Test
	public void testGetFile(){
		if(!setupDone)
		setUp()
	
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
			  host="sftp://localhost:3000"
			  user="$USER"
			  pwd="$PWD"
			}
		 }
		
		"""))
		
		
		return module
	}
	
	
	/**
	 * See http://mina.apache.org/sshd/embedding-sshd-in-5-minutes.html
	 */
	@BeforeClass
	public static void setup(){
		sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		sshd.setCommandFactory(new ScpCommandFactory(new CommandFactory() {
			public Command createCommand(String command) {
				return new ProcessShellFactory(command.split(" ")).create();
			}
		}));
		sshd.setUserAuthFactories([new UserAuthNone.Factory()])
		sshd.start()
		port = port
		URL = "localhost"
		
		sshd.setSubsystemFactories([new SftpSubsystem.Factory()])
		
		println "Started sftp server"
		setupDone = true
		
		
	}
	
	@AfterClass
	public static void shutdown(){
		if(sshd){
			sshd.stop()
		}
	}
	
	
	
	
}
