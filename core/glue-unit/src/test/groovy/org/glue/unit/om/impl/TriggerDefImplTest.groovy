package org.glue.unit.om.impl;
import static org.junit.Assert.*
import groovy.text.SimpleTemplateEngine

import org.glue.unit.om.TriggerDef
import org.junit.Test

class TriggerDefImplTest {

	
	/**
	 * Test that we can parse a single trigger
	 */
	@Test
	public void testParsing1Trigger(){
		
		def str = "hdfs:/myfile"
		
		TriggerDef[] triggers = TriggerDefImpl.parse(str)
		
		assertNotNull(triggers)
		assertEquals(1, triggers.size())
		
		assertEquals("hdfs", triggers[0].type)
		assertEquals("/myfile", triggers[0].value)
		
	}
	
	/**
	 * Test that we can parse a line with multiple triggers
	 */
	@Test
	public void testParsingMultipleTriggers(){
		
		String[] triggerDefs = ["hdfs:/myfile", "cron:3 3 3 3", "hdfs:abcs dfde *", "hdfs: b bdfer - "]
		
		
		def str = triggerDefs.join(";")
		
		TriggerDef[] triggers = TriggerDefImpl.parse(str)
		
		assertNotNull(triggers)
		assertEquals(triggerDefs.size(), triggers.size())
		
		int i = 0
		triggers.each { trigger ->
			
			def triggerDef = triggerDefs[i++]
			def (type, value) = triggerDef.split(":")
			
			assertEquals(type, trigger.type)
			assertEquals(value, trigger.value)
		}
		
		
	}
	
}
