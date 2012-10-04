---
layout: doc
title: Glue Configuration
permalink: configuration.html
category: tutorial
---


{% include nav.markdown %}


# Overview

Glue has two primary configuration files

File | Description | Restart if edit
------ | ----------- | ------- 
/opt/glue/conf/exec.groovy | Used to configure the Glue Server and the Workflow class paths | Yes 
/opt/glue/conf/workflow_modules | Used to configure modules used by the workflows | Yes


# exec.groovy


Property Name | Description 
 ------ | ----------- | ------- 
lookupPath | Points at the workflow directories, this is a command separated string
serverPort | The Glue Server will listen on this port for workflow submits
processLotDir | Points at a single directory to where worfklow run logs are written to
executorMaxProcess | The maximum amount of workflows that are allowed to run at any time
processModuleConfig | Points to the module configuration file
processJavaOpts | The Java options sent to each worfklow run's JVM, paramemters like -Djava.library etc can be specified here


#Unit Status Manager

All workflow runs and the status of their runs are saved to a JDBC compliant database.
This database is configured in the exec.groovy file

The easiest way to setup is to install MySQL, create a Glue database with user glue and pwd glue.
Then start the Glue Server, and it will automatically create the database schemas using Hibernate.


	unitStatusManager{
    	className='org.glue.unit.status.impl.db.DbUnitStatusManager'

    	config{
        host="jdbc:mysql://serverN:3306/glue"
        connection.username="glue"
        connection.password="glue"
        dialect="org.hibernate.dialect.MySQLDialect"
        connection.driver_class="com.mysql.jdbc.Driver"
        connection.url="jdbc:mysql://serverN:3306/glue"
        hbm2ddl.auto="update"
        connection.autocommit="false"
        show_sql="false"
        cache.use_second_level_cache="false"
        cache.provider_class="org.hibernate.cache.NoCacheProvider"
        cache.use_query_cache="false"
        connection.provider_class="org.hibernate.connection.C3P0ConnectionProvider"
        c3p0.min_size="5"
        c3p0.max_size="100"
        c3p0.timeout="1800"
        c3p0.max_statements="500"
    	}

	}
	
# Example Config file


	lookupPath="/opt/glue/workflows"

	serverPort=8025

	processLogDir='/opt/glue/log'
	executorMaxProcesses=4
	processModuleConfig="/opt/glue/conf/workflow_modules.groovy"


	processJavaOpts = ['-Djava.library.path=/opt/hadoop/lib/native/Linux-amd64-64']
	unitStatusManager{
    className='org.glue.unit.status.impl.db.DbUnitStatusManager'

    config{
        host="jdbc:mysql://localhost:3306/glue"
        connection.username="glue"
        connection.password="glue"
        dialect="org.hibernate.dialect.MySQLDialect"
        connection.driver_class="com.mysql.jdbc.Driver"
        connection.url="jdbc:mysql://localhost:3306/glue"
        hbm2ddl.auto="update"
        connection.autocommit="false"
        show_sql="false"
        cache.use_second_level_cache="false"
        cache.provider_class="org.hibernate.cache.NoCacheProvider"
        cache.use_query_cache="false"
        connection.provider_class="org.hibernate.connection.C3P0ConnectionProvider"
        c3p0.min_size="5"
        c3p0.max_size="100"
        c3p0.timeout="1800"
        c3p0.max_statements="500"
    }

	}


            