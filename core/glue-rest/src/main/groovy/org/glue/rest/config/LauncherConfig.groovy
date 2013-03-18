package org.glue.rest.config

import org.apache.log4j.Logger
import org.glue.rest.exception.ConfigurationException
import org.glue.unit.exceptions.ModuleConfigurationException

/**
 *
 * When the Launcher is called its configuration parameters are read,<br/>
 * and the application configuration is loaded.<br/>
 * 
 * This object contains that configuration, and will be added dynamically to the DI framework.
 *
 */
class LauncherConfig {

	private static final Logger LOG = Logger.getLogger(LauncherConfig)

	int restServerPort
	ConfigObject moduleFactoryConfig
	ConfigObject execConfig

	ConfigObject statusManagerConfig

	int executorPoolThreads
	int executorMaxProcesses
	
	int maxProcesses
	
	String processExecConfig
	String processModuleConfig
	
	String processLogDir
	
	def processJavaOpts = []
	def processClassPath = []
	
	
	static LauncherConfig getInstance(String[] args){

		String 	execConfigPath=args[0];
		String 	moduleConfigPath=args[1];

		def moduleConfigFile = new File(moduleConfigPath)
		def execConfigFile = new File(execConfigPath)


		if(!(moduleConfigFile.exists() && moduleConfigFile.canRead() ) ){
			throw new ConfigurationException("The modules configuration file cannot be read $moduleConfigPath")
		}

		if(!(execConfigFile.exists() && execConfigFile.canRead() ) ){
			throw new ConfigurationException("The exec configuration file cannot be read $execConfigPath")
		}


		ConfigObject moduleFactoryConfig

		try{
			moduleFactoryConfig  = new ConfigSlurper().parse(moduleConfigFile.toURI().toURL());
		}catch(GroovyRuntimeException exc){
			throw new ModuleConfigurationException("Error in module configuration $moduleConfigPath", null, exc)
		}

		ConfigObject execConfig

		try{
			execConfig = new ConfigSlurper().parse(execConfigFile.toURI().toURL());
		}catch(GroovyRuntimeException exc){
			throw new ConfigurationException("Error in exec configuration $execConfigPath", exc)
		}

		
		
		if(!execConfig.serverPort){
			throw new ConfigurationException("The property serverPort must be defined in $execConfigPath")
		}

		int serverPort
		try{
			serverPort = Integer.valueOf(execConfig.serverPort)
		}catch(Throwable t){
			throw new ConfigurationException("The property serverPort must be a valid port number in $execConfigPath", t)
		}

		if(!execConfig.lookupPath){
			throw new ConfigurationException("The property lookupPath must be defined in $execConfigPath")
		}

		def executorPoolThreads

		//any threads that are used by the glue server
		if(execConfig.executorPoolThreads){
			executorPoolThreads = Integer.valueOf(execConfig.executorPoolThreads)
		}else{
			executorPoolThreads = Runtime.getRuntime().availableProcessors() + 1
		}
		
		def processModuleConfig 
		if(execConfig.processModuleConfig){
			processModuleConfig = execConfig.processModuleConfig
		}else{
			throw new ConfigurationException("The property processModuleConfig must be defined in $execConfigPath")
		}
		
		def processExecConfig
		if(execConfig.processExecConfig){
			processExecConfig = execConfig.processExecConfig
		}else{
			processExecConfig = execConfigPath
		}
		
		LOG.info "Using processModuleConfig: $processModuleConfig"
		LOG.info "Using processExecConfig: $processExecConfig"
		
		//--- Max processes that can run at any time
		def executorMaxProcesses
		
		if(execConfig.executorMaxProcesses){
			executorMaxProcesses = Integer.valueOf(execConfig.executorMaxProcesses)
		}else{
			executorMaxProcesses = Runtime.getRuntime().availableProcessors() + 1
		}

		//java options for each workflow process
		def processJavaOpts
	    if(execConfig.processJavaOpts){
			processJavaOpts = execConfig.processJavaOpts
		}else{
			processJavaOpts = []
		}
		
		//class path for each workflow process
		def processClassPath
		if(execConfig.processClassPath){
			processClassPath = execConfig.processClassPath
		}else{
			processClassPath = []
		}
		
		//load the status manager
		if(!(execConfig.unitStatusManager && execConfig.unitStatusManager.className
		&& execConfig.unitStatusManager.config)){
			throw new ConfigurationException("The property unitStatusManager must be defined in $execConfigPath")
		}

		//process log dir
		if(!(execConfig.processLogDir && new File(execConfig.processLogDir).exists())){
			throw new ConfigurationException("The property processLogDir must be defined in $execConfigPath and exist")
		}
		
		
		ConfigObject statusManagerConfig = execConfig.unitStatusManager

		return new LauncherConfig(restServerPort:serverPort,
		moduleFactoryConfig:moduleFactoryConfig,
		execConfig:execConfig,
		executorPoolThreads:executorPoolThreads,
		statusManagerConfig:statusManagerConfig,
		processExecConfig:processExecConfig,
		processModuleConfig:processModuleConfig,
		executorMaxProcesses:executorMaxProcesses,
		processJavaOpts:processJavaOpts,
		processClassPath:processClassPath,
		processLogDir:execConfig.processLogDir
		)
	}
}
