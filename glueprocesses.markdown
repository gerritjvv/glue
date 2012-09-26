---
layout: doc
title: Glue Workflow Processes
permalink: glueprocesses.html
category: tutorial
---


{% include nav.markdown %}

# What is a Glue Process

## One Sentence
A Glue Workflow is a combination of processes, and each process in turn performs a series of tasks, which in turn are madeup of statements.


# Logical devision of work

Three ways to think of it:

1. If your a programmer think of a workflow as a Class and Processes as its methods. 
2. Divide a workflow into multiple tasks, a series of tasks can be grouped logically e.g.
3. Divide a workflow into highlevel steps (these are the processes), each step can be completed in a series of steps (tasks).


	load from database
		select data
		write data out as csv
	send data to HDFS
		create directory
		put file into directory
	run query
		create query
		add parameters to query based on data available
		run query
	save output
		download query output
		insert output into mysql

		
# Example
The example above can be written as:

File: myworkflow.groovy

	tasks{
		loadFromDatabase{
			tasks = { context ->
				//select data 
				//write data out as csv
			}
		}
		
		sendToHDFS{
			dependencies="loadFromDatabase"
			tasks = { context ->
				//create directory
				//put file into directory
			}
		}
		
		runQuery{
			dependencies="sendToHDFS"
		    tasks = { context ->
		    	//create query
				//add parameters to query based on data available
				//run query
		    }
		}
		
		saveOutput{
			dependencies="runQuery"
			tasks = { context ->
				//download query output
				//insert output into mysql
			}
		}
		
	}
	
	