package org.glue.modules.hadoop;

import static groovyx.gpars.dataflow.DataFlow.start

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

import org.apache.log4j.Logger
import org.apache.pig.PigServer
import org.glue.modules.hadoop.pigutil.PigServerUtil
import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.glue.unit.process.DefaultJavaProcessProvider
import org.glue.unit.process.JavaProcess

/**
 * Runs pig jobs.<br/>
 * This module is meant to be run as a Prototype and not singleton.
 */
public class PigModule implements GlueModule {
	
	private static final Logger LOG = Logger.getLogger(PigModule)
	
	Map<String, ConfigObject> pigConfigurations=[:];
	String defaultConfiguration = null;
	Boolean isRunning=false;
	Map<String,String> constants=new HashMap<String,String>();

	Set<JavaProcess> runningProcesses = new CopyOnWriteArrayList<JavaProcess>()
	
	Collection<String> jarFilesToRegister = []

	DefaultJavaProcessProvider javaProcessProvider

	int maxProcesses = 2;
	
	private Semaphore availableProcesses
	
	void destroy(){
		runningProcesses.each { JavaProcess process ->  
			try{
				if(process.isRunning()){
					process?.kill()
				}
			}catch(t){
			  ;//ignore
			}
		}
	}

	@Override
	public Boolean canProcessRun(GlueProcess process, GlueContext context) {
		return true;
	}

	@Override
	public void configure(String unitId, ConfigObject config) {
		
	}

	@Override
	public String getName() {
		return "pig";
	}

	@Override
	public void init(ConfigObject config) {
		if(!config.clusters) {
			new ModuleConfigurationException("Can't find any clusters in config!")
		}
		config.clusters.each { String key, ConfigObject c ->
			print "loading cluster $key"
			if(c.isDefault) {
				defaultConfiguration=key;
			}

			if(!new File(c.pigProperties.toString()).exists()){
				throw new ModuleConfigurationException("The file ${c?.pigProperties} was not found")
			}

			pigConfigurations[key]=c.pigProperties
			println "Loaded $key as ${c.pigProperties}"
		}

		
		config?.constants.each { const_name, const_val ->
				this.constants.put(const_name.toString(), const_val?.toString());
		}
		

		
		
		//set working directory
		if(config.workingDirectory){
			File file = new File(config.workingDirectory.toString())
			if(!file.exists()){
				throw new ModuleConfigurationException("The working directory ${file} does not exist", config)
			}
			
			javaProcessProvider = new DefaultJavaProcessProvider(workingDirectory:file)
		}else{
			javaProcessProvider = new DefaultJavaProcessProvider()
		}
		
		if(config.maxProcesses){
			maxProcesses = Integer.valueOf(config.maxProcesses.toString())	
		}else{
			maxProcesses = Runtime.getRuntime().availableProcessors() + 1
		}
		
		availableProcesses = new Semaphore(maxProcesses)
		
		//add java classpath
		if(config.classpath){
			config.classpath.each {
				File file = new File(it.toString())
				if(!file.exists()){
					throw new ModuleConfigurationException("The classpath item ${file} does not exist", config)
				}
				
				javaProcessProvider.classpath << it.toString()
			}
			
		}else{
			throw new ModuleConfigurationException("PigModule must have a classpath configured", config)
		}
		
		//add java options if present
		if(config.javaOpts){
			config.javaOpts.each {
				javaProcessProvider.javaOpts << it
			}
		}
		
		//we set the main class to PigRunner
		javaProcessProvider.mainClass = org.glue.modules.hadoop.pig.PigRunner.class.name
		
		checkForJarFiles(config)
	}


	/**
	 * If the jars = ['jar1.jar', ..] exists in the config these jars are added to the jarFilesToRegister collection.
	 * @param config
	 */
	private void checkForJarFiles(ConfigObject config){
		config?.jars?.each{ String fileName ->

			if(!new File(fileName).exists()){
				throw new ModuleConfigurationException("The file $fileName does not exist")
			}

			jarFilesToRegister << fileName
		}
	}

	/**
	 * For each item in jarFilesToRegister a line is added REGISTER jarname
	 * @return String
	 */
	private String createJarRegisterStatements(){
		StringBuilder buff = new StringBuilder(100);
		jarFilesToRegister.each { buff.append("REGISTER $it;\n") }

		return buff.toString()
	}

	@Override
	public void onProcessKill(GlueProcess process, GlueContext context){
	}

	@Override
	public void onProcessFail(GlueProcess process, GlueContext context,
	Throwable t) {
	}

	@Override
	public void onProcessFinish(GlueProcess process, GlueContext context) {
	}

	@Override
	public void onProcessStart(GlueProcess process, GlueContext context) {
	}

	@Override
	public void onUnitFail(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
	}

	public boolean run(GlueContext context, String pigFileOrScript, boolean localMode = false){
		run(context, null, pigFileOrScript, [:], localMode)
	}

	
	public boolean run(GlueContext context, String pigFileOrScript, Map<String, String> params, boolean localMode = false){
		run(context, null, pigFileOrScript, params, localMode)
	}

	public boolean run(GlueContext context, String jobName , String pigFileOrScript,boolean localMode = false){
		run(context, null, jobName, pigFileOrScript, [:], localMode)
	}
	
	public boolean run(GlueContext context, String jobName , String pigFileOrScript, Map<String, String> params, boolean localMode = false){
		run(context, null, jobName, pigFileOrScript, params, localMode)
	}

	/**
	 * 
	 * This is used for running pig queries.<br/>
	 * This method blocks until the pig query returns.
	 * @param glueContext Context
	 * @param pigConfig
	 * @param jobName
	 * @param scriptName
	 * @param params
	 * @return boolean true if success
	 */
	public boolean run(GlueContext context, String clusterName, String jobName , String pigFileOrScript, Map<String, String> params, boolean localMode = false){
		
		
		
		File file=null;
		boolean isTempFile = false
		try{
			if(! (file = new File(pigFileOrScript)).exists()){
				//the pig script is not a file.
				//create a temporary file to host the script
				isTempFile=true
				file = File.createTempFile("jobName-${System.currentTimeMillis()}", "-temp.pig")
				//add the jars to the pig script
				if(jobName){
					file << "SET job.name \'$jobName\';"
				}

				file << createJarRegisterStatements()
				file << PigModule.replaceParams(PigModule.replaceParams(pigFileOrScript,constants),params);
			}else{

				isTempFile = true

				file = File.createTempFile("jobName-${System.currentTimeMillis()}", "-temp.pig")
				//add the jars to the pig script
				if(jobName){
					file << "SET job.name \'$jobName\';"
				}
				file << createJarRegisterStatements()
				file << PigModule.replaceParams(PigModule.replaceParams( new File(pigFileOrScript).text,constants),params);
				
			}

			println file.text

			Collection args = []

			//set local mode if requested
			if(localMode){
				args << '-x'
				args << 'local'
			}

			//set properties file
			args << '-propertyFile'
			args << loadConfigFile(clusterName)

			//Get the logger file name for the current process
			File logFile = context?.logger?.getLogFile()
			if(logFile){
				args << '-logfile'
				args << logFile.absolutePath
			}else{
				LOG.warn("No log file could be detected from the context")
			}
			
			//add the pig job file
			args << '-f'
			args << file.getAbsolutePath()

			//run pig job
			//--- Run pig as a separate java process
			JavaProcess javaProcess = javaProcessProvider.get()
			
			runningProcesses << javaProcess
			try{
				availableProcesses.acquire()
				javaProcess.run(args)
			}finally{
				availableProcesses.release()
				runningProcesses.remove javaProcess
			}
			
			if(javaProcess.exitValue != 0){
				throw new RuntimeException("Pig Failed");
			}
			
		}finally{

			//cleanup temp file
			if(isTempFile && file){
				file.delete()
			}
		}
		
		return true;
	}

	private String loadConfigFile(String clusterName){
		if(clusterName){
			def clusterConfig = pigConfigurations[clusterName]
			if(!clusterConfig){
				throw new ModuleConfigurationException("The cluster $clusterName is not in the module configuration")
			}
			return clusterConfig
		}else{
			return pigConfigurations[defaultConfiguration]
		}
	}


	/*********************
	 * The methods above all duplicate the funcitonality of the filesystem. 
	 *  
	 */


	boolean mv(String from, String to, String pigConfig=null){
		PigServer p = this.getPigServer(pigConfig)
		def val = p.renameFile(from, to)
		p.shutdown();
		return val;
	}

	boolean rm(String file, String pigConfig=null){
		PigServer p = this.getPigServer(pigConfig)
		def val = p.deleteFile(file)
		p.shutdown();
		return val;
	}

	boolean isFile(String pigConfig, String file){
		return ! this.getPigServer(pigConfig).getPigContext().getDfs().isContainer( file )
	}

	boolean mkdir(String dir,String pigConfig=null){
		PigServer p = this.getPigServer(pigConfig)
		def val = p.mkdirs(dir)
		p.shutdown();
		return val;
	}

	OutputStream openOutputStream(String file, String pigConfig=null){
		PigServer p = this.getPigServer(pigConfig)
		def val = p.getPigContext().getDfs().asElement( file ).create()
		p.shutdown();
		return val;
	}

	InputStream openInputStream(String file, String pigConfig=null){
		PigServer p = this.getPigServer(pigConfig)
		def val = p.getPigContext().getDfs().asElement( file ).open()
		p.shutdown();
		return val;
	}

	String cat(String hadoopPath, String fileName=null, String pigConfig=null) {
		PigServer p = this.getPigServer(pigConfig);
		def f=null;
		if(fileName==null) {
			f = File.createTempFile(new File("cat_"+new Date().getTime()).getName(), ".hadoopcat")
			f.deleteOnExit();
		}
		else {
			f = new File(fileName);
			f.createNewFile();
		}
		println "Opening ${f.getAbsolutePath()} ";
		this.ls(hadoopPath,pigConfig).each{it ->
			if(this.isFile(pigConfig, it)) {
				println "Opening $it";
				AtomicInteger count = new AtomicInteger(0)
				this.openInputStream(it, pigConfig).eachLine{ line ->
					f.append line;
					f.append "\n"
					count.incrementAndGet()
				}
				println "$count lines read";
			}
		}
		return f.getAbsolutePath();
	}


	String[] ls(String path, String pigConfig=null){
		//do not send files with the hdfds prefix
		PigServer p = this.getPigServer(pigConfig)
		def paths = p.listPaths(path)
		def parsedPaths = []

		paths?.each { String absolutePath ->
			if(absolutePath.startsWith('hdfs://')){
				int index = absolutePath.indexOf('/', 7)
				if(index > -1)
					parsedPaths << absolutePath[index .. absolutePath.size()-1]
				else
					parsedPaths << absolutePath
			}else{
				parsedPaths << absolutePath
			}
		}
		p.shutdown();
		return parsedPaths as String[]
	}



	boolean copyFromLocal(GlueContext context, String from, String to,String pigConfig=null) {

		return this.runAsPigCommand(context, pigConfig, "copyFromLocal $from $to")
	}

	boolean copyToLocal(GlueContext context, String from, String to,String pigConfig=null) {

		return this.runAsPigCommand(context, pigConfig, "copyToLocal $from $to")
	}

	boolean cp(GlueContext context, String from, String to, String pigConfig=null) {
		return this.runAsPigCommand(context, pigConfig, "cp $from $to")
	}


	/***********************
	 * The methods above are some private methods.
	 */


	protected Properties getPigProperties(String pigConfig) {
		//println "Loading pigConfig $pigConfig"
		final Properties props = new Properties(System.getProperties());
		if(!pigConfig) pigConfig=this.defaultConfiguration;
		String pigPropFileName=this.pigConfigurations[pigConfig]?.toString();
		if(!pigPropFileName) pigPropFileName=this.pigConfigurations[this.defaultConfiguration]?.toString();
		new File (pigPropFileName).withInputStream { InputStream input -> props.load(input);}
		return props;
	}

	protected PigServer getPigServer(String pigConfigString) {
		return PigServerUtil.create(this.getPigProperties(pigConfigString));
	}

	protected PigServer getPigServer() {
		return PigServerUtil.create(this.getPigProperties(this.defaultConfiguration));
	}

	protected runAsPigCommand(GlueContext context, String pigConfigString, String command){
		File subtsFile = File.createTempFile('glue_pigCommand_','glue')
		subtsFile.append command;
		def success= run(context, 'default', subtsFile.getAbsolutePath() ,[:], pigConfigString)
		subtsFile.delete();
	}
	
	
	protected static String replaceParams(String p,Map params)
	{
		
		params?.each{ String k, String v ->
		 p = p.replace("\$$k", v)	
		}
		return p
	}
	
	@Override
	public Map getInfo() {
		return ['pigConfigurations': this.pigConfigurations, 'defaultConfiguration': this.defaultConfiguration]
	}
}
