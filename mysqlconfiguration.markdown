---
layout: doc
title: Glue SQL Module Configuration
permalink: mysqlconfiguration.html
category: tutorial
---


{% include nav.markdown %}



# Overview

The SQL Module allows you to configure multiple databases and assign each database a unique name. 
All of the workflows then use this unique name and not worry about the actual configuration, separating configuration out of the workflows.

Any JDBC compatible database can be configured here, even Hive which is not RDMS.

RDMS DBs with JDBC Drivers

* MySQL
* Oracle
* Lucid
* MongoDB
* CouchDB
* PostgreSQL
* dBase
* DB2
* SyBase
* Derby
* HSQLDB
* SQL Server (via ODBC)
* Almost all RDBMS systems have support for JDBC.

For an extensive list please see [Drivers](http://devapp.sun.com/product/jdbc/drivers)

# Configuration file

All modules are configured in the /opt/glue/conf/workflow_modules.groovy file

# Configuration Example

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

             myotherdb{
                host="jdbc:mysql://serverN:3306/db"
                user="uid1"
                pass="pwd2"
                driver="com.mysql.jdbc.Driver"
             }
             
            hive{
                host="jdbc:hive://localhost:10000/default"
                user="root"
                pass=""
                driver="org.apache.hadoop.hive.jdbc.HiveDriver"
            } 


         	}
      	}
	}


Properties explained:    

Name | Description 
------ | -----------  
sql | this is the name we assign to this module, and is used in the context i.e. context.sql points to this module
className |This points to the module class
isSingleton | legacy value 
db | group the different db configurations, i.e. context.sql.execSql('glue' ... ) will use the db at jdbc:mysql://localhost:3306/glue 
host | the jdbc host name, each database has its own jdbc host configuration url.
user | the database user name
pass | the database password
driver | JDBC driver class, the correct Jar file containing the drivers must be in the glue class path e.g. in /opt/glue/lib, no restart is required

 



