---
layout: doc
title: SQL Select Example
permalink: sqlSelectExample.html
category: tutorial
---


{% include nav.markdown %}


Note: In all the examples ctx refer to the GlueContext in Workflow Process.

The SQL Result Objects returned are from the class (GroovyRowResult)[http://groovy.codehaus.org/api/groovy/sql/GroovyRowResult.html]

<div id="tabs" style="width:100%">
  <ul>
    <li><a href="#tabs-2">Clojure</a></li>
    <li><a href="#tabs-1">Groovy</a></li>
    <li><a href="#tabs-3">Jython</a></li>
  </ul>
  <div id="tabs-1">

<b>Select and write out results to a file</b>
<br/>

{% highlight groovy %}
def file = ctx.sql.loadSql('mydb', 'select * from tbl')
{% endhighlight %}	

<p/>

<b>Select and iterate over results</b>
<br/>

{% highlight groovy %}

ctx.sql.eachSqlResult('mydb', 'select name, age from people', 
	{ res -> 
		println "Name: ${res.name} age: ${res.age}"
	}
	
	//or in one line:
	
ctx.sql.eachSqlResult('mydb', 'select name, age from people',{ println "Name: ${it.name} age: ${it.age}" }

{% endhighlight %}
	
  </div>
  <div id="tabs-2">

<b>Select and write out results to a file</b>
<br/>

{% highlight groovy %}

(def file (ctx-sql loadSql "mydb" "select * from tbl"))

{% endhighlight %}

<p/>

<b>Select and iterate over results</b>
<br/>

{% highlight groovy %}

(def results (map #{get % "name"} (ctx-sql eachSqlResult "mydb" "select name, age from people")))

{% endhighlight %}

  </div>
  <div id="tabs-3"> 
<b>Select and write out results to a file</b>
<br/>

{% highlight groovy %}


def file = ctx.sql().loadSql("mydb", "select * from tbl")


{% endhighlight %}

<p/>

<b>Select and iterate over results</b>
<br/>
  
{% highlight groovy %}

for rs in ctx.sql().eachSqlResult("mydb", "select name, age from people"):
    print(str(rs))

{% endhighlight %}
  

  </div>

</div>

	
