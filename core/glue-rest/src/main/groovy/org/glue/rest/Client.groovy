package org.glue.rest;

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.map.SerializationConfig
import org.glue.rest.job.JobSubmit
import org.glue.rest.job.JobSubmitResponse
import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.log.impl.DefaultGlueExecLoggerProvider
import org.restlet.Response
import org.restlet.resource.ClientResource


public class Client {

	/*
	 * Used for testing
	 */

	static Throwable error
	static String unitId
	static Response response
	static GlueExecLoggerProvider logProvider

	/**
	 * Mainly used for testing.
	 * If this property is defined the ClientConfig.serverUrl is ignored
	 */
	static URL serverUrl = null

	public static void main(String[] args) {

		ClientConfig config = ClientConfig.getInstance()

		//@TODO provide remote log read service
		//At the moment for the logProvider to work the client must be local
		//The log path is hardcoded
		logProvider = new DefaultGlueExecLoggerProvider(new File('/opt/glue/log'))

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
			formatter.printHelp("glue-client", options)

			return
		}

		//---------- Process commands -----------------------------//
		if(line.hasOption("repl")){
			def lang = line.getOptionValue("repl")
			def workflowName = line.getOptionValue("name")
			if(!workflowName) workflowName = "repl"
			
			org.glue.unit.exec.impl.ReplRunner.main(
				["-lang", lang, "-moduleConf", "/opt/glue/conf/workflow_modules.groovy",
					"-execConf", "/opt/glue/conf/exec.groovy",
					"-workflow", workflowName,
					"-uuid", UUID.randomUUID()] as String[]
				)
			
			

		}else if(line.hasOption('submit')){

			String unitName = line.getOptionValue('submit')

			//calculate options
			Map<String, String> params = [:]

			if(line.hasOption('D')){

				String[] optionValues = line.getOptionValues('D')
				int len = optionValues.length

				for(int i = 0; i < len; i++){

					def key = optionValues[0]
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

			submit config, unitName, params

			if(line.hasOption('t')){

				if(Client.response.status?.isSuccess() && Client.unitId){
					println "Press ctrl-c to stop tailing log"

					def file = logProvider.get(Client.unitId).getLogFile();
					if(file){
					
						println "Log file: $file"
						while(!file.exists()){
							println "Waiting for file"
							Thread.sleep(1000)
						}
						
						println "Tailing file $file"
						BufferedReader reader = new BufferedReader(new FileReader(file) )
						while(true){
							def str = reader.readLine()
							if(!str){
								Thread.sleep(500)
							}else{
								println str
							}

						}
					}

				}

			}

		}else if(line.hasOption('kill')){
		
		  String unitId = line.getOptionValue("kill")?.trim()
		  
		  if(unitId && unitId.length() > 0){
			cmd(config, ['kill', unitId] as String[])  
		  }
		
		}else if(line.hasOption('running')){
		  
			cmd(config, ['running'] as String[])  
		
		}else if(line.hasOption('queued')){
				
				  cmd(config, ['queued'] as String[])

		}else if(line.hasOption('status')){
			String[] statusArgs = line.getOptionValues('status')

			if(!statusArgs || statusArgs.size() == 0){
				cmd( config, ['status']as String[])
			}else if(statusArgs.size() == 1){
				String unitId = statusArgs[0]
				cmd( config, ['status', unitId]as String[])
			}else{
				String unitId = statusArgs[0]
				String processName = statusArgs[1]
				cmd( config, [
					'status',
					unitId,
					processName]
				as String[])
			}
		}else if(line.hasOption('modules')){

			cmd( config, ['modules']as String[])
		}else if(line.hasOption('stop')){
			cmd( config, ['stop']as String[])
		}else{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("glue-client", options)
		}


		//to stop
	}

	/**
	 * Send the SUBMIT client request
	 * @param config
	 * @param unitName
	 * @param params
	 * @return
	 */
	public static String submit(ClientConfig config, String unitName, Map<String, String> params = null) {

		// do some client stuff
		ClientResource resource = new ClientResource(
				(Client.serverUrl)? "${Client.serverUrl}/submit" :	"${config.serverUrl}/submit" )

		try{
			//if th reslet json extension is on the class path this object is serialized using json
			JobSubmitResponse resp	= resource.post(new JobSubmit(unitName:unitName, params:params), JobSubmitResponse);

			if(resp == null){
				Client.error = new RuntimeException("Uknown exception")
			}

			String unitId = resp.unitId
			Client.unitId = unitId
			Client.response = resource.getResponse()

			println unitId
		}catch(ResourceException resourceException){
			Client.error = resourceException

			println """
			 Error while submitting work flow $unitName
			 status: ${resourceException.status}
			 msg: ${resourceException.toString()}
			"""

		}
	}

	/**
	 * Helper method to run get commands e.g. status 
	 * @param config
	 * @param args
	 * @return String result
	 */
	public static String cmd(ClientConfig config,String[] args) {
		String path=args.join('/');

		try{
			ClientResource resource =new ClientResource(
					(Client.serverUrl)? "${Client.serverUrl}/$path" :	"${config.serverUrl}/$path");

			Map responseMap = resource.get(HashMap.class)

			Client.response = resource.response

			//read content as json and print out with proper indent
			ObjectMapper mapper = new ObjectMapper()
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			try{

				mapper.writeValue(System.out, responseMap)
			}catch(Throwable t){
				t.printStackTrace()
				println responseMap
			}
		}catch(ResourceException resourceException){
			Client.error = resourceException

			println """
			
			 Error submitting ${config.serverUrl}/$path
			 status: ${resourceException.status}
			 msg: ${resourceException.toString()}
			
			"""
		}
	}


	/**
	 * Builds the CLI options for the Client
	 * @return Options
	 */
	private static final Options buildOptions(){

		Option help = new Option('h', 'help', false, 'Type this to get a list of available commands')

		Option tail = new Option('t', 'tail', false, 'Works only with the submit command and will on successful submit tail the log output')

		Option submit = OptionBuilder.withArgName('workflow_name')
				.hasArg()
				.withDescription("Submits the work flow for execution")
				.create("submit")
				
				
	    Option repl = OptionBuilder.withArgName('repl')
						.hasArg()
						.withDescription("Runs a repl for groovy/jython/clojure")
						.create("repl")
						
		Option name = OptionBuilder.withArgName('name')
										.hasArg()
										.withDescription("Use with repl to specify the workflow name")
										.create("name")
										
						
		Option kill = OptionBuilder.withArgName('unitid')
						.hasArg()
						.withDescription("Kills a workflow")
						.create("kill")
		

		Option status = OptionBuilder.withArgName('[unit_id] [process_name]')
				.hasOptionalArgs(2)
				.withValueSeparator()
				.withDescription("Work flow execution status")
				.create("status")


		Option property  = OptionBuilder.withArgName( "property=value" )
				.hasArgs(2)
				.withValueSeparator()
				.withDescription( "use value for given property" )
				.create( "D" );

				
		Option running = new Option('running', false, 'Lists the running workflows')
		
		Option queued = new Option('queued', false, 'Lists the queued workflows')
				
		Option modules = new Option('modules', false, 'Lists the available modules')
		Option stop = new Option('stop', false, 'Asks the glue server to shutdown')



		Options options = new Options()
		options.addOption(help)
		options.addOption(submit)
		options.addOption(kill)
		options.addOption(tail)
		options.addOption(repl)
		options.addOption(name)
		options.addOption(status)
		options.addOption(running)
		options.addOption(queued)
		options.addOption(modules)
		options.addOption(stop)
		options.addOption(property)

		return options
	}
}
