package org.glue.unit.om.impl

import static org.junit.Assert.*

import org.apache.log4j.Logger
import org.junit.Test

import org.glue.unit.om.LazySequenceUtil


class LazySequenceUtilTest {
	static final Logger log = Logger.getLogger(LazySequenceUtilTest.class)

	/**
	 * Test that we can produce a lazy sequence
	 */
	@Test
	public void callStr() {

		int selectCalled = 0
		
		def seq = LazySequenceUtil.seq(
			
			{ pos -> 
				selectCalled++
				(0..9) as List
			}
			
			, 0)
		
		int i = 0
		for(e in seq){
			if(i++ > 100) break
		}
		
		assertEquals(11, selectCalled)
	}


}
