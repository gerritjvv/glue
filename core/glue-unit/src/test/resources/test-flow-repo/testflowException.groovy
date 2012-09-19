name="test"
tasks{
	process1{
		tasks = { context -> println "one" }

		success = { context -> println "one success" }
	}

	process2{
		dependencies="process1"
		tasks = { context ->
			throw new RuntimeException("InducedError")
			println "two"
		}
	}
}