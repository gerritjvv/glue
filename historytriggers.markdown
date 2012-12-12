---
layout: doc
title: Glue History Triggers
permalink: historytriggers.html
category: tutorial
---


{% include nav.markdown %}

#Overview


History Triggers are triggers that are invoked when the difference between file's creation time stamp and the current time is >= N (seconds, minutes, hours, days, weeks, months or years).

Normally history triggers are for cleaning up data or invoking data older than a day at least, the default refresh period for history triggers is 24 hours.

To change set the property
	history.refresh.freq

#Time Units

	* Second
	* Minute
	* Hour
	* Day
	* Week
	* Month
	* Year

#Type

The type value is hdfs-history

#Data Expression Format

 
The data format for a history trigger is:
	<hdfs>,<Integer>[. ]<second|minute|hour|day|week|month|year>
	
E.g. to run a workflow for files older than 1 week write
    /myhdfsdir/,1.week
    
 
 


Triggers require three tables in mysql to exist:

 	describe unittriggers;
	+---------+--------------+------+-----+---------+----------------+
	| Field   | Type         | Null | Key | Default | Extra          |
	+---------+--------------+------+-----+---------+----------------+
	| id      | int(11)      | NO   | PRI | NULL    | auto_increment | 
	| unit    | varchar(100) | YES  |     | NULL    |                | 
	| type    | varchar(20)  | YES  |     | NULL    |                | 
	| data    | varchar(100) | YES  |     | NULL    |                | 
	| lastrun | date         | YES  |     | NULL    |                | 
	+---------+--------------+------+-----+---------+----------------+
 	
 	describe hdfsfiles_history;
	+----------+---------------+------+-----+---------+----------------+
	| Field    | Type          | Null | Key | Default | Extra          |
	+----------+---------------+------+-----+---------+----------------+
	| id       | int(11)       | NO   | PRI | NULL    | auto_increment | 
	| path     | varchar(1000) | NO   | UNI | NULL    |                | 
	| datetime | datetime      | YES  |     | NULL    |                | 
	| seen     | tinyint(4)    | YES  | MUL | 0       |                | 
	+----------+---------------+------+-----+---------+----------------+

 
 	describe unitfiles;
	+--------+-------------+------+-----+---------+-------+
	| Field  | Type        | Null | Key | Default | Extra |
	+--------+-------------+------+-----+---------+-------+
	| unitid | int(11)     | YES  | MUL | NULL    |       | 
	| fileid | int(11)     | YES  |     | NULL    |       | 
	| status | varchar(10) | YES  |     | NULL    |       | 
	+--------+-------------+------+-----+---------+-------+

The tables unitfiles and hdfsfiles maintain the hdfs polling state, and the unittriggers table contain the different triggers for each workflow.

E.g. blow is an example of some entries:

	+----+--------------------+--------------+--------------------------------+---------+
	| id | unit               | type         | data                           | lastrun |
	+----+--------------------+--------------+--------------------------------+---------+
	|  1 | historytest.groovy | hdfs-history | /log/raw/gpb-impressions,1.day | NULL    | 
	+----+--------------------+--------------+--------------------------------+---------+

	
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

	historyTriggerStore{

	  className="org.glue.trigger.service.hdfs.TriggerStore2Module"
	  isSingleton="true"
	  config{
	  triggerStore{
	   className='org.glue.trigger.persist.db.HistoryDBTriggerStore'
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

	context.historyTriggerStore.markFilesAsProcessed( fileIds )

##List ready files

	context.historyTriggerStore.listReadyFiles { date, int fileId, String path ->
 	//do something

	}
	
	
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
	      ctx.historyTriggerStore.listReadyFiles { date, int fileId, String path ->
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
	      if(ctx.fileIds) ctx.historyTriggerStore.markFilesAsProcessed( ctx.fileIds )
	
	    }
	
	  }
	
	}



	
	
