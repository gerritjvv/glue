package org.glue.rest;

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException


public class ClassPathReader {

	
	
	public static void main(String[] args) {

		ClientConfig config = ClientConfig.getInstance()


		//----------- Parse command line options -------------------//
		CommandLineParser parser = new GnuParser()
		Options options = buildOptions()

		CommandLine line
		try{
			line = parser.parse(options, args)
		}catch(ParseException excp){
			Client.error = excp

			println excp.toString()
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("classpathreader", options)

			return
		}

		//---------- Process commands -----------------------------//
		if(line.hasOption("classpath")){
		
			print(getConfigProp("processClassPath").join(":"))
			
			
		}else if(line.hasOption('javaopts')){
			
			print(getConfigProp("processJavaOpts").join(" "))
		
		}else{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("classpathreader", options)
		}


		//to stop
	}

	def static getConfigProp(String name){
		
		//find and parse configuration
		URL clientConfigUrl = Thread.currentThread().getContextClassLoader().getResource('exec.groovy')

		//check path conf/
		if(!clientConfigUrl) clientConfigUrl = Thread.currentThread().getContextClassLoader().getResource('conf/exec.groovy')

		if(!clientConfigUrl){
			throw new RuntimeException("Cannot find configuration file exec.groovy")
		}

		ConfigObject config
		try{
			config = new ConfigSlurper().parse(clientConfigUrl)
		}catch(GroovyRuntimeException exp){
			throw new RuntimeException("Error parsing exec.groovy", exp)
		}
		
		return config."$name"
	}


	/**
	 * Builds the CLI options for the Client
	 * @return Options
	 */
	private static final Options buildOptions(){

		Option help = new Option('h', 'help', false, 'Type this to get a list of available commands')


		Option classpath = OptionBuilder.withArgName('classpath')
				
				.withDescription("Returns the exec.groovy configured processClassPath value")
				.create("classpath")
				
				
	    Option javaopts = OptionBuilder.withArgName('javaopts')
						
						.withDescription("Returns the exec.groovy configured javaProcessOpts")
						.create("javaopts")
						
		Options options = new Options()
		options.addOption(help)
		options.addOption(classpath)
		options.addOption(javaopts)

		return options
	}
}
