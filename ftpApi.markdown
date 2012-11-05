---
layout: doc
title: FTP Module API
permalink: ftpApi.html
category: tutorial
---


{% include nav.markdown %}


FTP and SFTP Module that use the [Apache Commons Virtual File System](http://commons.apache.org/vfs) project to access SFTP and FTP sites 


Class: [FTPModule](https://github.com/gerritjvv/glue/blob/master/core/glue-modules/src/main/groovy/org/glue/modules/FTPModule.groovy)



 Method | Description | Example 
 ------ | ----------- | ------- 
exists(server:String=null, pathName:String):boolean | true if file exist | ctx.ftp.exist('myfile') 
mkdir(server:String=null,pathName:String):boolean | create a directory, return true if succeeded | ctx.ftp.mkdir('mydir')
rmdir(server:String=null,pathName:String):boolean | remove a directory | ctx.rmdir('mydir')
put(server:String=null, pathName:String, input:InputStream):boolean | reads data from the java.io.InputStream and puts it into a ftp file | ctx.ftp.put('remotefile.txt', new FileInputStream(localFile))
put(server:String=null, pathName:String, remoteFile:String):boolean | copy the localPath file to the ftp server | ctx.ftp.put('localfile.txt', 'removefilename')
get(server:String=null, pathName:String, localPath:String):boolean | copy the file from the ftp site into a local file name | ctx.get('removefile.txt', 'localfile.txt')
withInputStream(server:String=null, pathName:String, closure(i:InputStream) | opens a java.io.InputStream from the ftp pathname and pass as an argument to the closure | ctx.ftp.withInputStream('removeFile.txt', { i -> readEncoded(new Base64InputStream(i, true, -1, null))  } )
rename(server:String=null, from:String, to:String):boolean | rename a file on the remote ftp server 
withWriter(server:String=null, pathName:String, closure(writer:Writer) | create the file on the remote ftp server, opens a java.io.Writer and passes the writer to the closure | ctx.ftp.withWriter('mynewfile.txt', { w -> w << "Hi This text is written to the file mynewfile.txt on the ftp site' } )
withOutputStream(server:String=null, path:String, closure) | create the file on the remote ftp server, opens a java.io.OutputStream and passes the writer to the closure | ctx.ftp.withWriter('mynewfile.txt', { o -> o << new FileInputStream('myfile.txt') } )
isFile(server:String=null, pathName:String):boolean | true if the pathName points to a file 
ls(server:String=null, pathName:String='/'):String[] | List the files and directories contained in pathName on the ftp server | ftp.ls('server1', 'mydir')?.each { f -> println f } 
getParent(server:String=null, pathName:String):String | Returns the parent directories of the pathName
 
 