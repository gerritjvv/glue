package org.glue.test.modules.hadoop.pig.counters

import static org.junit.Assert.*

import org.glue.modules.hadoop.pig.counters.Counter
import org.junit.Test

/**
 * 
 * Tests that the Group creation logic with As and With keywords works as expected.
 *
 */
class CounterTest {


	@Test
	public void testCreateAs(){
		def g = new Counter("var1Asvar2")

		assertEquals("var1", g.column)
		assertEquals("var2", g.alias)
		assertEquals('', g.function)
	}

	@Test
	public void testCreateWith(){
		def g = new Counter("var1WithmyFunction.123")

		assertEquals("var1", g.column)
		assertEquals("var1", g.alias)
		assertEquals("myFunction.123", g.function)
	}

	@Test
	public void testCreateAsAndWith(){
		def g = new Counter("var1Asvar2WithmyFunction.123")

		assertEquals("var1", g.column)
		assertEquals("var2", g.alias)
		assertEquals("myFunction.123", g.function)
	}

	@Test
	public void testCreateSimple(){
		def g = new Counter("var1")

		assertEquals("var1", g.column)
		assertEquals("var1", g.alias)
		assertEquals("", g.function)
	}
}
