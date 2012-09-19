lookupPath="src/test/resources/workflows"
serverPort=8025


processLogDir='target'

executorMaxProcesses=5
processModuleConfig="src/test/resources/modules.groovy"


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