package org.glue.test.modules.hadoop.impl

import jcascalog.Api

import org.junit.Test

import org.glue.modules.hadoop.cascalog.CascalogModule

class CascalogModuleTest {


	@Test
	public void testRun(){

		CascalogModule module = new CascalogModule()

		def conf = """
			clusters{
				abc{
					jobconf="src/test/resources/pig.properties"
					isDefault=true
				}
			}
		"""
		module.init(new ConfigSlurper().parse(conf))
		module.exec(
				"""
					(?<- (stdout) [?person] (age ?person 25))
				""")
	}
}
