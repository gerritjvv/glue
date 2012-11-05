---
layout: doc
title: Glue FTP Module Configuration
permalink: ftpconfiguration.html
category: tutorial
---


{% include nav.markdown %}



# Overview

The FTP Module allows you to configure multiple ftp or sftp sites and assign each a unique name. 
All of the workflows then use this unique name and not worry about the actual configuration, separating configuration out of the workflows.


# Configuration file

All modules are configured in the /opt/glue/conf/workflow_modules.groovy file

# Configuration Example
    ftp{
	className='org.glue.modules.FTPModule'
	 	config{
	 		servers{
	 			myserver{
	 			host='logon url'
	 			port=''
	 			user='user'
	 			pwd='password'
	 			}
	 		}
	 	}
	 }

Properties explained:    

Name | Description 
------ | -----------  
ftp | this is the name we assign to this module, and is used in the context i.e. context.ftp points to this module
className |This points to the module class
servers | group the different ftp configurations, i.e. context.ftp.ls('myserver', 'path') will use the ftp site configured in myserver 
host | the ftp/sftp server name
user | the ftp/sftp user name
pwd | the ftp/sftp password

 



