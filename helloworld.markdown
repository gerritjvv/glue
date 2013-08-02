---
layout: doc
title: Hello World Example
permalink: helloworld.html
category: tutorial
---


{% include nav.markdown %}

# Workflow

<div id="tabs">
  <ul>
    <li><a href="#tabs-1">Groovy</a></li>
    <li><a href="#tabs-2">Clojure</a></li>
    <li><a href="#tabs-3">Jython</a></li>
  </ul>
  <div id="tabs-1">

File: /opt/glue/workflows/helloworld.groovy

{% highlight groovy %}
	tasks{
		helloworld{
			tasks = { context ->
				println "Hello world"
			}
		}
	}
	
{% endhighlight %}

  </div>
  <div id="tabs-2">
File: /opt/glue/workflows/helloworld.clj

{% highlight clojure %}

(prn (str "Hi ctx is " glue/ctx))

{% endhighlight %}

  </div>
  <div id="tabs-3">
File: /opt/gue/workflows/helloworld.jython

{% highlight python %}

print "Hi ctx is " ctx

{% endhighlight %}
  </div>
</div>
	
# Run

Ensure the Glue Rest Server is running.

Type in:

	/opt/glue/bin/glue-client.sh -submit helloworld
	or 
	glue -submit helloworld
	
