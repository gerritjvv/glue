name="test"
tasks{
	process1{
		tasks = { context ->
			println "one, sleeping 2 secs"
			context.val = "greetings from process1, args is ${context.args.it}, unitId is ${context.unitId}"
			Thread.sleep 2000
		}

		success = { context -> println "one finished" }
	}

	process2{
		dependencies="process1,process3"
		tasks = { context -> println "two" }

		error = { context, t ->
			println "two error"
			t.printStackTrace()
		}

		success = { context -> println "two success" }
	}

	process3{
		tasks= { context ->
			println "This process3 gonna run for 7 seconds"; Thread.sleep 17000;
		}
		success= { context ->  println "ok, process3 is finished now, you can run all the rest" }
	}

	process4{
		dependencies= "process2, process3"
		tasks = { context ->  println "This one will run at last. ${context.val}"  }
	}
	process5{
		dependencies= "process2, process4"
		tasks = { context ->

			println "This one gonna fail";
			println "Hey!!!"
		}
	}
}