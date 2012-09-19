
/*
mail{
	className='org.glue.modules.MailModule'
	isSingleton=false
	config{
		recipientList='myemail@host.com'
		from_email ='Glue'
	}
}

pig{
	className='org.glue.modules.hadoop.PigModule'
	isSingleton=false
	config{
		clusters{
			dc1{
				isDefault=true
				pigProperties="src/main/resources/pig/cluster1.properties"
			}
		}

		db{
			ans_master{
				host="mydb"
				user="readonly"
				pass="readonly"
				driver="com.mysql.jdbc.Driver"
			}
		}
	}
}

dbstore{
	className='org.glue.modules.DbStoreModule'
	isSingleton=false
	config{

		connection.username="sa"
		connection.password=""
		dialect="org.hibernate.dialect.HSQLDialect"
		connection.driver_class="org.hsqldb.jdbcDriver"
		connection.url="jdbc:hsqldb:mem:dbStoreModuleTest"
		hbm2ddl.auto="create"
		connection.autocommit="false"
		show_sql="true"
		cache.use_second_level_cache="false"
		cache.provider_class="org.hibernate.cache.NoCacheProvider"
		cache.use_query_cache="false"
	}
}
*/
