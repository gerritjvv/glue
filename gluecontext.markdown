---
layout: doc
title: Glue Workflow Context
permalink: gluecontext.html
category: tutorial
---


{% include nav.markdown %}

The Glue context is a single Map/Dictionary instance shared between all processes in a workflow.

The context is used to:
* Share data between processes
* Provide access to modules

## Sharing Data between Processes

	tasks{
		processA{
			tasks = { context ->
				context.hasData = true
				context.dataSource = "n"
			}
		}
		processB{
			dependencies="processA"
			tasks = { context ->
				if( context.hasData ){
				   println "Data Source: ${context.dataSource}"
				}
				
			}
		}
	}
	
## Accessing a module

	tasks{
		processA{
			tasks = { context ->
			  //uses the hdfs module configured in /opt/glue/conf/workflow_modules.groovy
			  context.hdfs.list "mydir", { print it }
			  
			}
		}
	}
