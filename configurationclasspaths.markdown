---
layout: doc
title: Glue Configuration Classpath
permalink: configurationclasspaths.html
category: tutorial
---


{% include nav.markdown %}


# Overview

This covers how to add in Hadoop, Pig and Hive dependencies for different hadoop flavours, 
which are not compatible between each other e.g. cloudera cdh4 is not compatible with apache hadoop libraries.


#Workflow Classpath

To point the glue workflow at the correct libraries open the /opt/glue/conf/exec.grooy file 
and configure the processClassPath variables.

## Hadoop 0.20.2 compatible libraries
	processClassPath = ['/opt/glue/lib-pig', '/opt/glue/lib-hadoop', '/opt/glue/lib']

## Cloudera Dependencies
	processClassPath = ['/opt/glue/lib/', '/usr/lib/pig', '/usr/lib/pig/lib', '/usr/lib/hadoop/lib/', '/usr/lib/hadoop', '/opt/glue/conf', '/usr/lib/hive', '/usr/lib/hive/lib']


#Pig Classpath

Pig runs in a different jvm instance than the workflow itself, this allows us to setup the pig classpath to any pig distribution.

Open the /opt/glue/conf/workflow-modules.groovy file and in the pig module configuration, change the classpath property

## Apache Pig 10

	classpath = ['/opt/glue/lib-pig', '/opt/glue/lib-hadoop', '/opt/glue/lib']
	
## Cloudera Dependencies

	classpath = ['/opt/glue/lib/', '/usr/lib/pig', '/usr/lib/pig/lib', '/usr/lib/hadoop/lib/', '/usr/lib/hadoop', '/opt/glue/conf', '/usr/lib/hive', '/usr/lib/hive/lib']





