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

(def rs (glue/ctx-sql eachSqlResult "glue" "select * from unitfiles"))

(take 10 rs)

```

```python

def doLine(line):
    print(str(line))

ctx.hdfs().eachLine(doLine)

s = ctx.triggerStore2().listReadyFiles(ctx)
s[0]


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
