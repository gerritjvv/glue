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
* Provide utility methods

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

# Utility Methods

 Method | Description |
 ------ | ----------- |
eval(clsName:String, method:String, values:Collection):Object| Creates a new instance of the class clsName and calls the method "method" passing the method arguments values 
eval(clsName:String):Object | Creates a new instance of the class clsName and calls the method "method" without arguments
newInstance(clsName:String, args:Collection):Object| Creates a new instance of the class clsName passing the constructor arguments args
newInstance(clsName:String):Object| Creates a new instance of the class clsName
withTimeout(timeout:long, cl:Closure):Object| Runs the closure within a separate thread and waits for it to complete, if the closure takes long than timeout milliseconds a TimeoutException is thrown. The thread running the closure will be killed.

#Parallel Execution

The method parallel(threads:Int, failOnError:Boolean) will return a TaskExcecutor object with the following methods:

submit(cl:Closure)
await(timeoutInMillis:Long)

These functions allow multiple actions to be executed without having to create a thread pool and monitor it manually.
Any exceptions thrown in the tasks submitted will be rethrown in the await method.

After the await method is called, no more calls to submit will be allowed.

## Examples

## Groovy

ctx.parallel(5, true).submit( { Thread.sleep(100) }).submit( { Thread.sleep(100)} ).await(10000)

## Jython

p = ctx.parallel(5, true)
p.submit(Closure( lamda : 1+1 ) )
p.submit(Closure( lamda : 2+3 ) )
p.await(10000)






