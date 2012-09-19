package org.glue.gluetest

import java.io.File
import java.io.FileOutputStream

import javax.inject.Inject

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.MiniDFSCluster
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapred.MiniMRCluster
import org.apache.log4j.Logger
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
import org.hsqldb.Server
import org.hsqldb.ServerConstants
import org.hsqldb.jdbcDriver
import org.hsqldb.persist.HsqlProperties

/**
 * Is a class that helps when testing workflows. It starts up a default GlueExecutor.<br/>
 * This class has several injection dependencies. See the GlueServerBootstrap class on how to<br/>
 * create an instance with injected dependencies.<br/>
 * <p/>
 * To get an instance of this class use
 * <pre>SimpleGlueServer server = GlueServerBootstrap.createServer()</pre>
 * <p/>
 * Start database support:<br/>
 * Lets say you have 3 databases named, mydb1, mydb2 and mydb3.<br/>
 * The names need to be given to the GlueServer when the startDBServer method is called:<br/>
 * <pre>server.startDBServer(['mydb1', 'mydb2', 'mydb3'])</pre>
 * NOTE: that hdsqldb only supports a maximum of 10 databases.<br/>
 * <p/>
 * Start Hadoop support that includes, HDFS and Mapred.<br/>
 * <pre>server.startDFSCluster</pre>
 * <p/>
 * Using builders:<br/>
 * The easiest way of configuring the GlueServer is via ModuleBuilder(s).<br/>
 * This project provides several default builders:<br/>
 * <p/>
 * To start a GlueServer with database, hdfs, mapred, sql, and pig support use:<br/>
 * <pre>
 *  SimpleGlueServer server = GlueServerBootstrap.createServer()
 *  server.addModuleBuilder(new SQLModuleBuilder(['mydb1']))
 *  server.addModuleBuilder(new HDFSModuleBuilder())
 *	server.addModuleBuilder(new PigModuleBuilder())
 *
 *  //submit work flow
 *	def unitId = server.exec.submitUnitAsText(unitText, [:])
 *	server.exec.waitFor(unitId)
 * </pre>
 * 
 * 
 * 
 * 
 */
class SimpleGlueServer implements GlueServer{

	private static final Logger LOG = Logger.getLogger(SimpleGlueServer)

	@Inject
	GlueModuleFactoryProvider moduleFactoryProvider
	@Inject
	GlueUnitRepository unitRepository

	@Inject
	GlueExecutor exec

	@Inject
	GlueUnitStatusManager unitStatusManager

	@Inject
	GlueUnitBuilder unitBuilder

	//-------------------- Hadoop mini cluster for testing ---------////
	/**
	 * Mini DFS Cluster<br/>
	 * See startDFSCluster()
	 */
	MiniDFSCluster miniCluster
	/**
	 * Mini Mapred Cluster<br/>
	 * See startDFSCluster()
	 */
	MiniMRCluster miniMRCluster

	/**
	 * Hdfs configuration used for the DFSCluster
	 */
	Configuration hdfsConfiguration = new Configuration()
	/**
	 * Number of data nodes, value == 1
	 */
	int dataNodes = 1
	/**
	 * Number of replication blocks, value = 1
	 */
	int dfsReplication = 1;
	/**
	 * Number of task trackers, value = 1
	 */
	int taskTrackers = 1

	//------------------- Hsqldb database server  -----------------/////
	/**
	 * HSQLDB Server<br/>
	 * See startDBServer
	 */
	Server hsqldbServer
	/**
	 * The base directory from which the databases are created.
	 */
	File baseDirDBDir

	/**
	 * All module builder instances added via the addModuleBuilder method are added to this collection.<br/>
	 */
	Collection<ModuleBuilder> moduleBuilders = []

	/**
	 * Returns the jdbc hsql string for the database name.<br/>
	 * The database is only created if it doesn't already exist.<br/>
	 * @param dbName
	 */
	String getJDBCConnectionString(String dbName){
		"jdbc:hsqldb:hsql://localhost/$dbName;ifexists=true"
	}

	/**
	 * Returns the hsqldb jdbc driver class name
	 * @return String
	 */
	String getJDBDriverClassName(){
		"org.hsqldb.jdbcDriver"
	}
	/**
	 * Starts the HSQLDB Server, on the default port and localhost.<br/>
	 * This method loads the JDBC driver class.<br/>
	 * To connect via jdbc use:<br/>
	 *  Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/xdb", "SA", "")
	 * 
	 */
	synchronized void  startDBServer(Collection<String> databases){
		if(!hsqldbServer){
			//load JDBC
			println jdbcDriver


			baseDirDBDir = new File("build/dbserver/${System.currentTimeMillis()}")
			baseDirDBDir.mkdirs()
			HsqlProperties props = new HsqlProperties();

			//add each database to the server properties
			databases.eachWithIndex { String name, int index ->
				props.setProperty("server.dbname.$index", name); //server alias
				props.setProperty("server.database.$index", "${baseDirDBDir.absolutePath}/$name"); //server file path
			}

			props.setProperty("server.trace", "false");
			props.setProperty("server.silent", "true");

			hsqldbServer = new Server();
			hsqldbServer.setProperties(props);

			//check that the dbs have been opened
			if(!hsqldbServer.openDatabases()){
				throw hsqldbServer.getServerError()
			}

			//set state online and start server
			hsqldbServer.setState(ServerConstants.SERVER_STATE_ONLINE);
			hsqldbServer.start()
		}
	}

	/**
	 * Stop the HSQLDB Server 
	 */
	synchronized void stopDBServer(){
		try{
			hsqldbServer?.shutdown();
		}catch(t){
			LOG.warn("Error stopping hdsql db server $t")
		}
		try{
			baseDirDBDir?.deleteDir()
		}catch(t){
			LOG.warn("Error cleaning hdsql db server directories")
		}

		hsqldbServer = null
	}

	/**
	 * 
	 */
	synchronized void startDFSCluster(){
		startDFSCluster(null)
	}

	/**
	 * Start a default DFS Cluster	
	 */
	synchronized void startDFSCluster(Map hdfsConfig){
		if(!miniCluster){

			def hadoopLogDir = "build/test/logs"
			def dataLogDir = "build/test/data"

			//remove previous hadoop namenode
			//and storage directories
			new File(hadoopLogDir).deleteDir()
			new File(dataLogDir).deleteDir()

			hdfsConfiguration.setLong('dfs.block.size', 512);
			hdfsConfiguration.setInt('dfs.replication', dfsReplication);
			hdfsConfiguration.setStrings("hadoop.log.dir", hadoopLogDir)
			
			System.setProperty("hadoop.log.dir", hadoopLogDir);
			
			hdfsConfig?.each { key, val ->
				hdfsConfiguration.set(key, val)
			}
			
			miniCluster = new MiniDFSCluster(
					hdfsConfiguration, dataNodes, true, null
					)

			miniCluster.waitActive();
			def fs = miniCluster.getFileSystem();
			
			JobConf conf = new JobConf(hdfsConfiguration)
			
			hdfsConfig?.each { key, val ->
				conf.set(key, val)
			}
			
			miniMRCluster = new MiniMRCluster(taskTrackers, fs.getUri().toString(), 1,
				null, null, conf)

			// Create the configuration hadoop-site.xml file
			File conf_dir = new File(System.getProperty("user.home"), "pigtest/conf/");
			conf_dir.mkdirs();
			File conf_file = new File(conf_dir, "hadoop-site.xml");

			// Write the necessary config info to hadoop-site.xml
			def m_conf = miniMRCluster.createJobConf();
			m_conf.setInt("mapred.submit.replication", 2);
			m_conf.set("dfs.datanode.address", "0.0.0.0:0");
			m_conf.set("dfs.datanode.http.address", "0.0.0.0:0");
			m_conf.writeXml(new FileOutputStream(conf_file));

			// Set the system properties needed by Pig
			System.setProperty("cluster", m_conf.get("mapred.job.tracker"));
			System.setProperty("namenode", m_conf.get("fs.default.name"));
			System.setProperty("junit.hadoop.conf", conf_dir.getPath());
			
		}
	}

	/**
	 * Shutdown the hdfs minit cluster if it was started
	 */
	void shutdownDFSCluster(){
		try{
			miniCluster?.shutdown()
		}catch(t){
			LOG.warn("Error shutting down mini hdfs cluster $t")
		}
		try{
			miniMRCluster?.shutdown()
		}catch(t){
			LOG.warn("Error shutting down mini mapred cluster $t")
		}

		miniCluster = null
		miniMRCluster = null
	}
	/**
	 * Remove all units from the repository
	 */
	void clearUnitRepository(){
		unitRepository.clear()
	}

	/**
	 * Remove the unit by name 
	 * @param name
	 */
	void removeGlueUnit(String name){
		unitRepository.remove name
	}

	/**
	 * Adds the unit to the UnitRepository
	 * @param config ConfigObject
	 * @return GlueUnit
	 */
	GlueUnit addGlueUnit(ConfigObject config){
		GlueUnit unit = unitBuilder.build(config)
		unitRepository << unit
		return unit
	}

	/**
	 * Adds the unit to the UnitRepository
	 * @param config String
	 * @return GlueUnit
	 */
	GlueUnit addGlueUnit(String config){
		GlueUnit unit = unitBuilder.build(config)
		unitRepository << unit
		return unit
	}

	/**
	 * Adds the unit to the UnitRepository
	 * @param url URL
	 * @return GlueUnit
	 */
	GlueUnit addGlueUnit(URL url){
		GlueUnit unit = unitBuilder.build(url)
		unitRepository << unit
		return unit
	}

	/**
	 * Adds the unit to the UnitRepository
	 * @param GlueUnit unit
	 */
	void addGlueUnit(GlueUnit unit){
		unitRepository << unit
	}


	/**
	 * Adds a module to the module factory provider
	 * @param name module name
	 * @param config ConfigObject
	 * @param isSingleton
	 */
	void addModule(String name, ConfigObject config, boolean isSingleton){
		moduleFactoryProvider.addModule(name, config)
	}

	/**
	 *
	 * @param builder
	 */
	void addModuleBuilder(ModuleBuilder builder){
		moduleBuilders << builder

		addModule(builder.name, builder.buildModule(this), builder.isSingleton())
	}


	/**
	 * Remove module from the module factory provider
	 * @param name 
	 */
	void removeModule(String name){
		moduleFactoryProvider.removeModule(name)
	}

	/**
	 * Starts the GlueExecutor
	 */
	void start(){
	}


	/**
	 * Stops all services.
	 * After this method the SimpleGlueServer instance should be discarded.
	 */
	void stop(){

		exec.shutdown()
		exec.waitUntillShutdown()

		shutdownDFSCluster()
		stopDBServer()

		moduleBuilders?.each {
			try{
				it.close()
			}catch(t){
				LOG.warn("Error closing builder $it: $t")
			}
		}
	}
}