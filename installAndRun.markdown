---
layout: doc
title: Install and Run
permalink: installAndRun.html
category: tutorial
---


{% include nav.markdown %}


# Downloads

Glue is split into three installs:

* Glue Rest, the glue server running glue jobs
* Glue UI, a UI for monitoring and viewing running jobs
* Glue Cron, manages cron and data driven glue jobs


## Glue Rest

### RPM install

1. Download the latest rpm from http://code.google.com/p/klej/downloads
2. Run rpm -i <glue rest rpm>
3. Glue installs to /opt/glue



### Debian install

Follow the same instructions as for RPM install excep after downloading run sudo alien <rpm file> to generate a deb file.



### Configure

Follow the [configuration steps](configuration.html) outlined in the documentation.

At a minimum or for quick testing do:

1. Install MySQL
2. Create a database name "glue"
3. Create a user "glue", password "glue"
4. Grant all permissions to the user from localhost

    CREATE USER 'glue'@'localhost' IDENTIFIED BY 'glue';
    
    GRANT ALL ON glue.* TO 'glue'@'localhost';
    
    FLUSH privileges;


### Start Glue Rest

Glue installs init.d scripts to /etc/init.d/glue-server

To start glue type:
 service glue-server start
 
Check that its running by looking at the output logs in /opt/glue/logs


## Glue UI


1. Install Tomcat5 or 6
2. Copy the downloaded glue ui war file to the TOMCAT_HOME/webapps folder 
3. Rename the war file glue.war
4. Restart Tomcat
5. The check that you can see the UI at http://[server]:8080/glue

Follow the [configuration steps](configuration.html) outlined in the documentation.

The GLUE_UI_CONFIG variable must point to a basic configuration file (explained in the documentation)
Convention for this value is to point to /opt/glue/conf/glue-ui.groovy

Set this variable in TOMCAT_HOME/conf/tomcat5.conf or TOMCAT_HOME/conf/tomcat6.conf

e.g.

    export GLUE_UI_CONFIG=/opt/glue/conf/glue-ui.groovy
 


## Glue Cron

### RPM Install

1. Ensure Glue Rest is installed
2. Download the Triggers RPM file
3. Run rpm -i <rpm file>.rpm
4. Glue Cron installs to /opt/gluecron

### Debian install

Follow the same instructions as for RPM install excep after downloading run sudo alien <rpm file> to generate a deb file.


### Configure


1. Install MySQL
2. Create a database name "glue"
3. Create a user "glue", password "glue"
4. Grant all permissions to the user from localhost

    CREATE USER 'glue'@'localhost' IDENTIFIED BY 'glue';
    
    GRANT ALL ON glue.* TO 'glue'@'localhost';
    
    FLUSH privileges;

### Create Tables

Run either:
    /opt/gluecron/bin/dbsetup.sh
     
    or create the tables:

	CREATE TABLE `unittriggers` (
	  `id` int(11) NOT NULL AUTO_INCREMENT,
	  `unit` varchar(100) DEFAULT NULL,
	  `type` varchar(10) DEFAULT NULL,
	  `data` varchar(100) DEFAULT NULL,
	  `lastrun` date DEFAULT NULL,
	  PRIMARY KEY (`id`)
	) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
	
	CREATE TABLE `unitfiles` (
	  `unitid` int(11) DEFAULT NULL,
	  `fileid` int(11) DEFAULT NULL,
	  `status` varchar(10) DEFAULT NULL,
	  UNIQUE KEY `a` (`unitid`,`fileid`)
	) ENGINE=MyISAM DEFAULT CHARSET=latin1;
	
	CREATE TABLE `hdfsfiles` (
	  `id` int(11) NOT NULL AUTO_INCREMENT,
	  `path` varchar(1000) NOT NULL,
	  `seen` tinyint(4) DEFAULT '0',
	  PRIMARY KEY (`id`),
	  UNIQUE KEY `path` (`path`),
	  KEY `seen1` (`seen`)
	) ENGINE=MyISAM AUTO_INCREMENT=1032955 DEFAULT CHARSET=latin1;

### Configuration Parameters

|Property | Description | Default
|---------|-------------|---------
refresh.freq | frequency at which checks are performed in minutes | 5
hdfsfiles.table | database table for hdfs files | hdfsfiles
hdfsfiles-history.table | database table for hdfs files for triggers of type hdfs-history | hdfsfiles
unittriggers.table | database table from which the triggers are read | unittriggers
unitfiles.table | table in which the status of each unit's execution against the hdfs files is stored | unitfiles
unitfiles-history.table | table in which the status of each unit's execution against the hdfs history files is stored | unitfiles

### Configure Hadoop version

It is important that the correct hadoop jars are in the gluecron classpath. One version of Hadoop is not always compatible with another
and for this reason Glue Cron does not package the hadoop libraries.

Ensure that you have the hadoop client installed.

The script /opt/gluecron/conf/env.sh will try to automatically detect the hadoop install and add the jar and configuration dependencies to the classpath.
If you have any problems during starting glue please check that the variable HADOOP_LIB points to the correct locations.

To do so follow the instructions below:

1. Open the file /opt/gluecron/conf/env.sh 
2. Edit and export the variable HADOOP_LIB so that it contains the paths to the hadoop configuration and the hadoop jar files.
 

### Start Glue Cron

Glue installs init.d scripts to /etc/init.d/gluecron

To start gluecron type:
 service gluecron start
 
Check that its running by looking at the output logs in /opt/gluecron/logs/gluecron.log

 
