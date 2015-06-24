---
layout: doc
title: Glue Triggers
permalink: triggers.html
category: tutorial
---


{% include nav.markdown %}

#Overview

Two types of triggers are supported:

* HDFS Directory Polling (file or directory only based)
* Cron style time execution

For cron expression syntax see [quartz-scheduler](http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger)


Triggers require three tables in mysql to exist:

 	describe unittriggers;
	+---------+--------------+------+-----+---------+----------------+
	| Field   | Type         | Null | Key | Default | Extra          |
	+---------+--------------+------+-----+---------+----------------+
	| id      | int(11)      | NO   | PRI | NULL    | auto_increment | 
	| unit    | varchar(100) | YES  |     | NULL    |                | 
	| type    | varchar(10)  | YES  |     | NULL    |                | 
	| data    | varchar(100) | YES  |     | NULL    |                | 
	| lastrun | date         | YES  |     | NULL    |                | 
	+---------+--------------+------+-----+---------+----------------+
 	
 	describe hdfsfiles;
	+-------+---------------+------+-----+---------+----------------+
	| Field | Type          | Null | Key | Default | Extra          |
	+-------+---------------+------+-----+---------+----------------+
	| id    | int(11)       | NO   | PRI | NULL    | auto_increment | 
	| path  | varchar(1000) | NO   | UNI | NULL    |                |
	| ts    | bigint(20)    | NO   | MUL | 0       | 
	| seen  | tinyint(4)    | YES  | MUL | 0       |                | 
	+-------+---------------+------+-----+---------+----------------+
 
 	describe unitfiles;
	+--------+-------------+------+-----+---------+-------+
	| Field  | Type        | Null | Key | Default | Extra |
	+--------+-------------+------+-----+---------+-------+
	| unitid | int(11)     | YES  | MUL | NULL    |       | 
	| fileid | int(11)     | YES  |     | NULL    |       | 
	| status | varchar(10) | YES  |     | NULL    |       | 
	+--------+-------------+------+-----+---------+-------+

The tables unitfiles and hdfsfiles maintain the hdfs polling state, and the unittriggers table contain the different triggers for each workflow.

E.g. below is an example of some entries:

	+----+---------+----------+---------------+---------+
	| id | unit    | type     | data          | lastrun |
	+----+---------+----------+---------------+---------+
	|  1 | test    | hdfs     | /logs/test    | NULL    | 
	|  2 | test1   | hdfs     | /logs/a       | NULL    | 
	|  3 | test1   | hdfs     | /logs/b       | NULL    | 
	|  4 | test1   | hdfs-dir | /logs/c       | NULL    | 
	|  5 | mytest2 | cron     | 0 0/5 * * * ? | NULL    | 
	+----+---------+----------+---------------+---------+

Note: Using hdfs-dir instead of hdfs will only check directories for modification.

I.e. if you only need to see when directory partitions have changed rather than individual files use the type 'hdfs-dir'

## Different Paths Same Workflow
Different hdfs paths can be defined for the same workflow. A unique id is assigned for each unit_name, data combination, and its this id that is used in the unitfiles table that matches a file path fileid in hdfsfiles to a units execution.

### Important

The folder entries in the data column should not have overlapping files, other wise the workflow would receive the file twice in the ready files, because two distinct entries will be made in the unitfiles table.
Wrong way

For unit myunit

data = /log/a

data = /log/a

Correct way
For unit myunit

data = /log/a

data = /log/b

# HDFS Directory Polling

## Module Configuration

The module is configured where all glue modules are configured in the file: /opt/glue/conf/modules.groovy

### Properties

Properties are:

	name	 description
	connection.username	 database user name
	connection.password	 database password
	connection.driver	 jdbc driver class
	connection.url	 jdbc connection url


## Example Config

	triggerStore2{

	  className="org.glue.trigger.service.hdfs.TriggerStore2Module"
	  isSingleton="true"
	  config{
	  triggerStore{
	   className='org.glue.trigger.persist.db.DBTriggerStore2'
     	config{
	       connection.username="glue"
	       connection.password="glue"
	       connection.driver="com.mysql.jdbc.Driver"
	       connection.url="jdbc:mysql://localhost:3306/glue"
	     }
	   }
	 }
	}



#Work flow Usage
##Mark files as processed

	def fileIds = []

	context.triggerStore2.markFilesAsProcessed( fileIds )

##List ready files

	context.triggerStore2.listReadyFiles { int fileId, String path ->
 	//do something

	}
  
       def ready_files = context.triggerStore2.listReadyFiles()
       //do something with th ready files each item is a vector of [id path] e.g [[1 "a/b"] [2 "c/d"]]	
	
## Get Updated Hive Style Date Partitions

	tasks{
	
	
	  prepareFiles{
	
	    tasks = { ctx ->
	
	      //regex to extract values out of the partition path 
	      def DATE = /hr=(\d\d\d\d\d\d\d)/
	
	      def dateSet as HashSet
	
	      def fileIds = []
	      //here we iterate over all of the files that's been seen newly for this workflow
	      //in the directories its subsribed to in the glue unittriggers table
	      ctx.triggerStore2.listReadyFiles { int fileId, String path ->
	        fileIds << fileId
	
	        //extract year, month, day
	        def m = path =~ DATE
	        if(m.size() < 1 || m[0].size() < 2) return //skip if none found
		
			if(m[0][1])
	           dateSet << m[0][1] //now we should have something like yyyyMMdd
	
	      }
	
	
	     ctx.fileIds = fileIds
	     ctx.dateSet = dateSet
	
	    }
	
	  }
	
	  exec{
	    dependencies = "prepareFiles"
	
	    tasks = { ctx ->
	
	       //here we have a list of dates that have been updated
	       def dateSet = ctx.dateSet
	
	
	    }
	
	  }
	  cleanup{
	    dependencies = "exec"
	
	    tasks = { ctx ->
	
	      //only a workflow's logic can know when its completed processing a file
	      //this method marks the file as processed, this file will not appear again
	      //in the listReadyFiles method
	      if(ctx.fileIds) ctx.triggerStore2.markFilesAsProcessed( ctx.fileIds )
	
	    }
	
	  }
	
	}


# Update triggers manually for reprocessing

In mysql do


     update unitfiles dest, (select unitid,fileid,status from hdfsfiles, unitfiles where unitid in ($list-of-unitids-from-unittriggers) and fileid=id and path like '%year=$yearPartition/month=$month/day=$day%') src set dest.status="ready" where dest.status="processed" and dest.unitid=src.unitid and dest.fileid=src.fileid ;



	
	
