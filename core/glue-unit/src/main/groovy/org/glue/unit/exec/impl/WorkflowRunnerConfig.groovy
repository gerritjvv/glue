package org.glue.unit.exec.impl

import java.io.File;

import groovy.util.ConfigObject

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.glue.unit.exceptions.UnitSubmissionException
import org.glue.unit.script.ScriptClassCache

/**
 * 
 * Encapsulates the WorkflowRunner configuration
 *
 */
 @Typed(TypePolicy.MIXED)
class WorkflowRunnerConfig {

	ConfigObject moduleFactoryConfig
	ConfigObject execConfig

	ConfigObject statusManagerConfig

	String uuid
	String workflow

	Map<String, String> params

	int executorPoolThreads

	/**
	 * Parses the command line options into a WorkflowRunnerConfig instance
	 * @param args
	 * @return WorkflowRunnerConfig
	 */
	static WorkflowRunnerConfig getInstance(String[] args) {

		//load configuration
		//load di class

		//----------- Parse command line options -------------------//
		CommandLineParser parser = new GnuParser()
		Options options = buildOptions()

		CommandLine line
		try{
			line = parser.parse(options, args)
		}catch(ParseException excp){
			println excp.toString()
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("WorkflowRunner", options)
			throw excp
		}


		/**
		 * Get the argument values that we need to run a single workflow
		 * moduleConf: the module configuration groovy file, this file is compiled and passed to a module factor provider.
		 * workflow: this is the workflow name to be executed
		 * uuid: the workflow should have a uniquely assigned id
		 */

		File moduleConfFile = getFile(line, 'moduleConf')
		def moduleConfObj = ScriptClassCache.getDefaultInstance().parse(moduleConfFile)
		def execConfFile = getFile(line, 'execConf')
		def execConfObj = ScriptClassCache.getDefaultInstance().parse(execConfFile)


		def workflowName = line.getOptionValue('workflow')
		def uuid = line.getOptionValue('uuid')


		//load the status manager
		if(!(execConfObj.unitStatusManager && execConfObj.unitStatusManager.className)){
			throw new UnitSubmissionException("The property unitStatusManager must be defined in $execConfFile")
		}

		ConfigObject statusManagerConfig = execConfObj.unitStatusManager

		int executorPoolThreads

		if(execConfObj.executorPoolThreads){
			executorPoolThreads = Integer.valueOf(execConfObj.executorPoolThreads)
		}else{
			executorPoolThreads = Runtime.getRuntime().availableProcessors() + 1
		}

		//calculate options
		Map<String, String> params = [:]

		if(line.hasOption('D')){

			String[] optionValues = line.getOptionValues('D')
			int len = optionValues.length

			for(int i = 0; i < len; i++){

				def key = optionValues[i]
				def value
				def valueIndex = i+1

				if(valueIndex < len){
					value = optionValues[valueIndex]
					i++
				}else{
					value = ''
				}

				params[key] = value
			}
		}

		return new WorkflowRunnerConfig(
		moduleFactoryConfig:moduleConfObj,
		execConfig:execConfObj,
		statusManagerConfig:statusManagerConfig,
		uuid:uuid,
		workflow:workflowName,
		executorPoolThreads:executorPoolThreads,
		params:params
		)
	}

	/**
	 * Gets the option value, ensures that the file exists and is a file.
	 * @param line
	 * @param optionName
	 * @return File
	 */
	private static final File getFile(CommandLine line, optionName){
		String moduleConf = line.getOptionValue(optionName)

		File file = new File(moduleConf)

		if(!(file.exists() || file.isFile())){
			throw new UnitSubmissionException("The configuration file $moduleConf does not exist")
		}

		return file
	}

	/**
	 * Builds the CLI options for the WorkflowRunner
	 * @return Options
	 */
	private static final Options buildOptions(){

		Option help = new Option('help', false, 'Type this to get a list of available commands')

		Option execConf = OptionBuilder.withArgName('exec configuration groovy file')
				.hasArg()
				.isRequired()
				.withDescription("execution configuration file")
				.create("execConf")


		Option moduleConf = OptionBuilder.withArgName('module configuration groovy file')
				.hasArg()
				.isRequired()
				.withDescription("Configuration for the modules that will run with this workflow")
				.create("moduleConf")

		Option workflowName = OptionBuilder.withArgName('workflow name')
				.hasArg()
				.isRequired()
				.withDescription("name of workflow to execute")
				.create("workflow")

		Option uuid = OptionBuilder.withArgName('uuid')
				.hasArg()
				.isRequired()
				.withDescription("The execution id assigned to this workflow")
				.create("uuid")

		Option property  = OptionBuilder.withArgName( "property=value" )
				.hasArgs(2)
				.withValueSeparator()
				.withDescription( "pass parameters to the workflow" )
				.create( "D" );

		Options options = new Options()
		options.addOption(help)
		options.addOption(moduleConf)
		options.addOption(execConf)
		options.addOption(workflowName)
		options.addOption(uuid)
		options.addOption(property)

		return options
	}
}
