---
layout: doc
title: Glue Logs
permalink: logs.html
category: tutorial
---


{% include nav.markdown %}

Logs are devided into 2 categories:

* Application Logs
* Workflow Logs

# Application Logs
These supply information about the execution of the Glue Rest and Glue Cron servers, and can be found at:

    /opt/glue/logs/server.log
    /opt/gluecron/logs/gluecron.log
    
# Workflow Logs
  
All standard output and standard error output streams are redirected into the workflow logs.
Each workflow's execution has its own separate directory where these logs are stored locally on the same machine 
as where the glue rest server is installed.

The logs have format

    /opt/glue/log/<uuid>/<processName>
    /opt/glue/log/<uuid>/main
    
All workflow logs are available via the Glue UI.
    

    