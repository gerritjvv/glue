// The config file that is used to load the datasource names.
// The file could be placed anywhere and the path


servers{
	test1{
		stats{
			workflows="daily_uniques_us_insert"
		}
	    glueUrl{
	        url="http://192.168.56.101:8025"
	    }
	    glueDb {
	        driverClassName = "com.mysql.jdbc.Driver"
	        username = "glue"
	        password = "glue"
	
	        dbCreate = "validate"
	        url = "jdbc:mysql://192.168.56.101/glue"
	        showSql=true
	    }
	    auxillaryDb {
	        driverClassName = "com.mysql.jdbc.Driver"
	        username = "readonly"
	        password = ""
	
	        url = "jdbc:mysql://192.168.56.101/master"
	        showSql=true
	    }
	}

}
