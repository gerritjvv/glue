---
layout: doc
title: Glue Pig Module Configuration
permalink: pigconfiguration.html
category: tutorial
---


{% include nav.markdown %}


# Overview

Pig is configured using a properties file, the properties file contains the location to the NameNode and JobTracker.
This file can be found at /etc/pig/conf/pig.properties (depending on the pig install)
As a requirement from the HDFS Api the core-site.xml file must be on the classpath, but the file can be empty with only
    <?xml version="1.0"?>
	<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

	<configuration>
	</configuration>
	
	
The Pig Module allows you to setup and configure Pig for multiple clusters by pointing a logical name to a different 
properties file, i.e. you can run pig on two different clusters if you wanted to.

# Configuration file

All modules are configured in the /opt/glue/conf/workflow_modules.groovy file

# Note for CHD4 Users

  Use the below classpath in your pig module configuration

    classpath = ['/usr/lib/hadoop/client-0.20/hadoop-core-2.0.0-mr1-cdh4.1.0.jar',
                    '/usr/lib/pig', '/usr/lib/pig/lib', '/opt/glue/lib/', '/usr/lib/hadoop', '/usr/lib/hadoop/lib', '/opt/glue/conf']

  Its important to have the mr1 jar first in the classpath

# Note hadoop 2.2.0 Users

 Glue comes with hadoop 2.2.0 jars in the /opt/glue/pig/lib-hadoop-2.2.0 directory, and a hadoopless version of pig in /opt/glue/lib-pig

 By default use the hadoop installed locally on the client, but if non is available use the glue provided hadoop.

    classpath = ['/opt/glue/lib', '/opt/glue/conf', '/opt/hadoop', '/opt/hadoop/conf', '/opt/glue/lib-pig']  
 
 Configuration for pig is different with yarn because there is no job tracker.
 A pig configuration should look like:
    
    fs.defaultFS=hdfs://localhost:8020
    yarn.resourcemanager.address=localhost
    yarn.resourcemanager.scheduler.address=localhost
    yarn.resoucemanager.resource-tracker.address=localhost

 

# Configuration Example

	pig{
   		className='org.glue.modules.hadoop.PigModule'
   		isSingleton=false
   		config{
       	jars = []
       	classpath = ['/usr/lib/pig', '/usr/lib/pig/lib', '/opt/glue/lib/', '/usr/lib/hadoop', '/usr/lib/hadoop/lib', '/opt/glue/conf']
           clusters{
                   mycluster1{
                           isDefault=true
                           pigProperties="/opt/glue/conf/mycluster1.properties"
                   }
                   mycluster2{
                           pigProperties="/opt/glue/conf/mycluster2.properties"
                   }
           	}

        }

  	}


Properties explained:    

Name | Description 
------ | -----------  
pig | this is the name we assign to this module, and is used in the context i.e. context.pig points to this module
className |This points to the module class
isSingleton | legacy value 
jars | is a list of jars that pig will register for queries 
classpath | Each pig script is run in a separate JVM, the class path can be set to include any libraries 
clusters | group the different cluster configurations, i.e. context.pig.run('mycluster1' ... ) will use properties file mycluster1.properties
isDefault | If no cluster is specified in the context.pig.run then the default properties file is used 



