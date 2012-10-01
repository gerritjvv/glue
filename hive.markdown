---
layout: doc
title: Hive
permalink: hive.html
category: tutorial
---


{% include nav.markdown %}


Hive has JDBC Drivers thus making it trivial to integrate via the SQL module.

# Add Hive JDBC Drivers and other Jars to the Workflow Class Path

Edit the /opt/glue/conf/exec.groovy file

add to the processClassPath the Hive lib directory e.g.

	processClassPath = ['/opt/glue/lib/', '/usr/lib/pig', '/usr/lib/pig/lib', 
						'/usr/lib/hadoop/lib/', '/usr/lib/hadoop', '/opt/glue/conf', 
						'/usr/lib/hive', '/usr/lib/hive/lib']



Remember to restart the Glue Server after this step.

# Configuration a database in the /opt/glue/conf/workflow_modules.groovy for the SQL Module section

Change the host and port where required

.e.g


	sql{
   		className='org.glue.modules.SqlModule'
   		isSingleton=false
     	config{
        	 db{


            	glue{
                	host="jdbc:mysql://localhost:3306/glue"
                	user="glue"
                	pass="glue"
                	driver="com.mysql.jdbc.Driver"
             	}

            	glue{
                	host="jdbc:hive://localhost:10000/default"
                	user=""
                	pass=""
                	driver="org.apache.hadoop.hive.jdbc.HiveDriver"
             	}



         	}
        }
	}
	

# Hive SQL Queries

IMPORTANT: Hive does not support any if not most of the JDBC statements thus almost none of the Groovy SQL Module's methods can be used
except for the withSQL to get a SQL instance


To query and get the results of a table use:

	tasks{

  		select{
     		tasks = { context ->


        		context.sql.withSql "hive_default", { sql ->

           		def st = sql.getDataSource().getConnection().createStatement()
           		def rsSet = st.executeQuery('select * from mytable limit 10')
				
				//rsSet is the java.sql.ResultSet instance
				//please see http://docs.oracle.com/javase/1.4.2/docs/api/java/sql/ResultSet.html for more information
           		while(rsSet.next()){
            		println rsSet.getString(1)
           		}

        	}

    	 }
  	}


}

# Multiple Hive Databases


Just add the database name as a prefix plus '.' to the table name.

E.g querying mytable in db test


	tasks{

  		select{
     		tasks = { context ->


        		context.sql.withSql "hive_default", { sql ->

           		def st = sql.getDataSource().getConnection().createStatement()
           		def rsSet = st.executeQuery('select * from test.mytable limit 10')
				
				//rsSet is the java.sql.ResultSet instance
				//please see http://docs.oracle.com/javase/1.4.2/docs/api/java/sql/ResultSet.html for more information
           		while(rsSet.next()){
            		println rsSet.getString(1)
           		}

        	}

    	 }
  	  }

	}