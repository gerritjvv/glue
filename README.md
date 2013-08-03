Glue
====

BigData Workflow Engine for Hadoop, Hbase, Netezza, Pig, Hive ...


Introduction
------------



For a detailed introduction see http://gerritjvv.github.com/glue


Glue is a workflow engine for bigdata supporting multiple languages such as:

* Groovy
* Clojure
* Jython
* JRuby


Examples
-------

```clojure

(def lines (ctx-hdfs eachLine "/myhdfsfile"))

```

```python

def doLine(line):
    print(str(line))

ctx.hdfs().eachLine(doLine)

```


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
