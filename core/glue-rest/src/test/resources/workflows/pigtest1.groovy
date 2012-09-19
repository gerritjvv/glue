name="pigtest"
	tasks{
	lstest{
		tasks = { context ->
			println "Test of ls"
			context.modules.pig.ls('/').each{ d-> println d };
			Thread.sleep 2000
		}
		
			error = { context, t ->
				println "FAIL"
				t.printStackTrace()
			}
			
			success = { context ->
				println "Success"
			}
		}		
		
	mkdirtest{
		dependencies="lstest"
		tasks = { context ->
			println "Test of creating a directory"
			context.modules.pig.rm('/tmp/dimitest')
			context.modules.pig.mkdir('/tmp/dimitest') 
		}
		
		
	}
	
	sqltest{
		dependencies="lstest"
		tasks = { context ->
			println "Test of loading sql"
			def fileName = context.modules.pig.loadSql('ans_master','select bid, nid from banners limit 10000, 10000');
			context.banners=fileName;
		}
		
		error = { context, t ->
			println "FAIL"
			t.printStackTrace()
		}
		
		success = { context ->
			println "Success"
		}
	}
	
	pigruntest{
		dependencies="mkdirtest,sqltest"
		tasks = { context ->
			println "Test of running a sample pig script"
			context.modules.pig.run('test1', 'src/main/resources/pig/scripts/test.pig', ['banners_location':context.banners]) 
		}
		
		error = { context, t ->
			println "FAIL"
			t.printStackTrace()
		}
		
		success = { context ->
			println "Success"
						}
				}
	cleanup{
			dependencies = "pigruntest"
		tasks = { context ->
			context.modules.pig.rm(context.banners);
			println "Deleted ${context.banners}";
		}
		}
	}
modules{
	pig{
		defaultCluster="dc3"
}
}