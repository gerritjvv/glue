package org.glue.rest;

import org.glue.rest.config.LauncherConfig
import org.glue.rest.di.DIBootstrap
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.om.GlueModuleFactoryProvider
import org.apache.log4j.Logger
import org.restlet.Component



public class Launcher {

	static boolean testMode = false
	
	private static Logger LOG = Logger.getLogger(Launcher)

	public static void main(String[] args) {

		try{
			//Parse command line arguments
			LauncherConfig config = LauncherConfig.getInstance(args)
			//start DI Framework
			DIBootstrap di = new DIBootstrap(config)
			GlueExecutor exec = di.getBean(GlueExecutor.class)
			Component component = di.getBean(Component.class)
			try{
				//we should start all modules here.
				GlueModuleFactoryProvider moduleFactoryProvider = di.getBean("moduleFactoryProvider")
				moduleFactoryProvider.get()?.availableModules?.each{ println "Module: ${it}" }
				component.start()
				println "Rest Client Started: ${component.isStarted()}"
				exec.waitUntillShutdown();
			}
			finally{
				di.shutdown()
				component.stop();
			}
		}catch(Throwable t){
			LOG.error(t.toString(), t)
			throw t
		}

		if(!testMode){
        	System.exit(0)
		}
	}
}
