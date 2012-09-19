package org.glue.unit.repo.impl;

import static org.junit.Assert.*

import org.glue.unit.repo.PathDateExtractor
import org.junit.Test

/**
 * 
 * Test that the PathDateExtractor can extract the dates from a path correctly.
 *
 */
class PathDateExtractorTest {

	@Test
	public void testDayDate(){

		String test = '/mypath/daydate=2011-01-05/'

		Date date = PathDateExtractor.extractDate(test)
		assertNotNull(date)
		assertEquals(2011, date[Calendar.YEAR])
		assertEquals(01, date[Calendar.MONTH]+1)
		assertEquals(05, date[Calendar.DAY_OF_MONTH])
	}

	@Test
	public void testDate(){

		String test = '/mypath/date=2011-01-05/'

		Date date = PathDateExtractor.extractDate(test)
		assertNotNull(date)
		assertEquals(2011, date[Calendar.YEAR])
		assertEquals(01, date[Calendar.MONTH]+1)
		assertEquals(05, date[Calendar.DAY_OF_MONTH])
	}

	@Test
	public void testYearMonthDayHour(){

		String test = '/mypath/year=2011/month=01/day=05/hour=04'

		Date date = PathDateExtractor.extractDate(test)
		assertNotNull(date)
		assertEquals(2011, date[Calendar.YEAR])
		assertEquals(01, date[Calendar.MONTH]+1)
		assertEquals(05, date[Calendar.DAY_OF_MONTH])
		assertEquals(04, date[Calendar.HOUR_OF_DAY])
	}
}
