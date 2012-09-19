name="test1"
triggers="hdfs:mypath/*"
tasks{

	myprocess{
		tasks={
			println "hi"
		}
	}

		
}
