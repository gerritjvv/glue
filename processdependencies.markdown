---
layout: doc
title: Glue Workflow Process Dependencies
permalink: processdependencies.html
category: tutorial
---


{% include nav.markdown %}

# Process execution

All processes for a workflow are combined into a [directed asyclic graph](http://en.wikipedia.org/wiki/Directed_acyclic_graph).
The processes on each level of the graph are executed in parallel.

# Dependencies between processes

If B depends on process A, C on process A, and D on A,C then the execution order is:

	A, B, C, D

or
	A, C, B, D
	
or
	A, C, D, B

This can be written in the workflow like so:

	tasks{
		A{
			tasks = { context -> }
		}
		B{
			dependencies="A"
			tasks = { context -> }
		}
		C{
			dependencies="A"
			tasks = { context -> }
		}
		D{
			dependencies="A,C"
			tasks = { context -> }
		}
	}


