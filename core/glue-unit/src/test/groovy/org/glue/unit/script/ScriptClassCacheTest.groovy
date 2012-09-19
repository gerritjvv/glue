package org.glue.unit.script;

import static org.junit.Assert.*

import org.junit.Test

/**
 *
 * Test that the ScriptClassCache does cache the scripts correctly.
 *
 */
class ScriptClassCacheTest {


	/**
	 * Parse config object from text
	 */
	@Test
	public void testParseFromText(){

		def scriptText = """
		
		   a="Hi"
		
		"""

		def object = ScriptClassCache.getDefaultInstance().parse(scriptText)

		assertNotNull(object)
		//use groovy truth here
		assertEquals(object.a, 'Hi')

	}

	/**
	 * Parser script class from text
	 */
	@Test
	public void testScriptFromText(){

		def scriptText = """
		
		   println 'Hi'
		
		"""

		Class<Script> script1 = ScriptClassCache.getDefaultInstance().loadScriptText(scriptText)

		Class<Script> script2 = ScriptClassCache.getDefaultInstance().loadScriptText(scriptText)

		assertEquals(script1, script2)
	}

	/**
	 * Parse config object from  file
	 */
	@Test
	public void testParseFromFile(){

		def scriptText = """
		
		   a="Hi"
		
		"""

		File dir = new File('target/test/ScriptClassCacheTest/testParseFromText')
		dir.mkdirs()
		File file = new File(dir, 'test.groovy');
		file.createNewFile()
		file.text = scriptText

		try{
			def object = ScriptClassCache.getDefaultInstance().parse(file)
			object = ScriptClassCache.getDefaultInstance().parse(file)
			assertNotNull(object)
			//use groovy truth here
			assertEquals(object.a, 'Hi')
		}catch(t){
			println t
			t.printStackTrace()
		}

		file.delete()
	}

	/**
	 * Parse script class from file
	 */
	@Test
	public void testScriptFromFile(){

		def scriptText = """
		
		   println 'Hi'
		
		"""

		File dir = new File('target/test/ScriptClassCacheTest/testScriptFromFile')
		dir.mkdirs()
		File file = new File(dir, 'test.groovy');

		file.text = scriptText

		Class<Script> script1 = ScriptClassCache.getDefaultInstance()
				.loadScriptFile(file)

		Class<Script> script2 = ScriptClassCache.getDefaultInstance()
				.loadScriptFile(file)

		assertEquals(script1, script2)

		file.delete()
	}
}
