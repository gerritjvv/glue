---
layout: doc
title: Pig Module API
permalink: pigApi.html
category: tutorial
---


{% include nav.markdown %}


Class: [PigModule](https://github.com/gerritjvv/glue/blob/master/core/glue-modules-hadoop/src/main/groovy/org/glue/modules/hadoop/PigModule.groovy)

Jars are registered automatically and specified in the module configuration, this allows pig scripts to be free of jar register statements.

Its advised to write pig scripts in the workflow but externaly pig files can be called by replacing the script:String with the file name.


Exceptions are thrown if the script pig job fails


| Method | Description | Example |
| ------ | ----------- | ------- |
|run(jobName:String, script:String) | run the string as a pig script | ctx.pig.run('mypigjob', " a = load 'myfile.txt'; g = group a all; r = foreach g generate COUNT(\$1); dump r;") 
|run(script:String, localMode:boolean) | run a script with the option of specifying if its local or remote 
 