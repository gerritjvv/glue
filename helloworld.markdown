---
layout: doc
title: Hello World Example
permalink: helloworld.html
category: tutorial
---


{% include nav.markdown %}

# Workflow

File: /opt/glue/workflows/helloworld.groovy


	tasks{
		helloworld{
			tasks = { context ->
				println "Hello world"
			}
		}
	}
	
	
# Run

Ensure the Glue Rest Server is running.

Type in:

	/opt/glue/bin/glue-client.sh -submit helloworld
	or 
	glue -submit helloworld
	