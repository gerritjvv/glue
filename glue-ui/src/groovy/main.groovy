// The config file that is used to load the datasource names.
// The file could be placed anywhere and the path


servers{
	test1{
	    glueUrl{
	        url="http://localhost:8025"
	    }
	    glueDb {
	        driverClassName = "com.mysql.jdbc.Driver"
	        username = "glue"
	        password = "glue"
	
	        dbCreate = "validate"
	        url = "jdbc:mysql://localhost/glue"
	        showSql=true
	    }
	    auxillaryDb {
	        driverClassName = "com.mysql.jdbc.Driver"
	        username = "readonly"
	        password = ""
	
	        url = "jdbc:mysql://localhost/master"
	        showSql=true
	    }
	}

}
