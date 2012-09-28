---
layout: doc
title: SQL Module API
permalink: sqlApi.html
category: tutorial
---


{% include nav.markdown %}


The SQL Module supports any database that has a JDBC driver.


Class: [SQLModule](https://github.com/gerritjvv/glue/blob/master/core/glue-modules/src/main/groovy/org/glue/modules/SqlModule.groovy)

Exceptions: All methods throw RunttimeExceptions when an error happened on the database side.


| Method | Description | Example |
| ------ | ----------- | ------- |
| withSql( db:String, closure ) | creates a [Groovy SQL object](http://groovy.codehaus.org/api/groovy/sql/Sql.html) and passes it to the closure as an argument, the sql object is automatically closed after the closure returns  |  ctx.sql.withSql 'mydb', { sql -> sql.execute ('create table mytable (test varchar(10) )' }  
| getSql(db):Sql | returns a [Groovy SQL object](http://groovy.codehaus.org/api/groovy/sql/Sql.html) | def sql = ctx.sql.getSql('mydb') 
| eachSqlResult(db:String, sql:String, closure) | Run a query on db and pass each Groovy Result Instance to the closure | ctx.sql.eachSqlResult 'mydb2', 'select name, age from people', { res -> println "name: ${res.name}, age: ${res.age}" } 
| mysqlImport(db, file:File) | Only works for MySQL databases and is used to load large files rapidly via the mysql command line. The file must be TSV | ctx.sql.mysqlImport('mydb', new File('datafile.tsv')) 
| loadSql(db:String, sql:String, delimiter:String = '\t'): String | writes the results from the sql query into a temporary file| ctx.sql.loadSql('mydb', 'select * from mytable', ',') 
| updateSql(db:String, sql:String):int | runs a sql update command | 
| execSql(db:String, sql:String):String | executes an arbritary sql command and returns a String response if any | 




 