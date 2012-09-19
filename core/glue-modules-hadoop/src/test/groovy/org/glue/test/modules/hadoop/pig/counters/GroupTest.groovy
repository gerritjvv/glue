package org.glue.test.modules.hadoop.pig.counters

import org.glue.modules.hadoop.pig.counters.Group
import org.junit.Test
import static org.junit.Assert.*

/**
 * 
 * Tests that the Group creation logic with As and With keywords works as expected.
 *
 */
class GroupTest {


	@Test
	public void testCreateAs(){
		def g = new Group("var1Asvar2")

		assertEquals("var1", g.column)
		assertEquals("var2", g.alias)
		assertEquals('', g.function)
	}

	@Test
	public void testCreateWith(){
		def g = new Group("var1WithmyFunction.123")

		assertEquals("var1", g.column)
		assertEquals("var1", g.alias)
		assertEquals("myFunction.123", g.function)
	}

	@Test
	public void testCreateAsAndWith(){
		def g = new Group("var1Asvar2WithmyFunction.123")

		assertEquals("var1", g.column)
		assertEquals("var2", g.alias)
		assertEquals("myFunction.123", g.function)
	}

	@Test
	public void testCreateSimple(){
		def g = new Group("var1")

		assertEquals("var1", g.column)
		assertEquals("var1", g.alias)
		assertEquals("", g.function)
	}
}
