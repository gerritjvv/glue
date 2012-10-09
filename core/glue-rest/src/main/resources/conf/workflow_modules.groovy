/**
 * All modules in this file are used only withing the workflow job.
 * These are accessed via the GlueContext
 */
/*
* ----- Standard log outpu module
*/


/*
* --------- Mail module ------
mail{
				   className='org.glue.modules.MailModule'
				   isSingleton=false
				   config{
					   recipientList='test@specificmedia.com'
					   from_email ='Glue <test@specificmedia.com>'
					   smtp_host = '<put the smtp server address here>'
					   debug=true
					   uiUrl="http://<glue server address>/glue"
					   getUnitUrl= { uiUrl, unitId ->
							return "$uiUrl/index.php?unitId=$unitId";
					   }
					   getProcessUrl ={ uiUrl, unitId, processName ->
							return "$uiUrl/index.php?unitId=$unitId&processName=$processName";
					  }
				   }
			   
}

*/

/*
* ------ HDFSModule provides decent hdfs integration and alot of groovy style helper functions.
* ------ e.g. eachLine through hdfs files, list recursivelly etc.
* The clusters section contains different cluster configurations that allow pig to submit jobs to different hadoop clusters
* the properties file should be equal to the $PIG_HOME/conf/pig.properties for each cluster, you can also point this to the pig.properties
*
hdfs{
   className='org.glue.modules.hadoop.impl.HDFSModuleImpl'
   //must never be a singleton
   isSingleton=false
   config{
	   clusters{
		   type1hadoopcluster{
			   isDefault=true
			   hdfsProperties="/opt/glue/conf/type1.properties"
		   }
		   type2hadoopcluster{
			   hdfsProperties="/opt/glue/conf/type2.properties"
		   }
	   }
   }
}
*/

/*
* ------- Pig module ----
* Uncomment to use as context.modules.pig
* The clusters section contains different cluster configurations that allow pig to submit jobs to different hadoop clusters
* the properties file should be equal to the $PIG_HOME/conf/pig.properties for each cluster, you can also point this to the pig.properties
* if pig is installed on the local machine
* The isDefault sets a cluster config as default for glue workflows that do not specify the configuration in its modules section
*
* The pig module has some basic database functionality and the db section can be used to define different databases that can be
* referred to my name e.g. "mydb" from the glue workflows
pig{
   className='org.glue.modules.hadoop.PigModule'
   isSingleton=false
   config{
       jars = []
       //classpath = ['/usr/lib/pig', '/usr/lib/pig/lib', '/opt/glue/lib/', '/opt/glue/conf', '/usr/lib/hadoop', '/usr/lib/hadoop/lib']
       
	   clusters{
		   type1hadoopcluster{
			   isDefault=true
			   pigProperties="/opt/glue/conf/type1.properties"
		   }
		   type2hadoopcluster{
			   pigProperties="/opt/glue/conf/type2.properties"
		   }
	   }
	   
	   db{
		   mydb{
			   host="jdbc:mysql://localhost:3306/"
			   user="readonly"
			   pass="radonly"
			   driver="com.mysql.jdbc.Driver"
		   }
	   }
	   
	   //constants can be accessed from context.modules.pig.constants
	   constants{
			myconstant="test"		
	   }
	}  
	   
   }
}
*/
/*
* ----- DBStore to add persistent tracking of glue unit execution
* ----- this should be enabled as part of a good installation and can be used by a UI to track job execution.
dbstore{
   className='org.glue.modules.DbStoreModule'
   isSingleton=false
   config{
	   host="jdbc:mysql://127.0.0.1:3306/glue"
	   user="root"
	   pass="root"
	   driver="com.mysql.jdbc.Driver"
   }
}
*/

