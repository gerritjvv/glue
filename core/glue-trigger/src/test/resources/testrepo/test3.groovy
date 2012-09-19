name="test3"
triggers="hdfs:test2:mypath/*"
tasks{

	myprocess{
		tasks={
			println "hi"
		}
	}

		
}
