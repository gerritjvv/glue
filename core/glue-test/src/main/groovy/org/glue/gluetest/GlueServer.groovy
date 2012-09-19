package org.glue.gluetest

import org.apache.hadoop.hdfs.MiniDFSCluster
import org.apache.hadoop.mapred.MiniMRCluster
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
import org.hsqldb.Server

/**
 * 
 * Basic interface to the GlueServer testing framework.
 *
 */
interface GlueServer {

	/**
	 * Returns the jdbc hsql string for the database name.<br/>
	 * The database is only created if it doesn't already exist.<br/>
	 * @param dbName
	 */
	String getJDBCConnectionString(String dbName)

	/**
	 * Returns the hsqldb jdbc driver class name
	 * @return String
	 */
	String getJDBDriverClassName()

	/**
	 * Starts the HSQLDB Server, on the default port and localhost.<br/>
	 * This method loads the JDBC driver class.<br/>
	 * To connect via jdbc use:<br/>
	 *  Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/xdb", "SA", "")
	 *
	 */
	void  startDBServer(Collection<String> databases)

	/**
	 * Stop the HSQLDB Server
	 */
	void stopDBServer()

	/**
	 * Remove all units from the repository
	 */
	void clearUnitRepository()
	/**
	 * Remove the unit by name
	 * @param name
	 */
	void removeGlueUnit(String name)

	/**
	 * Adds the unit to the UnitRepository
	 * @param config ConfigObject
	 * @return GlueUnit
	 */
	GlueUnit addGlueUnit(ConfigObject config)

	/**
	 * Adds the unit to the UnitRepository
	 * @param config String
	 * @return GlueUnit
	 */
	GlueUnit addGlueUnit(String config)

	/**
	 * Adds the unit to the UnitRepository
	 * @param url URL
	 * @return GlueUnit
	 */
	GlueUnit addGlueUnit(URL url)

	/**
	 * Adds the unit to the UnitRepository
	 * @param GlueUnit unit
	 */
	void addGlueUnit(GlueUnit unit)

	/**
	 * 
	 * @param builder
	 */
	void addModuleBuilder(ModuleBuilder builder);
	
	/**
	 * Adds a module to the module factory provider
	 * @param name module name
	 * @param config ConfigObject
	 * @param isSingleton
	 */
	void addModule(String name, ConfigObject config, boolean isSingleton)

	/**
	 * Remove module from the module factory provider
	 * @param name
	 */
	void removeModule(String name)
	/**
	 * Starts the GlueExecutor
	 */
	void start()

	void stop()

	void startDFSCluster()
	void shutdownDFSCluster()
	
	GlueModuleFactoryProvider getModuleFactoryProvider()
	GlueUnitRepository getUnitRepository()
	GlueExecutor getExec()
	GlueUnitStatusManager getUnitStatusManager()
	GlueUnitBuilder getUnitBuilder()
	MiniDFSCluster getMiniCluster()
	MiniMRCluster getMiniMRCluster()
	Server getHsqldbServer()
	
}	