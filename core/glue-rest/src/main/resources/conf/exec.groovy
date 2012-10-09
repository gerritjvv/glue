//this property point to a directory containing the glue workflow files
//this directory must be available to the glue server
lookupPath="/opt/glue/workflows"
serverPort=8025

processLogDir='/opt/glue/logs'

executorMaxProcesses=5
processModuleConfig="/opt/glue/conf/workflow_modules.groovy"


//processClassPath = ['/usr/lib/hadoop/lib/', '/usr/lib/hadoop']
//processJavaOpts = ['-Djava.library.path=/opt/hadoop/lib/native/Linux-amd64-64']


unitStatusManager{
	className='org.glue.unit.status.impl.db.DbUnitStatusManager'
	
	config{
		connection.username="sa"
		connection.password=""
		dialect="org.hibernate.dialect.HSQLDialect"
		connection.driver_class="org.hsqldb.jdbcDriver"
		connection.url="jdbc:hsqldb:mem:DbUnitStatusManagerTest"
		hbm2ddl.auto="create"
		connection.autocommit="false"
		show_sql="true"
		cache.use_second_level_cache="false"
		cache.provider_class="org.hibernate.cache.NoCacheProvider"
		cache.use_query_cache="false"
	}
	
}
