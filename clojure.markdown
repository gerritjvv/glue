---
layout: doc
title: Glue Clojure
permalink: clojure.html
category: tutorial
---


{% include nav.markdown %}


#Overview

Glue supports writing workflows in 100% clojure.
 
The GlueContext object is made available to the scripts using the glue/ctx and context variables.

From the GlueContext all modules are accessed via the marcos (ctx-<modulename> method args)

Lets say we have a sql module in /opt/glue/conf/wofkflow_modules.groovy

{% highlight clojure %}
 
(def rs (glue/ctx-sql eachSqlResult "glue" "select * from unitfiles"))

(take 10 rs)

{% endhighlight %}

# Workflow naming

All workflow names must be free of '-' or '_' i.e only use "uploadfiles.clj" and not "upload-files.clj"
#Repl

For testing, adhocs and quick debugging the Clojure Repl can be used.

To start the repl type:

    glue -rpel clojure -name <a workflow name for use with triggers>


The name is only usefull when you want to use triggerStore2 methods such as listReadyFiles


Most methods in Glue Modules take a Context object as the first parameter, in the workflow itself
the method call is intercepted and the context is put in automatically.
In the repl this does not happen so you'll need to pass in the Context on each method call.

The Context is available as "glue/ctx"


#Lazy Sequences


The Glue Modules contain methods that return collections, where it makes sense Clojure Lazy sequences have been used in the modules.
For examples see the SQLModule.groovy's eachSqlResult method.

#Closures

Glue and all of its modules are written using Closures.
All of the modules accept either Runnable or Callable instances.

Closure functions are runnable and callable and can thus be used where you see a Closure as an argument in the documentation.





