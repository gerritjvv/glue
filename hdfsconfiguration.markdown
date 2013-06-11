---
layout: doc
title: Glue HDFS Configuration
permalink: hdfsconfiguration.html
category: tutorial

---


{% include nav.markdown %}


# Overview

HDFS is configured using a properties file, the properties file contains the location to the NameNode and JobTracker.
Note that this properties file is exactly the same file as pig uses.

This file can be found at /etc/pig/conf/pig.properties (depending on the pig install)

As a requirement from the HDFS Api the core-site.xml file must be on the classpath, but the file can be empty with only
    <?xml version="1.0"?>
	<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

	<configuration>
	</configuration>
	
	
The HDFS Module allows you to setup and configure HDFS for multiple clusters by pointing a logical name to a different 
properties file, i.e. you can run pig on two different clusters if you wanted to.

# Configuration file

All modules are configured in the /opt/glue/conf/workflow_modules.groovy file

# Configuration Example

	hdfs{
   		className='org.glue.modules.hadoop.impl.HDFSModuleImpl'
   		//must never be a singleton
   		isSingleton=false
   		config{
           clusters{
                   mycluster1{
                           isDefault=true
                           hdfsProperties="/opt/glue/conf/mycluster1.properties"
                   }
                   mycluster2{
                           hdfsProperties="/opt/glue/conf/mycluster2.properties"
                   }
           }
   		}
	}



Properties explained:    

Name | Description 
------ | -----------  
hdfs | this is the name we assign to this module, and is used in the context i.e. context.hdfs points to this module
className |This points to the module class
isSingleton | legacy value 
clusters | group the different cluster configurations, i.e. context.hdfs.run('mycluster1' ... ) will use properties file mycluster1.properties
isDefault | If no cluster is specified in the context.hdfs.[method] then the default properties file is used 


