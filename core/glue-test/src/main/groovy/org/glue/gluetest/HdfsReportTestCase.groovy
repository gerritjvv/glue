package org.glue.gluetest

import org.glue.gluetest.util.GlueServerBootstrap


/**
 * Creates a SimpleGlueServer with modules:<br/>
 * <ul>
 *  <li>SQLModule</li>
 *  <li>HdfsModule</li>
 *  <li>HdfsTriggerStoreModule</li>
 *  <li>HdfsTriggerModule</li>
 *  <li>CoordinationModule</li>
 *  <li>DBStoreModule</li>
 *  <li>PigModule</li>
 * </ul>
 * <p/>
 * The local file is uploaded to the hdfs directory returned in HdfsEnrichHourDir.<br/>
 * The glue unit is added to the glue module repository.
 * <p/>
 * When the GlueServer is initialised the serverCreated method is called.
 * <p/>
 * Locally installed libraries:<br/>
 * The following files must be on the local test machine:<br/>
 * <pre>
 * 	'/opt/hadoopgpl/lib/hadoop-lzo.jar',
 *	'/opt/hadoopgpl/lib/protobuf-java-2.3.0.jar',
 *	'/opt/hadoopgpl/lib/protobuf-java-2.3.0.jar',
 *	'/opt/hadoopgpl/lib/protobuf-java-2.3.0.jar',
 *	'/opt/hadoopgpl/lib/protobuf-java-2.3.0.jar',
 *	'/opt/hadoopgpl/lib/pig-0.8.0/elephant-bird.jar'
 * </pre>
 * Native hadoop libraries:<br/>
 * Must be installed on /opt/hadoopgpl/native/Linux-$archType/
 * <p/>
 * DBNames:<br/>
 * The following database is always created: HdfsTriggerModuleBuilder.databaseName.
 */
abstract class HdfsReportTestCase extends GroovyTestCase{

	private boolean started = false;

	SimpleGlueServer server

	/**
	 * Returns the glue unit file location
	 * @return File
	 */
	abstract File[] getGlueUnit()
	/**
	 * Gets a local to hdfs mapping. i.e. each collection of local files will be uploaded<br/>
	 * to the hdfs location that is the map key.
	 * @return Map key = String value = Collection of File
	 */
	abstract Map<String, Collection<File>> getLocalToHdfsFileMap()


	/**
	 * Called after the glue server has been created
	 */
	abstract void serverCreated(SimpleGlueServer glueServer)

	/**
	 * Loads the external libraries
	 * @param libraries Collection of String current libraries
	 * @return Collection of String
	 */
	Collection<String> getExternalLibraries(Collection<String> libraries){
		libraries
	}
	
	/**
	 * The database names to be created
	 * @return Collection String
	 */
	abstract Collection<String> getDBNames()
	
	/**
	 * gives can add to the pig properties
	 * @param properties
	 * @return Map key = String value = String
	 */
	Map<String, String> getPigProperties(Map<String, String> properties){
		properties
	}
	
	/**
	 * Is called to setup the GlueServer<br/>
	 * Will only initialise the GlueServer once.
	 */
	@Override
	protected void setUp() throws Exception {
		if(started){
			return
		}

		started = true

		String archType = System.getProperty("os.arch")

		if(archType.startsWith('i386')){
			archType = 'i386-32'
		}else{
			archType = 'amd64-64'
		}

		System.setProperty('java.library.path', "/opt/hadoopgpl/native/Linux-$archType/")

		def pigProperties = getPigProperties([
					'mapred.child.java.opts':"-Djava.library.path=\"/opt/hadoopgpl/native/Linux-$archType/\""
				]);
		

		def externalLibraries = [
			'/opt/pig/lib/piggybank.jar'
		]
		
		externalLibraries = getExternalLibraries(externalLibraries)
		
		//check that the libraries exist
		externalLibraries.each {
			assertTrue( new File(it).exists() )
		}


		def dbNames = getDBNames()
		if(dbNames){
			dbNames << HdfsTriggerModuleBuilder.databaseName
			dbNames << DbStoreModuleBuilder.DB_NAME
		}else{
			dbNames = [HdfsTriggerModuleBuilder.databaseName, DbStoreModuleBuilder.DB_NAME]
		}
		
		simpleGlueServer = GlueServerBootstrap.createServer()
		simpleGlueServer.addModuleBuilder(new SQLModuleBuilder(dbNames))
		def hdfsModuleBuilder = new HDFSModuleBuilder()

		//we set the pig properties also to hdfs
		//for the lzo native libs
		hdfsModuleBuilder.config = pigProperties
		simpleGlueServer.addModuleBuilder(hdfsModuleBuilder)
		def pigModuleBuilder = new PigModuleBuilder(pigProperties, externalLibraries)
		pigModuleBuilder.setNativeLibraryPath("/opt/hadoopgpl/native/Linux-$archType/")

		simpleGlueServer.addModuleBuilder(pigModuleBuilder)
		simpleGlueServer.addModuleBuilder(new HdfsTriggerModuleBuilder())
		simpleGlueServer.addModuleBuilder(new HdfsTriggerStoreModuleBuilder())
		simpleGlueServer.addModuleBuilder(new CoordinationModuleBuilder())
		simpleGlueServer.addModuleBuilder(new DbStoreModuleBuilder())

		//add the count_clicks to the unit repository
		getGlueUnit()?.each{ File unitFile ->
			simpleGlueServer.addGlueUnit(unitFile.toURI().toURL())
		}

		simpleGlueServer.start()

		//load test data to the hdfs cluster
		hdfsModule = simpleGlueServer.getModuleFactoryProvider().get(null).getModule("hdfs")

		getLocalToHdfsFileMap()?.each { String key, Collection<File> files ->
			hdfsModule.mkdirs(key)
			println "Created directory $key"
			files.each { File localFile ->
				hdfsModule.put(localFile.absolutePath, key)
				println "Loaded file $localFile"
			}
		}

		this.server = simpleGlueServer
		serverCreated(simpleGlueServer)
	}

	@Override
	protected void tearDown() throws Exception {
		server.stop()
	}
	
}