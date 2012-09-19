name="simpleflow2"
	
	process1{
		tasks = { context ->
			println "one"
		}
		
		success = { context ->
			println "one success"
		}
		
	}
	
	process2{
		dependencies="process1"
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
