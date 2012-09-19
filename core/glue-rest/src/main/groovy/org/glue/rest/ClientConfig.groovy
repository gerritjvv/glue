package org.glue.rest

import org.glue.rest.exception.ConfigurationException

/**
 * 
 * This class contains the client configuration
 *
 */
class ClientConfig {

	/**
	 * Contains the configuration from the $GLUE_HOME/conf/client.groovy file
	 */
	ConfigObject config

	/**
	 * Points to the glue server
	 */
	URL serverUrl

	static final ClientConfig getInstance(){

		//find and parse configuration
		URL clientConfigUrl = Thread.currentThread().getContextClassLoader().getResource('client.groovy')

		//check path conf/
		if(!clientConfigUrl) clientConfigUrl = Thread.currentThread().getContextClassLoader().getResource('conf/client.groovy')

		if(!clientConfigUrl){
			throw new ConfigurationException("Cannot find configuration file client.groovy")
		}

		ConfigObject config
		try{
			config = new ConfigSlurper().parse(clientConfigUrl)
		}catch(GroovyRuntimeException exp){
			throw new ConfigurationException("Error parsing client.groovy", exp)
		}

		//check for server property

		String serverUrl = config?.server
		if(!serverUrl){
			throw new ConfigurationException("The property server must be defined in the file client.groovy")
		}
		
		if(!(serverUrl.startsWith('http://') && serverUrl.startsWith('https://'))){
			serverUrl = "http://$serverUrl"
		}

		//return ClientConfig instance
		return new ClientConfig(
		config:config,
		serverUrl:new URL(serverUrl)
		)

	}
}
