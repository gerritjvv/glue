---
layout: doc
title: Glue Jython Example
permalink: jythonexample.html
category: tutorial
---


{% include nav.markdown %}


#Overview

Glue supports writing workflows in 100% jython.
 
The GlueContext object is made available to the jython scripts using the ctx and context variables.

From the GlueContext all modules are accessed.


#Closures

Glue and all of its modules are written using Closures.

To convert Jython methods to Groovy Closures use the class org.glue.unit.om.impl.jython.Closure


#Example

Running pig

    from org.glue.unit.om.impl.jython import Closure
    import re
    from string import Template

    def extract_date(path):
      pat = re.compile("date=(\d\d\d\d\d\d\d\d)")
      m = pat.search(str(path), re.I)
      if m is not None:
         date = m.group(0).split('=')[1]
         return dict(year=str(date[0]) + str(date[1]) + str(date[2]) + str(date[3]), month=str(date[4]) + str(date[5]), day=str(date[6]) + str(date[7]))
    
      else:
         return None
	
        #map will contain key = day date, value = tuple (listOfFileIds, listOfPaths)
	fileMap = {}
	def compileReadyFiles(fileId, path):
	    date = extract_date(path)
	    if date is None:
	       return None # exit the method

	    if not ctx.hdfs().exist(path):
	       return None # exit the method

	    key = str(date["year"] + date["month"] + date["day"])
	    if key not in fileMap:
       	fileMap[key] = ([], [])
	
	    (fileIds, paths) = fileMap[key]

	    fileIds.append(fileId)
	    paths.append(str(path))

	#this step will call the compileReadyFiles method for each file seen by the gluecron system in hdfs
	#the compileReadyFiles method builds a map (fileMap) with key = date, value = tuple(listOfFileIds, listOfPaths)
	ctx.triggerStore2().listReadyFiles(Closure(compileReadyFiles))

	#here we create our pig template
	pigStr=Template("""
	        a = LOAD '$files' using PigStorage('\u0001') as (name, geo);

	        g = group a by geo;
	        r = foreach g generate FLATTEN(group), COUNT($1);
	        store r into '/tmp/myfiles/$date'
	""")

	print "FileMap"
	print str(fileMap)

	#iterate over each query and run the pig script
	for date, (fileIds, paths) in fileMap.items():
	    filesStr = ','.join(paths)
	    ctx.pig().run("events_view " + date, pigStr.substitute(files=filesStr, date=date))
	    ctx.triggerStore2().markFilesAsProcessed( fileIds )


