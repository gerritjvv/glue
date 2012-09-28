---
layout: doc
title: Hadoop Module Each Line Example
permalink: hadoopEachLineExample.html
category: tutorial
---


{% include nav.markdown %}

Compression: Glue will use the hadoop compression codecs installed and configured, thus if the 
cluster supports Snappy, LZO, GZIP, BZIP2 these are automatically suppored in Glue.

GZIP, BZIP2 are also supported via Java APIs if native versions are not available.

This means you can eachLine a directory with files in lzo, snappy, bzip2, and or gzip, mixed or all the same and Glue will
return the plain text lines of all of the files in the directory


#Each Line a directory recursively

	ctx.hdfs.eachLine 'mydir', { line -> println line }
	
	//each line for cluster A and B
	ctx.hdfs.eachLine 'A', 'mydir', { line -> println line }
	ctx.hdsf.eachLine 'B', 'mydir', { line -> println line }
	

#Write a file from each line
	
	new File('mylocalfile).withWriter { writer -> ctx.hdfs.eachLine 'mydir', { line -> writer << "$line\n" } }
	

