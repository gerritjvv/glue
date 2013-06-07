---
layout: doc
title: Glue Clojure Example
permalink: clojureexample.html
category: tutorial
---


{% include nav.markdown %}


#Overview

Glue supports writing workflows in 100% clojure.
 
The GlueContext object is made available to the cython scripts using the ctx and context variables.

From the GlueContext all modules are accessed.


#Closures

Glue and all of its modules are written using Closures.
All of the modules accept either Runnable or Callable instances.

Closure functions are runnable and callable and can thus be used where you see a Closure as an argument in the documentation.




