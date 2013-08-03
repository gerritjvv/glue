---
layout: doc
title: SQL Insert Example
permalink: sqlInsertExample.html
category: tutorial
---


{% include nav.markdown %}


<div id="tabs" style="width:100%">
  <ul>
    <li><a href="#tabs-1">Groovy</a></li>
    <li><a href="#tabs-2">Clojure</a></li>
    <li><a href="#tabs-3">Jython</a></li>
  </ul>
  <div id="tabs-1">


{% highlight groovy %}

ctx.sql.execSql('mydb', "INSERT INTO mytable (name, age) values ('ABC', 123)")

{% endhighlight %}

  </div>
  <div id="tabs-2">

{% highlight clojure %}

(ctx-sql execSql "mydb" "INSERT INTO mytable (name, age) values ('ABC', 123)")

{% endhighlight %}

  </div>
  <div id="tabs-3">

{% highlight python %}

ctx.sql().execSql('mydb', "INSERT INTO mytable (name, age) values ('ABC', 123)")

{% endhighlight %}

  </div>

</div>
                               
