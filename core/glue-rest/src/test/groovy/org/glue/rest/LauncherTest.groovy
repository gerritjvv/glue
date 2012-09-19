package org.glue.rest;

import static org.junit.Assert.*;
import org.glue.unit.exec.GlueState;

import org.junit.Test;
import org.restlet.resource.ClientResource;
import org.restlet.Component;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.glue.unit.exec.GlueExecutor;
import org.glue.unit.exec.impl.GlueExecutorImpl;
import org.restlet.data.Protocol;
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.impl.GlueModuleFactoryImpl

class LauncherTest {

	@Test
	public void testLauncherShutdown() {


		String execConfigPath='src/test/resources/conf/exec.groovy';
		String moduleConfigPath="src/test/resources/conf/modules.groovy";
		int port=8225;
		String host = "localhost:$port";

	    def out = System.out
		def err = System.err
		
		Thread.start {
			Launcher.main([
				execConfigPath,
				moduleConfigPath,
				port]
			as String[])
		}

		Thread.sleep(5000L)
		println "Sleeping thread for 4 secs, then shutting it down"
		Thread.sleep 4000;
		println "Now asking for the status"
		Client.main([host, 'status']as String[])
		Thread.sleep 100;
		println "Now submitting a job"
		Client.main([host, 'submit', 'test1']as String[])
		Client.main([host, 'status']as String[])
		Thread.sleep 1000;
		println "Now Shutting it down"
		Client.main([host, 'status']as String[])
		println "Now shutting it down";
		Client.main([host, 'stop']as String[])

        System.out = out
		System.err = err

		println "HAPPY END!"
	}
}
