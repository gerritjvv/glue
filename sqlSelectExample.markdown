---
layout: doc
title: SQL Select Example
permalink: sqlSelectExample.html
category: tutorial
---


{% include nav.markdown %}


Note: In all the examples ctx refer to the GlueContext in Workflow Process.

The SQL Result Objects returned are from the class (GroovyRowResult)[http://groovy.codehaus.org/api/groovy/sql/GroovyRowResult.html]

#Select and write out results to a file

	def file = ctx.sql.loadSql('mydb', 'select * from tbl')
	

#Select and iterate over results

	ctx.sql.eachSqlResult('mydb', 'select name, age from people', 
	{ res -> 
		println "Name: ${res.name} age: ${res.age}"
	}
	
	or in one line:
	
	ctx.sql.eachSqlResult('mydb', 'select name, age from people',{ println "Name: ${it.name} age: ${it.age}" }
	

	
