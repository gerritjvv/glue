//this property point to a directory containing the glue workflow files
//this directory must be available to the glue server
lookupPath="/opt/glue/workflows"
serverPort=8025

processLogDir='/opt/glue/log'

executorMaxProcesses=5
processModuleConfig="/opt/glue/conf/workflow_modules.groovy"

//hadoop 0.20.2 pig 10
processClassPath = ['/opt/glue/lib-pig', '/opt/glue/lib-hadoop', '/opt/glue/lib']

//cloudera hadoop, pig, hive
//processClassPath = ['/opt/glue/lib/', '/usr/lib/pig', '/usr/lib/pig/lib', '/usr/lib/hadoop/lib/', '/usr/lib/hadoop', '/opt/glue/conf', '/usr/lib/hive', '/usr/lib/hive/lib']

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
