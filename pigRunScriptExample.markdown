---
layout: doc
title: Pig Run Script Example
permalink: pigRunScriptExample.html
category: tutorial
---


{% include nav.markdown %}


# Single line
	ctx.pig.run('mypigjob', " a = load 'myfile.txt'; g = group a all; r = foreach g generate COUNT(\$1); dump r;")
	
# Multiline

	OUTPUT='myfile.txt'

	ctx.pig.run('mypigjob', 
			""" 
				a = load '$OUTPUT'; -- we are in a Groovy String this argument refers to a variable and is replaced with its value.
				g = group a all; 
				r = foreach g generate COUNT(\$1); -- exscape the $ with \\ 
				dump r;
			""")
			
			
# Multiline without Groovy Template passing arguments manually

	ctx.pig.run('mypigjob', 
			""" 
				a = load '$INPUT'; 
				g = group a all; 
				r = foreach g generate COUNT($1); --note no \\ 
				dump r;
			""", ['INPUT':'myfile.txt')

			
			
# Multiline catch failed job

	OUTPUT='myfile.txt'

    try{
	  ctx.pig.run('mypigjob', 
			""" 
				a = load '$OUTPUT'; -- we are in a Groovy String this argument refers to a variable and is replaced with its value.
				g = group a all; 
				r = foreach g generate COUNT(\$1); -- exscape the $ with \\ 
				dump r;
			""")
			
	}catch(Throwable t){
	   println "Pig Job Failed due to: ${t}"
	}
	