---
layout: doc
title: Hadoop Module List Files Example
permalink: hadoopListFilesExample.html
category: tutorial
---


{% include nav.markdown %}


# List recursively through a directory and its sub-directories (except hidden directories)

	ctx.hdfs.list 'mydir', { file -> println file }
	
	//do for for cluster A and cluster B
	ctx.hdfs.list 'a', 'mydir', { file -> println file }
	ctx.hdfs.list 'b', 'mydir', { file -> println file }
	
# List non-recursively through a directory
	
		ctx.hdfs.list 'mydir', false, { file -> println file }
		
