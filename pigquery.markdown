---
layout: doc
title: Pig Query
permalink: pigquery.html
category: tutorial
---


{% include nav.markdown %}

# Procedures

Load temp file to HDFS, show write temp, put to hadoop
Query
Insert Results into a DB

# Create database table
 
 We'll assume that the table is created in the database glue, and that this has been configured in /opt/glue/conf/workflow_modules.groovy
 
 
 	create table example (category varchar(255) PRIMARY KEY, hits INT);
    
    
	
# Workflow

	
	tasks{
	
	
	  loadFile{
	
	    tasks = { context ->
	
	       def fileName= "/opt/glue/log/${context.unitId}/myfile.csv"
	       def categories = ['a', 'b', 'c', 'd']
	       def r = new Random()
	
	       new File(fileName).withWriter { w ->  (1..1000).each { w << "${categories[r.nextInt(4)]},${r.nextInt(100)}\n" } }
	
	
	       def hdfsDir = "/queries/gluetest/data"
	
	
	       def hdfsFileName = "$hdfsDir/myfile.csv"
	       if( context.hdfs.exist(hdfsDir) )
	           context.hdfs.delete(hdfsDir, true)
	
	       context.hdfs.mkdirs(hdfsDir)
	       context.hdfs.put(fileName, hdfsDir)
			
	       context.paths = [ hdfsFileName ]
	       new File(fileName).delete()
	    }
	
	  }
	
	
	  query{
	     dependencies = "loadFile"
	     tasks = { context ->
	
	        def resultPath = '/queries/gluetest/resp'
	        println "HDFS Path: ${context.paths}"
	        context.pig.run("glue test", """
	
	            ads = load '${context.paths.join(',')}'  using PigStorage(',') as (c:chararray, n:int);
	            g = group ads by c;
	            r = foreach g generate FLATTEN(group), COUNT(\$1);
	            rmf /queries/gluetest/resp;
	            store r into '${resultPath}';
	
	        """, [:])
	
	        context.resultPath = resultPath
	
	     }
	
	  }
	
	  insertToDB{
	     dependencies = "query"
	
	     tasks = { context ->
	
	        if(!context.resultPath) return //exit if null path
	
	        //There are many ways to write into a DB
	        //If the data set is small the most convinient way is to do an INSERT UPDATE ON KEY FOUND if MySQL is used
	        //one optimization is to use a multi value insert statement
	        def sql = "INSERT INTO example (category, hits) VALUES "
	        def i = 0
	        context.hdfs.eachLine context.resultPath, { line ->
	                def (category, hits ) = line.split() //split on tab
	                if(i++ != 0) sql += ","
	
	                sql += "('$category', $hits)"
	        }
	
	        sql += "ON DUPLICATE KEY UPDATE hits=VALUES(hits)" //we update all duplicates with the new value
	        context.sql.execSql('glue', sql);
	
	     }

  		}
	
	
	}
	
			
	