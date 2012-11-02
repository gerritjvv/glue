---
layout: doc
title: Nagios
permalink: nagios.html
category: tutorial
---


{% include nav.markdown %}

# Overview

The glue server returns nagios compatible information about each workflow execution.
This means that nagios can be used easily to monitor and notify when workflows have failed.

# Monitoring Workflows


 Checks will return 
 
	0 | if the service is ok
	2 | if the service has error or did not run with in the specified time period
	3 | if the workflow cannot be found in the glue repository
  
  
 Nagios can either check for single workflows i.e.
 
	http://<gluesever>:<port>/check/<workflowName>?expectedRun=30&limit=1
  
	This will check the status of the last run for the workflow specified and return:
  
	2 | if the last run was ERROR OR the last run was completed more than 30 minutes ago.
	0 | if the last run was SUCCESS AND the last run was completed less than 30 minutes ago.
   
 More information can be supplied via the info call:
 
	http://<gluesever>:<port>/history/<workflowName>?limit=10
  
	This will return the last 10 runs one line per run in the following format:
  
	Unit Run ID,STATUS(SUCCESS|ERROR),start date time,end  date time
	
	The date time fields are formatted using yyyy-MM-dd HH:mm:ss
	
# Examples

##Did workflow mytest run with a status OK during the last 20 minutes

	http://localhost:8025/check/mytest?expectedRun=20
  
##Did workflow mytest run with a status OK during the last 24 hours (60 * 24) minutes

	http://localhost:8025/check/mytest?expectedRun=1440
 
##History last 5 days' runs of mytest
 
    http://localhost:8025/history/mytest?limit=5
  
 
 
 

 