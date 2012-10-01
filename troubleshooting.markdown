---
layout: doc
title: Trouble Shoot
permalink: troubleshooting.html
category: tutorial
---


{% include nav.markdown %}

# NoClassDefFoundError on org/apache/hadoop/thirdparty/guava/common/collect/LinkedListMultimap

See

http://blog.timmattison.com/archives/2012/02/07/tip-fix-noclassdeffounderror-on-orgapachehadoopthirdpartyguavacommoncollectlinkedlistmultimap/

and configure the /opt/glue/conf/exec.groovy to load each workflow correct classpath

e.g. 
	processClassPath = ['/opt/glue/lib/', '/usr/lib/hadoop/lib/', '/usr/lib/hadoop']
	
Restart the Glue Server.


# Strange file does not exist HDFS errors

Current version of Glue run by default as root and as a result all workflows run as root by default as well.
Ensure that the directories you are writing to have write + read permissions for root

# File Does not exist when using '/tmp/'

Avoid using the /tmp/ directory in HDFS, the local hadoop client and its APIs get confused between the local disk /tmp/ folder (which has special meaning)
and the HDFS /tmp/ folder which is just another folder and has no special meaning. 

# Pig Query Not Working

Glue prints out the final pig query as is to the STDOUT.

Lets you your workflow run id is a61c2919-ddde-4881-b843-59111abb9dbf, and your pig script is called in the process query,
then have a look at the output of: /opt/glue/log/a61c2919-ddde-4881-b843-59111abb9dbf/query

e.g.
	Running query
	SET job.name 'glue test';

            ads = load '/queries/gluetest/data/myfile.csv' as (c:chararray, n:int);
            g = group ads by c;
            r = foreach g generate FLATTEN(group), COUNT($1);
            rmf /queries/gluetest/resp;
            store r into '/queries/gluetest/resp';

You can use this output to manually test your query sytanx in the pig console, then paste in back into the workflow.

	


