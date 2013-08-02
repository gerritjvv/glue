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


<div id="tabs">
  <ul>
    <li><a href="#tabs-1">Groovy</a></li>
    <li><a href="#tabs-2">Clojure</a></li>
    <li><a href="#tabs-3">Jython</a></li>
  </ul>
  <div id="tabs-1">


     <table>
<thead>
<tr>
<th>Method </th>
<th> Description </th>
<th> Example </th>
</tr>
</thead>
<tbody>
<tr>
<td>{% highlight groovy %} withSql( db:String, closure ) {% endhighlight %} </td>
<td> creates a <a href="http://groovy.codehaus.org/api/groovy/sql/Sql.html">Groovy SQL object</a> and passes it to the closure as an argument, the sql object is automatically closed after the closure returns  </td>
<td>{% highlight groovy %}  ctx.sql.withSql 'mydb', { sql -> sql.execute ('create table mytable (test varchar(10) )' } {% endhighlight %} </td>
</tr>
<tr>
<td> {% highlight groovy %} getSql(db):Sql {% endhighlight %} </td>
<td> returns a <a href="http://groovy.codehaus.org/api/groovy/sql/Sql.html">Groovy SQL object</a> </td>
<td> {% highlight groovy %} def sql = ctx.sql.getSql('mydb') {% endhighlight %} </td>
</tr>
<tr>
<td> {% highlight groovy %} eachSqlResult(db:String, sql:String, closure) {% endhighlight %} </td>
<td> Run a query on db and pass each Groovy Result Instance to the closure </td>
<td> {% highlight groovy %} ctx.sql.eachSqlResult 'mydb2', 'select name, age from people', { res -> println "name: ${res.name}, age: ${res.age}" } {% endhighlight %} </td>
</tr>
<tr>
<td> {% highlight groovy %} mysqlImport(db, file:File) {% endhighlight %} </td>
<td> Only works for MySQL databases and is used to load large files rapidly via the mysql command line. The file must be TSV </td>
<td> {% highlight groovy %} ctx.sql.mysqlImport('mydb', new File('datafile.tsv')) {% endhighlight %} </td>
</tr>
<tr>
<td> {% highlight groovy %} loadSql(db:String, sql:String, delimiter:String = '\t'): String {% endhighlight %}</td>
<td> writes the results from the sql query into a temporary file</td>
<td> {% highlight groovy %} ctx.sql.loadSql('mydb', 'select * from mytable', ',') {% endhighlight %}</td>
</tr>
<tr>
<td> {% highlight groovy %} updateSql(db:String, sql:String):int {% endhighlight %}</td>
<td> runs a sql update command </td>
<td> </td>
</tr>
<tr>
<td> {% highlight groovy %} execSql(db:String, sql:String):String {% endhighlight %}</td>
<td> executes an arbritary sql command and returns a String response if any </td>
<td></td>
</tr>
</tbody>
</table>



  </div>
  <div id="tabs-2">

<table>
<thead>
<tr>
<th>Method </th>
<th> Description </th>
<th> Example </th>
</tr>
</thead>
<tbody>
<tr>
<td> {% highlight clojure %}  (glue/ctx-sql withSql db (fn [sql] )) {% endhighlight %}</td>
<td> creates a <a href="http://groovy.codehaus.org/api/groovy/sql/Sql.html">Groovy SQL object</a> and passes it to the closure as an argument, the sql object is automatically closed after the closure returns  </td>
<td>  </td>
</tr>
<tr>
<td>  {% highlight clojure %} (def sql (glue/ctx-sql getSql db)) {% endhighlight %} </td>
<td> returns a <a href="http://groovy.codehaus.org/api/groovy/sql/Sql.html">Groovy SQL object</a> </td>
<td> </td>
</tr>
<tr>
<td>  {% highlight clojure %} (glue/ctx-sql eachSqlResult db sql (fn [x] )) {% endhighlight %}</td>
<td> Run a query on db and pass each Groovy Result Instance to the closure </td>
<td> </td>
</tr>
<tr>
<td>  {% highlight clojure %} (def r-seq (glue/ctx-sql eachSqlResult db sql)) {% endhighlight %}</td>
<td> Same as above but returns a lazy sequence of result sets </td>
<td></td>
</tr>
<tr>
<td>  {% highlight clojure %} (glue/ctx-sql mysqlImport db file) {% endhighlight %}</td>
<td> Only works for MySQL databases and is used to load large files rapidly via the mysql command line. The file must be TSV </td>
<td> </td>
</tr>
<tr>
<td>  {% highlight clojure %} (glue/ctx-sql loadSql db sql delimiter) {% endhighlight %}</td>
<td> writes the results from the sql query into a temporary file</td>
<td> </td>
</tr>
<tr>
<td>  {% highlight clojure %} (glue/ctx-sql updateSql db sql) {% endhighlight %} </td>
<td> runs a sql update command </td>
<td></td>
</tr>
<tr>
<td> {% highlight clojure %}  (glue/ctx-sql execSql db  sql) {% endhighlight %} </td>
<td> executes an arbritary sql command and returns a String response if any </td>
<td></td>
</tr>
</tbody>
</table>
  
  </div>
  <div id="tabs-3">

<table>
<thead>
<tr>
<th> Method </th>
<th> Description </th>
<th> Example </th>
</tr>
</thead>
<tbody>
<tr>
<td> {% highlight clojure %} ctx.sql.withSql( db, callbackf ) {% endhighlight %} </td>
<td> creates a <a href="http://groovy.codehaus.org/api/groovy/sql/Sql.html">Groovy SQL object</a> and passes it to the closure as an argument, the sql object is automatically closed after the closure returns  </td>
<td>  {% highlight clojure %} ctx.sql.withSql 'mydb', { sql -> sql.execute ('create table mytable (test varchar(10) )' } {% endhighlight %}</td>
</tr>
<tr>
<td> {% highlight clojure %} ctx.sql.getSql(db) {% endhighlight %}</td>
<td> returns a <a href="http://groovy.codehaus.org/api/groovy/sql/Sql.html">Groovy SQL object</a> </td>
<td>  {% highlight clojure %} def sql = ctx.sql.getSql('mydb') {% endhighlight %}</td>
</tr>
<tr>
<td>  {% highlight clojure %} sql = ctx.sql.eachSqlResult(db, sql, callbacf) {% endhighlight %}</td>
<td> Run a query on db and pass each Groovy Result Instance to the closure </td>
<td>  {% highlight clojure %} ctx.sql.eachSqlResult 'mydb2', 'select name, age from people', { res -> println "name: ${res.name}, age: ${res.age}" } {% endhighlight %}</td>
</tr>
<tr>
<td> {% highlight clojure %} rslist = ctx.sql.eachSqlResult(db, sql) {% endhighlight %}</td>
<td> Same as above but returns a lazy sequence of result sets </td>
<td></td>
</tr>
<tr>
<td>  {% highlight clojure %} ctx.sql.mysqlImport(db, file) {% endhighlight %}</td>
<td> Only works for MySQL databases and is used to load large files rapidly via the mysql command line. The file must be TSV </td>
<td></td>
</tr>
<tr>
<td> {% highlight clojure %}  ctx.sql.loadSql(db, sql, delimiter) {% endhighlight %}</td>
<td> writes the results from the sql query into a temporary file</td>
<td></td>
</tr>
<tr>
<td> {% highlight clojure %} ctx.sql.updateSql(db, sql) {% endhighlight %}</td>
<td> runs a sql update command </td>
<td></td>
</tr>
<tr>
<td>  {% highlight clojure %} ctx.sql.execSql(db, sql) {% endhighlight %}</td>
<td> executes an arbritary sql command and returns a String response if any </td>
<td></td>
</tr>
</tbody>
</table>

  </div>
  
</div>
