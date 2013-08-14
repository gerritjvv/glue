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
* Ruby
* Scala

Example
-------

```clojure
;clojure

(def lines (ctx-hdfs eachLine "/myhdfsfile"))

```


```python

#jython

def lineHandler(line):
    print(str(line))


ctx.hfds().eachLine("myhdfsfile", lineHandler)


```

```scala

//scala

for(line <- ctx("hdfs").eachLine("/myhdfsfile"))
 println(line)


```

```groovy
//groovy
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
