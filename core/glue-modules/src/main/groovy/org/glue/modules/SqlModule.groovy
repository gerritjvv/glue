package org.glue.modules

import static groovyx.gpars.dataflow.DataFlow.start
import groovy.sql.Sql

import java.util.concurrent.ConcurrentHashMap

import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.CallHelper
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

import com.mchange.v2.c3p0.ComboPooledDataSource

/**
 * Provides groovy SQL object support.<br/>
 * Uses the c3p0 for pooling.
 */
@Typed(TypePolicy.DYNAMIC)
public class SqlModule implements GlueModule {

	def defaultConfiguration="";
	ConfigObject config;

	/**
	 * A map that holds a data source pool for each database.
	 */
	Map<String, ComboPooledDataSource> databaseDataSourceMap = new ConcurrentHashMap<String, ComboPooledDataSource>()

	/**
	 * A map that holds an Sql instance, per glue context.
	 * The key is the glue context id.
	 */
	Map<String, Sql> sqlPerContextMap = new ConcurrentHashMap<String, Sql>()

	void destroy(){
//		databaseDataSourceMap.each { key, pool ->
//                      try{
//			
//                      }catch(t){ 
//                           ;//ignore
//                      }
//		}
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
		return "sql";
	}

	@Override
	public void init(ConfigObject config) {
		this.config=config;
		if(!config.db){
			throw new ModuleConfigurationException("The configuration must have a db section")
		}


		config.db.each{ dbName, ConfigObject conf ->

			println "creating db"
			println """
			 driver: ${getConfProperty(conf, 'driver', 'driverClass')}
			 jdbc: ${getConfProperty(conf, 'jdbc', 'host')}
			 uid: ${getConfProperty(conf, 'user', 'uid')}
			 
			"""

			//create pool data source for each database
			//for each property we support an alternative property name
			//properties
			// driver
			// jdbc
			// user
			// pwd
			// minPoolSize
			// acuireIncrement
			// maxPoolSize
			ComboPooledDataSource dataSource = new ComboPooledDataSource()
			dataSource.setDriverClass(getConfProperty(conf, 'driver', 'driverClass').toString())
			dataSource.setJdbcUrl( getConfProperty(conf, 'jdbc', 'host').toString())
			dataSource.setUser( getConfProperty(conf, 'user', 'uid').toString())

			def pwd = getConfProperty(conf, 'pass', 'pwd', false)
			if(pwd){
				dataSource.setPassword( pwd.toString() )
			}

			def minPoolSize = getConfProperty(conf, 'minPoolSize', 'poolSize', false)
			if(!minPoolSize) minPoolSize = '0'

			dataSource.setMinPoolSize( Integer.parseInt( minPoolSize.toString() ) )
			dataSource.setInitialPoolSize( Integer.parseInt( minPoolSize.toString() ) )

			def acquireIncrement = getConfProperty(conf, 'acquireIncrement', 'inc', false)
			if(!acquireIncrement) acquireIncrement = '1'

			def maxPoolSize = getConfProperty(conf, 'maxPoolSize', 'maxPoolSize', false)
			if(!maxPoolSize) maxPoolSize = '10'

			dataSource.setAcquireIncrement( Integer.parseInt( acquireIncrement.toString() ) )
			dataSource.setMaxPoolSize( Integer.parseInt( maxPoolSize.toString() ) )
			dataSource.setAutoCommitOnClose(true)

			databaseDataSourceMap.put(dbName.toString(), dataSource)
		}
	}

	/**
	 * Get the property by name1 or name2
	 * @param conf
	 * @param name1
	 * @param name2
	 * @param boolean errorNotExist default = false
	 * @return Object
	 */
	private static final getConfProperty(ConfigObject conf, String name1, String name2, boolean errorNotExist = true){

		Object confValue = conf[name1]

		if(!confValue) confValue = conf[name2]

		if(!confValue && errorNotExist){
			throw new ModuleConfigurationException("The configuration must have property $name1 or $name2")
		}

		return confValue
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
		destroy()
	}

	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {

		destroy()

	}

	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
	}

	/**
	 * opens a SQL object, passes the SQL object to the closure, and closes it.
	 * @param db String database name
	 * @param closure
	 */
	public void withSql(db, Object closure){
		def sqlObj = getSql(db)
		try{
			CallHelper.makeCallable(closure).call(sqlObj)
		}finally{
			sqlObj.close()
		}
	}

	/**
	 * Get a Sql instance
	 * @param db String db name
	 * @return Sql
	 */
	public Sql getSql(db){

		def dataSource = databaseDataSourceMap[db?.toString()]

		if(!dataSource){
			throw new ModuleConfigurationException("The database $db does not exist values ( ${databaseDataSourceMap.keySet()})", config)
		}

		new Sql(dataSource)
	}

	/**
	 * Runs a sql command and call the closure with each row found
	 * @param db
	 * @param sql
	 * @param closure Closure is called with one parameter which is an instance of GroovyResultSet
	 */
	public void eachSqlResult(db, sql, Object closure) {
		def sqlObj = getSql(db)
		try{
			def cls = CallHelper.makeCallable(closure)
			
			sqlObj.eachRow(sql.toString()){ row -> cls.call(row) }
		}finally{
			sqlObj.close()
		}
	}

	/**
	 * Loads a file into a database using the mysql client via a Java Process call.
	 * @param db
	 * @param file
	 */
	public void mysqlImport(String db, File file){

		def dataSource = databaseDataSourceMap[db?.toString()]

		if(!dataSource){
			throw new ModuleConfigurationException("The database $db does not exist values ( ${databaseDataSourceMap.keySet()})", config)
		}

		URL jdbcUrl = new URL(dataSource.getJdbcUrl().replace('jdbc:', '').replace('mysql:','http:'));
                

		String host = jdbcUrl.host
		String user = dataSource.getUser();
		String pwd = dataSource.getPassword();
		String database = jdbcUrl.path.replace('/','')

               def mysqlStr = "mysql -u${user} -p${pwd} -h ${host} ${database}"
                println "mysql -u${user} -p'passwordremoved' -h ${host} ${database} < ${file.absolutePath}"
                Process process = "cat ${file.absolutePath}".execute() |  mysqlStr.execute()

                process.consumeProcessOutput(System.out, System.err)

                process.waitFor();
                if(process.exitValue() != 0){
                        //error
                        println "Process Exit value ${process.exitValue()}"
                        throw new RuntimeException("Error while running mysql -u${user} -p'passwordremoved' -h ${host} ${database} < ${file.absolutePath}")
                }else{
                        println 'Mysql Success'
                }


	}

	/**
	 * Save the results of a sql command into a temporary file.
	 * @param db
	 * @param sql
	 * @return String the temporary file name created
	 */
	public String loadSql(String db, String sql, String delimiter ='\t') {
		File uploadFile = File.createTempFile('glue_sqlUpload_','sql')

		String fileLoc=uploadFile.getAbsolutePath();
		def sqlObj = getSql(db)

		uploadFile.withWriter { BufferedWriter writer ->
			try{
                              try{
				sqlObj.eachRow(sql){  row ->

					int length=row.getMetaData().getColumnCount();

					for(def i=0;i<length;i++){
						if(i != 0)
							writer.append(delimiter)

						if(row[i] != null){
							writer.append(row[i].toString());
						}
					}
					writer.newLine()
				}
                               }catch( t){
                                  t.printStackTrace()
                                  throw new RuntimeException(t.toString(), t)
                               }
			}finally{
				sqlObj.close()
			}
		}


		return fileLoc
	}

	/**
	 * @param db
	 * @param sql update sql
	 * @return update count
	 */
	public int updateSql(db, sql){
		def sqlObj = getSql(db)
		try{
			sqlObj.executeUpdate(sql.toString());
		}finally{
			sqlObj.close()
		}
	}

	/**
	 * Execute's a sql command
	 * @param db
	 * @param sql
	 * @return String the sql results
	 */
	public String execSql(db, sql) {
		def sqlObj = getSql(db)
		try{
			sqlObj.execute(sql.toString());
		}finally{
			sqlObj.close()
		}
	}

	@Override
	public Map getInfo() {
		return config;
	}
}
