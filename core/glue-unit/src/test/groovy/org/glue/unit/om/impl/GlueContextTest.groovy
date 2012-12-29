package org.glue.unit.om.impl

import static org.junit.Assert.*

import org.apache.log4j.Logger
import org.junit.Test

class GlueContextTest {
	static final Logger log = Logger.getLogger(GlueContextTest.class)

	/**
	 * Test the eval method on a static class
	 */
	@Test
	public void testEvalStatic() {

		GlueContextImpl ctx = new GlueContextImpl()
		ctx.eval('org.glue.unit.om.impl.MyClass','setValue', true)
		
		assertTrue(MyClass.isSet())	
	}

	/**
	* Test the eval method on a static class
	*/
   @Test
   public void testNewInstance() {

	   GlueContextImpl ctx = new GlueContextImpl()
	   MyClass m = ctx.newInstance('org.glue.unit.om.impl.MyClass')
	   
	   assertNotNull(m)
   }

}
