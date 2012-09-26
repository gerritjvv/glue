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

1. Download the latest rpm from https://github.com/gerritjvv/glue/downloads
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

Follow the [configuration steps](configuration.html) outlined in the documentation.

At a minimum or for quick testing do:

1. Install MySQL
2. Create a database name "glue"
3. Create a user "glue", password "glue"
4. Grant all permissions to the user from localhost

    CREATE USER 'glue'@'localhost' IDENTIFIED BY 'glue';
    
    GRANT ALL ON glue.* TO 'glue'@'localhost';
    
    FLUSH privileges;


### Start Glue Cron

Glue installs init.d scripts to /etc/init.d/gluecron

To start gluecron type:
 service gluecron start
 
Check that its running by looking at the output logs in /opt/gluecron/logs/gluecron.log

 