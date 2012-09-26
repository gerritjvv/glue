---
layout: doc
title: Running Bash, Python or any external script
permalink: bashPythonScripts.html
category: tutorial
---


{% include nav.markdown %}

Groovy allows execution of external commands, i.e. any command or program that can be run from the shell command line,
using a simple syntax like 
	
	["cmd", "arg1", "arg2"].execute()

For more please see [Groovy Process Management](http://groovy.codehaus.org/Process+Management)


#Example 

Lets say we have a script called dosomestuff.py that takes a single argument 1

To run this from a glue workflow we do:

	tasks{
		process{
			tasks = { context ->
				["python", "dosomestuff.py", "1"].execute()
			}
		}
	}
