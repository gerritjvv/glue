Glue
====

BigData Workflow Engine for Hadoop, Hbase, Netezza, Pig, Hive ...


Introduction
------------

* Glue is a job execution engine.
* Glue task unit is similar to ant except
    * Written as groovy configuration except xml 
    * Contains closures with real groovy code that should be executed
    * Tasks are executed simultaneously (if they do not depend on each other).
* Glue task units also can be supplied with parameters.
* REST interface is used to submit and monitor each unit and task.


Web Site
--------

See http://gerritjvv.github.com/glue


Example
-------

```groovy
name = "test"
tasks{
        process1{
                tasks = { context ->
                        println "one"
                }
                
                success = { context ->
                        println "one success"
                }
                
        }
        
        process2{
                dependencies = "process1"
                
                tasks = { context ->
                        println "two"
                }
                
                error = { context ->
                        println "two error"
                }
                
                success = { context ->
                        println "two success"
                }
        }
}
```
