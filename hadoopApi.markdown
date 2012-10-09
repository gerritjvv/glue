---
layout: doc
title: Hadoop Module API
permalink: hadoopApi.html
category: tutorial
---


{% include nav.markdown %}

The Hadoop Module can be used to connect to different cluster.
I.e. you can interact with more than one cluster.
In all methods the clusterName can be excluded to refer to the default configured cluster.

Class: [HDFSModuleImpl](https://github.com/gerritjvv/glue/blob/master/core/glue-modules-hadoop/src/main/groovy/org/glue/modules/hadoop/impl/HDFSModuleImpl.groovy)

| Method | Description | Example |
| ------ | ----------- | ------- |
|cat(clusterName:String = null, path:String):String | Returns the text to a path | println ctx.hdfs.cat('myfile.txt') 
|eachLine(clusterName:String = null, path:String) | Recursively searches for non hidden files in a directory and return each line, if the file is compressed it will be decompressed first, each line is sent to the closure | ctx.hfds.eachLine 'myfile.txt', { line -> println line }
|withDecompressedInputStream(clusterName:String = null, file:String) | Reads the file and if compressed, use the Hadoop configured Codec to decompress, if the file is compressed a CompressionInputStream is passed to the Closure| ctx.hdfs.withDecompressInputStream "myfile.gz", { input ->  /* do stuff */ } 
|list(cluserName:String = null, path:String ) | Recursively lists all non hidden files | ctx.hdfs.list 'mydir', { file -> println file }
|put(clusterName:String = null, src:String, dest:String) | Loads a local file to HDFS | ctx.hdfs.put('myfile.txt', '/hdfsdir/') 
|get(clusterName:String = null, hdfsSrc:String, localDest:String) | Download a file from HDFS to local | ctx.hdfs.get('/hdfsdir/myfile.txt', 'localfile.txt') 
|delete(clusterName:String = null, file:String, recursive:boolean)|Delete a file or directory (if rescursive is true) | 
|exist(clusterName:String = null, file:String):boolean | Returns true if a file or directory exist | ctx.hdfs.exist('mydir') 
|mkdirs(clusterName:String = null, file:String)| Make all parent and sub-directories in the path | ctx.hdfs.mkdirs('/path1/path2/path2') 
|isDirectory(clusterName:String = null, file:String):boolean | Returns true if a directory | 
|isFile(clusterName:String = null, file:String):boolean | Returns true if a file | 
|move(clusterName:String = null, src:String, dest:String) | Move a file from one HDFS location to another HDFS location | ctx.hdfs.put('/hdfsdir1/myfile1.txt', '/hdfsdir2/') 
|open(clusterName:String = null, file:String):FSDataInputStream| Open a InputStream for reading data to an HDFS File |  
|open(clusterName:String = null, file:String, closure)| Open a InputStream for reading data to an HDFS File and send it to the closure | e.g. Write to local file: new File('localfile').withOutputStream { out -> ctx.hdfs.open 'mynfile.txt', {is-> out << is } } 
|create(clusterName:String = null, file:String):FSDataOutputStream | Create a new file on HDFS | 
|create(clusterName:String = null, file:String, closure) | Createa new file on HDFS and sends the FSDataOutputStream to the closure | e.g. Copy a file to hdfs new File('mylocalfile').withInputStream { is -> ctx.hdfs.create('myfile.txt', { out -> out << is } } 
|unTar(localFile:String, localDir:String) | Extracts the contents of a TAR file | ctx.hdfs.unTar('myfile.tar', '/opt/glue/log/${ctx.unitId}/tardir') 
|unZip(localFile:String zipDir:String) | Unzips a ZIP/GZIP file | ctx.hdfs.unZip('myfile.gzip', '/opt/glue/log/${ctx.unitId}/zipdir') 
|getFileStatus(clusterName:String = null, file:String):[FileStatus](http://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/clas-use/FileStatus.html) |  Returns the HDFS File Status | 
|setTimes(clusterName:String = null, file:String, mtime:long, atime:long) | calls the FileSystem.setTimes method on a file| 
|setOwner(clusterName:String = null, file:String, username:String, groupname:String) | calls the FileSystem.setOwner on a file | 
|setPermissions(clusterName:String = null, file:String, unixStylePermissions:String) | calls the FileSystem.setPermission on a file |  
|getContentSummary(clusterName:String = null, file:String)[ContentSummary](http://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/ContentSummary.html) | Returns the content summary of a file or directory | 
|getDefaultBlockSize(clusterName:String = null):long | Returns the default block size | 
|getDefaultReplication(clusterName:String = null):short | Returns the default replication | 
