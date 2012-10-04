---
layout: doc
title: Glue Netezza Module Configuration
permalink: netezzaconfiguration.html
category: tutorial
---


{% include nav.markdown %}



# Overview

The SQL Module use JDBC to connect to any JDBC compliant database.

For more detailed information see [SQL Module Configuration](mysqlconfiguration.html)

# Configuration file

All modules are configured in the /opt/glue/conf/workflow_modules.groovy file

# Configuration Example

	sql{
   		className='org.glue.modules.SqlModule'
   		isSingleton=false
     	config{
         netezza{

              host="jdbc:netezza://serverN/databaseN;loglevel=2;logdirpath=/tmp/netezza_log"
              user="myuser"
              pass="mypass"
              driver="org.netezza.Driver"

       	 }
      	}
	}



 



