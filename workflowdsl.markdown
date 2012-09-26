---
layout: doc
title: Glue Workflow DSL
permalink: workflowdsl.html
category: tutorial
---


{% include nav.markdown %}

# What is a DSL?

DSL stands for domain specific language, and is groovy's way of allowing language constructs to be written for a specific domain.
Glue's workflows makes use of groovy's domain specific language definition. The workflow is then parsed by the Glue Rest server 
and its processes are executed.

Workflows are divided into Processes and their Tasks.

# Example

To write a workflow we'd start with

 	tasks{
    	//in this code block we write out processes
	}
  
 If we want to add a process "hello" that prints out "hello world" then we'd add
 
 
	tasks{
 		
 		hello{
 		    
 		    tasks = { context ->
 		      //here we can place any valid groovy syntax.
 		      //the context variable acts as a Map or Dictionary that is shared
 		      //between processes
 		      
 		      println "hello world"
 		    }
 		    
 		}
 
	} 
	
	
To add another process "printName" that prints out "Dan" after hello executed we add

	tasks{
 		
 		hello{
 		    
 		    tasks = { context ->
 		      //here we can place any valid groovy syntax.
 		      //the context variable acts as a Map or Dictionary that is shared
 		      //between processes
 		      
 		      println "hello world"
 		    }
 		    
 		}
 		
 		printName{
 		   dependencies = "hello"
 		   
 		   tasks = { context ->
 		       println "Dan"
 		   }
 		   
 		}
 
	} 



	