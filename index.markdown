---
layout: doc
title: Home
permalink: index.html
category: tutorial
---


#Glue Job execution

## Who should use this?

* DevOPS
* Data Analysts
* Programmers


## Overview

Glue is a job execution engine, written in Java and Groovy.
workflows are written in Groovy DSL (simple statements) and use
pre-developed modules to interact with external resources e.g. DBs,
Hadoop, Netezza, FTP etc.

##Abstracting configuration from functionality

In Glue one of the main features and design goals is to always
abstract configuration away from functionality. This means that no
more hardcoded IPs, UserIDs and Passwords spread over 10s of
hundreds of bash/python scripts.

Scripts written for one environment can be easily ported to
another because the configuration is done outside of each workflow.

How is this done?

Each Module has a configuration section in the
/opt/glue/conf/workflow_modules.grooy where data like hosts, ips,
usernames etc are placed. This configuration is loaded and provided
to the Module before starting each workflow.

Configurations can be changed dynamically and are re-read
before each workflow run, such that no restart is required.

##Extending via Modules

Functionality can be added to an already running and installed
Glue instance, via dynamically loaded modules.</p>

A module abstracts away complex and repetitive interactions
with external systems.

E.g.

Glue provides modules for the below systems:

MySQL (via JDBC any JDBC compliant DB) Netezza HDFS Pig Hive
FTP and SFTP

These modules are called via the context in each workflow
script:

E.g. to cat through each line in each file in an HDFS
directory do:

			
context.hdfs.cat 'mydir', { line -&gt; /* do something with the text line */ }

