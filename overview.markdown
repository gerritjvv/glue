---
layout: doc
title: Documentation Overview
permalink: overview.html
category: tutorial
---


{% include nav.markdown %}


Glue is a job execution engine, written in Java and Groovy.
workflows are written in Groovy DSL (simple statements) and use
pre-developed modules to interact with external resources e.g. DBs,
Hadoop, Netezza, FTP etc.


Glue helps to 'Glue' together a series of interactions with external systems.

Examples:

Load data from N mysql tables
Push data to Hadoop HDFS
Run a Pig Job 
Download output from HDFS
Push output to MySQL/Netezza

## Data Subscription

One big headache in HDFS BigData is to run script when data becomes available and not just on timed frequency, i.e.
when data arrives we want our workflow(s) to start.

Glue via GlueCron gives the ability to register one or more workflows to one or more HDFS directories.


##Why Groovy

The wofklow logic language is groovy and supports the whole groovy syntax.

Reasons for choosing groovy are:

* Provides any java library which makes extending glue and access functionality already provided in java procects trivial.
	* Usage of JDBC allows automatic and simple support for many databases including NoSQL Hive or Netezza.
	* In BigData currently most software is written either in Java or support Java Directly. 
* Use of dynamic variables and lambda/closure functions is ideal for workflows
* Simple to learn and well supported
* Groovy naturally supports scripting

For more on Groovy please see: http://groovy.codehaus.org/

## No XML Workflows

XML is a terrible language for humans to write in, expecially when writing workflows and process oriented scripts.



 