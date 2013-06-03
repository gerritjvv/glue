---
layout: doc
title: AWS S3 API
permalink: s3Api.html
category: tutorial
---


{% include nav.markdown %}

S3 Module allows easy access (internally via the AWS SDK) to the S3 services.
This module can be configured to access multiple servers and buckets.


Class: [S3Module](https://github.com/gerritjvv/glue/blob/master/core/glue-modules/src/main/groovy/org/glue/modules/S3Module.groovy)

 Method | Description | Example |
 ------ | ----------- | ------- |
putFile(server:String=null, bucket:String=null, file:String, dest:String):PutObjectResult | Copy the file to the destination key on S3 using the default bucket | ctx.s3.putFile("myfile", "/dir/myfile.txt")
getFile(server:String=null,bucket:String=null, file:String, localFile:String) | Copy the file from S3 to the local file | ctx.s3.getFile("/dir/myfile.txt", "myfile")
deleteFile(server:String=null,bucket:String=null,fileString) | Delete the file on S3 | ctx.s3.deleteFile("/dir/myfile.txt")
putFile(input:InputStream,metadata:ObjectMetaData, dest:String):PugObjectResult | Reads from the java.io.InputStream and writes the content to the dest file |
createBucket(server:String=null, bucket:String) | Create a bucket on S3 | ctx.s3.createBucket("mynewbucket")
deleteBucket(server:String=null, bucket:String) | Delete a bucket from S3 | ctx.s3.deleteBucket("mynewbucket")
listFiles(server:String=null, bucket:String=null, dir:String):List<String> | Returns a list of files | ctx.s3.listFiles("/mydir")

  
