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

<div id="tabs" style="width:100%">
  <ul>
    <li><a href="#tabs-1">Groovy</a></li>
    <li><a href="#tabs-2">Clojure</a></li>
    <li><a href="#tabs-3">Jython</a></li>
  </ul>
  <div id="tabs-1">
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
<td>cat(clusterName:String = null, path:String):String </td>
<td> Returns the text to a path </td>
<td>{% highlight groovy %} println ctx.hdfs.cat('myfile.txt') {% endhighlight %}</td>
</tr>
<tr>
<td>eachLine(clusterName:String = null, path:String) </td>
<td> Recursively searches for non hidden files in a directory and return each line, if the file is compressed it will be decompressed first, each line is sent to the closure </td>
<td>{% highlight groovy %}ctx.hfds.eachLine 'myfile.txt', { line -> println line }{% endhighlight %}</td>
</tr>
<tr>
<td>withDecompressedInputStream(clusterName:String = null, file:String) </td>
<td> Reads the file and if compressed, use the Hadoop configured Codec to decompress, if the file is compressed a CompressionInputStream is passed to the Closure</td>
<td>{% highlight groovy %} ctx.hdfs.withDecompressInputStream "myfile.gz", { input ->   }{% endhighlight %}</td>
</tr>
<tr>
<td>list(cluserName:String = null, path:String ) </td>
<td> Recursively lists all non hidden files </td>
<td>{% highlight groovy %}ctx.hdfs.list 'mydir', { file -> println file }{% endhighlight %}</td>
</tr>
<tr>
<td>put(clusterName:String = null, src:String, dest:String) </td>
<td> Loads a local file to HDFS </td>
<td>{% highlight groovy %}ctx.hdfs.put('myfile.txt', '/hdfsdir/'){% endhighlight %}</td>
</tr>
<tr>
<td>get(clusterName:String = null, hdfsSrc:String, localDest:String) </td>
<td> Download a file from HDFS to local </td>
<td>{% highlight groovy %}ctx.hdfs.get('/hdfsdir/myfile.txt', 'localfile.txt'){% endhighlight %}</td>
</tr>
<tr>
<td>delete(clusterName:String = null, file:String, recursive:boolean)</td>
<td>Delete a file or directory (if rescursive is true)  </td>
<td></td>
<td></td>
</tr>
<tr>
<td>exist(clusterName:String = null, file:String):boolean </td>
<td> Returns true if a file or directory exist </td>
<td>{% highlight groovy %}ctx.hdfs.exist('mydir'){% endhighlight %}</td>
</tr>
<tr>
<td>mkdirs(clusterName:String = null, file:String)</td>
<td> Make all parent and sub-directories in the path </td>
<td>{% highlight groovy %}ctx.hdfs.mkdirs('/path1/path2/path2'){% endhighlight %}</td>
</tr>
<tr>
<td>isDirectory(clusterName:String = null, file:String):boolean </td>
<td> Returns true if a directory  </td>
<td></td>
</tr>
<tr>
<td>isFile(clusterName:String = null, file:String):boolean </td>
<td> Returns true if a file </td>
<td></td>
</tr>
<tr>
<td>move(clusterName:String = null, src:String, dest:String) </td>
<td> Move a file from one HDFS location to another HDFS location </td>
<td>{% highlight groovy %}ctx.hdfs.put('/hdfsdir1/myfile1.txt', '/hdfsdir2/'){% endhighlight %}</td>
</tr>
<tr>
<td>open(clusterName:String = null, file:String):FSDataInputStream</td>
<td> Open a InputStream for reading data to an HDFS File   </td>
<td></td>
</tr>
<tr>
<td>open(clusterName:String = null, file:String, closure)</td>
<td> Open a InputStream for reading data to an HDFS File and send it to the closure </td>
<td> e.g. Write to local file: new File('localfile').withOutputStream { out -> ctx.hdfs.open 'mynfile.txt', {is-> out &lt;&lt; is } } </td>
</tr>
<tr>
<td>create(clusterName:String = null, file:String):FSDataOutputStream </td>
<td> Create a new file on HDFS </td>
<td></td>
</tr>
<tr>
<td>unTar(localFile:String, localDir:String) </td>
<td> Extracts the contents of a TAR file </td>
<td>{% highlight groovy %}ctx.hdfs.unTar('myfile.tar', '/opt/glue/log/${ctx.unitId}/tardir'){% endhighlight %}</td>
</tr>
<tr>
<td>unZip(localFile:String zipDir:String) </td>
<td> Unzips a ZIP/GZIP file </td>
<td>{% highlight groovy %}ctx.hdfs.unZip('myfile.gzip', '/opt/glue/log/${ctx.unitId}/zipdir'){% endhighlight %}</td>
</tr>
<tr>
<td>getFileStatus(clusterName:String = null, file:String):<a href="http://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/clas-use/FileStatus.html">FileStatus</a> </td>
<td>  Returns the HDFS File Status </td>
<td></td>
</tr>
<tr>
<td>setTimes(clusterName:String = null, file:String, mtime:long, atime:long) </td>
<td> calls the FileSystem.setTimes method on a file</td>
<td></td>
</tr>
<tr>
<td>setOwner(clusterName:String = null, file:String, username:String, groupname:String) </td>
<td> calls the FileSystem.setOwner on a file </td>
<td></td>
</tr>
<tr>
<td>setPermissions(clusterName:String = null, file:String, unixStylePermissions:String) </td>
<td> calls the FileSystem.setPermission on a file  </td>
<td></td>
</tr>
<tr>
<td>getContentSummary(clusterName:String = null, file:String)<a href="http://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/ContentSummary.html">ContentSummary</a> </td>
<td> Returns the content summary of a file or directory </td>
<td></td>
</tr>
<tr>
<td>getDefaultBlockSize(clusterName:String = null):long </td>
<td> Returns the default block size</td>
<td></td>
</tr>
<tr>
<td>getDefaultReplication(clusterName:String = null):short </td>
<td> Returns the default replication</td>
<td></td>
</tr>
<tr>
<td>timeSeries(n:String,tableHDFSDir:String,nowdate:Date, modifyTime:String, partitionFormatter:Closure<String>, dateIncrement:Closure<Date>, collector:Collector = null)</td>
<td> Iterates from nowdate-n (e.g. nowdate - 2.hours) on a date increment over a series of date partitioned HDFS files and return true if all of the partitions have files and have not been modified since a specified amount of time  </td>
<td></td>
</tr>
<tr>
<td>downloadChunked(clusterName:String=null, hdfsDir:Collection<String>, localDir:String, chunkSize:int=1073741824, compression:String = "gz", callBack:Closure) </td>
<td> Performs a eachLine on each of the direcotries in hdfsDir, writing the data (compressed) to a local part file, when the chunk size is reached the file is rolled and its file name send to the callBack closure. </td>
<td></td>
</tr>
</tbody>
</table>


  </div>
  <div id="tabs-2">

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
<td>cat(clusterName:String = null, path:String):String </td>
<td> Returns the text to a path </td>
<td>{% highlight clojure %} (ctx-hdfs cat "myfile.txt") {% endhighlight %}</td>
</tr>
<tr>
<td>eachLine(clusterName:String = null, path:String) </td>
<td> Recursively searches for non hidden files in a directory and return each line, if the file is compressed it will be decompressed first, each line is sent to the closure </td>
<td>{% highlight clojure %}(ctx-hfds eachLine "myfile.txt"  (fn [line] -> (prn line))){% endhighlight %}</td>
</tr>
<tr>
<td>seq_eachLine(clusterName:String = null, path:String):Collection</td>
<td>Same as eachLine but returns a lazy sequence</td>
<td>{% highlight clojure %}(def lines (map str (ctx-hdfs seq_eachLine "myfile.txt"))){% endhighlight %}</td>
</tr>
<tr>
<td>withDecompressedInputStream(clusterName:String = null, file:String) </td>
<td> Reads the file and if compressed, use the Hadoop configured Codec to decompress, if the file is compressed a CompressionInputStream is passed to the Closure</td>
<td>{% highlight clojure %} (ctx-hdfs withDecompressInputStream "myfile.gz" (fn [input]   )){% endhighlight %}</td>
</tr>
<tr>
<td>list(cluserName:String = null, path:String ) </td>
<td> Recursively lists all non hidden files </td>
<td>{% highlight clojure %}(ctx-hdfs list "mydir" (fn [file] (prn file ))){% endhighlight %}</td>
</tr>
<tr>
<td>seq_list(clusterName:String = null, path:String):Collection</td>
<td>Returns a ist of files, the method iterates recursively through all directories</td>
<td>{% highlight clojure %}(def files (ctx-hdfs seq_list "mydir")){% endhighlight %}</td>
</tr>
<tr>
<td>put(clusterName:String = null, src:String, dest:String) </td>
<td> Loads a local file to HDFS </td>
<td>{% highlight clojure %}(ctx-hdfs put "myfile.txt"  "/hdfsdir/"){% endhighlight %}</td>
</tr>
<tr>
<td>get(clusterName:String = null, hdfsSrc:String, localDest:String) </td>
<td> Download a file from HDFS to local </td>
<td>{% highlight clojure %}(ctx-hdfs get "/hdfsdir/myfile.txt" "localfile.txt"){% endhighlight %}</td>
</tr>
<tr>
<td>delete(clusterName:String = null, file:String, recursive:boolean)</td>
<td>Delete a file or directory (if rescursive is true)  </td>
<td></td>
<td></td>
</tr>
<tr>
<td>exist(clusterName:String = null, file:String):boolean </td>
<td> Returns true if a file or directory exist </td>
<td>{% highlight clojure %}(def exit (ctx-hdfs exist "mydir")){% endhighlight %}</td>
</tr>
<tr>
<td>mkdirs(clusterName:String = null, file:String)</td>
<td> Make all parent and sub-directories in the path </td>
<td>{% highlight clojure %}(ctx-hdfs mkdirs "/path1/path2/path2"){% endhighlight %}</td>
</tr>
<tr>
<td>isDirectory(clusterName:String = null, file:String):boolean </td>
<td> Returns true if a directory  </td>
<td></td>
</tr>
<tr>
<td>isFile(clusterName:String = null, file:String):boolean </td>
<td> Returns true if a file </td>
<td></td>
</tr>
<tr>
<td>move(clusterName:String = null, src:String, dest:String) </td>
<td> Move a file from one HDFS location to another HDFS location </td>
<td>{% highlight clojure %}(ctx-hdfs put "/hdfsdir1/myfile1.txt" "/hdfsdir2/"){% endhighlight %}</td>
</tr>
<tr>
<td>open(clusterName:String = null, file:String):FSDataInputStream</td>
<td> Open a InputStream for reading data to an HDFS File   </td>
<td>(def input (ctx-hdfs open "myfile"))</td>
</tr>
<tr>
<td>open(clusterName:String = null, file:String, closure)</td>
<td> Open a InputStream for reading data to an HDFS File and send it to the closure </td>
<td>  </td>
</tr>
<tr>
<td>create(clusterName:String = null, file:String):FSDataOutputStream </td>
<td> Create a new file on HDFS </td>
<td></td>
</tr>
<tr>
<td>unTar(localFile:String, localDir:String) </td>
<td> Extracts the contents of a TAR file </td>
<td>{% highlight clojure %}(ctx-hdfs unTar "myfile.tar" (str "/opt/glue/log/" (.getUnitId ctx) "/tardir")){% endhighlight %}</td>
</tr>
<tr>
<td>unZip(localFile:String zipDir:String) </td>
<td> Unzips a ZIP/GZIP file </td>
<td>{% highlight clojure %}(ctx-hdfs unZip "myfile.gzip" (str "/opt/glue/log/" (.getUnitId ctx) "/zipdir")){% endhighlight %}</td>
</tr>
<tr>
<td>getFileStatus(clusterName:String = null, file:String):<a href="http://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/clas-use/FileStatus.html">FileStatus</a> </td>
<td>  Returns the HDFS File Status </td>
<td></td>
</tr>
<tr>
<td>setTimes(clusterName:String = null, file:String, mtime:long, atime:long) </td>
<td> calls the FileSystem.setTimes method on a file</td>
<td></td>
</tr>
<tr>
<td>setOwner(clusterName:String = null, file:String, username:String, groupname:String) </td>
<td> calls the FileSystem.setOwner on a file </td>
<td></td>
</tr>
<tr>
<td>setPermissions(clusterName:String = null, file:String, unixStylePermissions:String) </td>
<td> calls the FileSystem.setPermission on a file  </td>
<td></td>
</tr>
<tr>
<td>getContentSummary(clusterName:String = null, file:String)<a href="http://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/ContentSummary.html">ContentSummary</a> </td>
<td> Returns the content summary of a file or directory </td>
<td></td>
</tr>
<tr>
<td>getDefaultBlockSize(clusterName:String = null):long </td>
<td> Returns the default block size</td>
<td></td>
</tr>
<tr>
<td>getDefaultReplication(clusterName:String = null):short </td>
<td> Returns the default replication</td>
<td></td>
</tr>
<tr>
<td>timeSeries(n:String,tableHDFSDir:String,nowdate:Date, modifyTime:String, partitionFormatter:Closure<String>, dateIncrement:Closure<Date>, collector:Collector = null)</td>
<td> Iterates from nowdate-n (e.g. nowdate - 2.hours) on a date increment over a series of date partitioned HDFS files and return true if all of the partitions have files and have not been modified since a specified amount of time  </td>
<td></td>
</tr>
<tr>
<td>downloadChunked(clusterName:String=null, hdfsDir:Collection<String>, localDir:String, chunkSize:int=1073741824, compression:String = "gz", callBack:Closure) </td>
<td> Performs a eachLine on each of the direcotries in hdfsDir, writing the data (compressed) to a local part file, when the chunk size is reached the file is rolled and its file name send to the callBack closure. </td>
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
<td>cat(clusterName:String = null, path:String):String </td>
<td> Returns the text to a path </td>
<td>{% highlight python %} print(str(ctx.hdfs().cat('myfile.txt'))) {% endhighlight %}</td>
</tr>
<tr>
<td>eachLine(clusterName:String = null, path:String) </td>
<td> Recursively searches for non hidden files in a directory and return each line, if the file is compressed it will be decompressed first, each line is sent to the closure </td>
<td>{% highlight python %}
def lineHandler(line):
    print(str(line))


ctx.hfds().eachLine("myfile.txt", lineHandler)
{% endhighlight %}</td>
</tr>
<tr>
<td>seq_eachLine(clusterName:String = null, path:String)</td>
<td>Same as eachLine but returns a lazy sequence of lines</td>
<td>
{% highlight python %}
for line in ctx.hdfs().eachLine("myfile.txt"):
    print(str(line))

{% endhighlight %}     
</tr>
<tr>
<td>withDecompressedInputStream(clusterName:String = null, file:String) </td>
<td> Reads the file and if compressed, use the Hadoop configured Codec to decompress, if the file is compressed a CompressionInputStream is passed to the Closure</td>
<td>
{% highlight python %} 
def compRead(input):
    #handle the input stream


ctx.hdfs().withDecompressInputStream("myfile.gz", compRead)
{% endhighlight %}</td>
</tr>
<tr>
<td>list(cluserName:String = null, path:String ) </td>
<td> Recursively lists all non hidden files </td>
<td>
{% highlight python %}
def prnFile(file):
    print(str(file))

ctx.hdfs().list("mydir", prnFile)
{% endhighlight %}</td>
</tr>
<td>seq_list(cluserName:String = null, path:String ):Collection </td>
<td> Recursively lists all non hidden files and returns a collection </td>
<td>
{% highlight python %}

for(file in ctx.hdfs().seq_list("mydir", prnFile)):
   print(str(file))

{% endhighlight %}</td>
</tr>

<tr>
<td>put(clusterName:String = null, src:String, dest:String) </td>
<td> Loads a local file to HDFS </td>
<td>{% highlight python %}ctx.hdfs().put('myfile.txt', '/hdfsdir/'){% endhighlight %}</td>
</tr>
<tr>
<td>get(clusterName:String = null, hdfsSrc:String, localDest:String) </td>
<td> Download a file from HDFS to local </td>
<td>{% highlight python %}ctx.hdfs().get('/hdfsdir/myfile.txt', 'localfile.txt'){% endhighlight %}</td>
</tr>
<tr>
<td>delete(clusterName:String = null, file:String, recursive:boolean)</td>
<td>Delete a file or directory (if rescursive is true)  </td>
<td></td>
<td></td>
</tr>
<tr>
<td>exist(clusterName:String = null, file:String):boolean </td>
<td> Returns true if a file or directory exist </td>
<td>{% highlight python %}ctx.hdfs().exist('mydir'){% endhighlight %}</td>
</tr>
<tr>
<td>mkdirs(clusterName:String = null, file:String)</td>
<td> Make all parent and sub-directories in the path </td>
<td>{% highlight python %}ctx.hdfs().mkdirs('/path1/path2/path2'){% endhighlight %}</td>
</tr>
<tr>
<td>isDirectory(clusterName:String = null, file:String):boolean </td>
<td> Returns true if a directory  </td>
<td></td>
</tr>
<tr>
<td>isFile(clusterName:String = null, file:String):boolean </td>
<td> Returns true if a file </td>
<td></td>
</tr>
<tr>
<td>move(clusterName:String = null, src:String, dest:String) </td>
<td> Move a file from one HDFS location to another HDFS location </td>
<td>{% highlight python %}ctx.hdfs().put('/hdfsdir1/myfile1.txt', '/hdfsdir2/'){% endhighlight %}</td>
</tr>
<tr>
<td>open(clusterName:String = null, file:String):FSDataInputStream</td>
<td> Open a InputStream for reading data to an HDFS File   </td>
<td></td>
</tr>
<tr>
<td>open(clusterName:String = null, file:String, closure)</td>
<td> Open a InputStream for reading data to an HDFS File and send it to the closure </td>
<td>  </td>
</tr>
<tr>
<td>create(clusterName:String = null, file:String):FSDataOutputStream </td>
<td> Create a new file on HDFS </td>
<td></td>
</tr>
<tr>
<td>unTar(localFile:String, localDir:String) </td>
<td> Extracts the contents of a TAR file </td>
<td>{% highlight python %}ctx.hdfs().unTar('myfile.tar', '/opt/glue/log/$' + str(ctx.getUnitId()) + '/tardir'){% endhighlight %}</td>
</tr>
<tr>
<td>unZip(localFile:String zipDir:String) </td>
<td> Unzips a ZIP/GZIP file </td>
<td>{% highlight python %}ctx.hdfs().unZip('myfile.gzip', '/opt/glue/log/' + str(ctx.getUnitId()) + '/zipdir'){% endhighlight %}</td>
</tr>
<tr>
<td>getFileStatus(clusterName:String = null, file:String):<a href="http://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/clas-use/FileStatus.html">FileStatus</a> </td>
<td>  Returns the HDFS File Status </td>
<td></td>
</tr>
<tr>
<td>setTimes(clusterName:String = null, file:String, mtime:long, atime:long) </td>
<td> calls the FileSystem.setTimes method on a file</td>
<td></td>
</tr>
<tr>
<td>setOwner(clusterName:String = null, file:String, username:String, groupname:String) </td>
<td> calls the FileSystem.setOwner on a file </td>
<td></td>
</tr>
<tr>
<td>setPermissions(clusterName:String = null, file:String, unixStylePermissions:String) </td>
<td> calls the FileSystem.setPermission on a file  </td>
<td></td>
</tr>
<tr>
<td>getContentSummary(clusterName:String = null, file:String)<a href="http://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/ContentSummary.html">ContentSummary</a> </td>
<td> Returns the content summary of a file or directory </td>
<td></td>
</tr>
<tr>
<td>getDefaultBlockSize(clusterName:String = null):long </td>
<td> Returns the default block size</td>
<td></td>
</tr>
<tr>
<td>getDefaultReplication(clusterName:String = null):short </td>
<td> Returns the default replication</td>
<td></td>
</tr>
<tr>
<td>timeSeries(n:String,tableHDFSDir:String,nowdate:Date, modifyTime:String, partitionFormatter:Closure<String>, dateIncrement:Closure<Date>, collector:Collector = null)</td>
<td> Iterates from nowdate-n (e.g. nowdate - 2.hours) on a date increment over a series of date partitioned HDFS files and return true if all of the partitions have files and have not been modified since a specified amount of time  </td>
<td></td>
</tr>
<tr>
<td>downloadChunked(clusterName:String=null, hdfsDir:Collection<String>, localDir:String, chunkSize:int=1073741824, compression:String = "gz", callBack:Closure) </td>
<td> Performs a eachLine on each of the direcotries in hdfsDir, writing the data (compressed) to a local part file, when the chunk size is reached the file is rolled and its file name send to the callBack closure. </td>
<td></td>
</tr>
</tbody>
</table>
  </div>
</div>

  
