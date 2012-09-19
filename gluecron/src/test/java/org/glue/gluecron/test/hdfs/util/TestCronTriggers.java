package org.glue.gluecron.test.hdfs.util;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;
import org.junit.Assert;
import org.quartz.CronExpression;

/**
 * 
 * 
 *
 */
public class TestCronTriggers {

	@Test
	public void testCrons() throws Throwable{
			
			System.out.println("is: " + new CronExpression("* * * * * ?").isSatisfiedBy(new Date()));
			Assert.assertTrue(new CronExpression("* * * * * ?").isSatisfiedBy(new Date()));
			
			{
				// nextSatisfied is within lastCheckTime and thisCheckTime:
				Date lastCheckTime = new Date(System.currentTimeMillis() - 1000 * 60);
				Date nextSatisfied = new Date(System.currentTimeMillis() - 1000 * 30);
				Date thisCheckTime = new Date(System.currentTimeMillis());
				Assert.assertTrue(nextSatisfied.compareTo(thisCheckTime) <= 0);
			}
			
			{
				// nextSatisfied is after thisCheckTime:
				Date lastCheckTime = new Date(System.currentTimeMillis() - 1000 * 60);
				Date nextSatisfied = new Date(System.currentTimeMillis() + 1000 * 60);
				Date thisCheckTime = new Date(System.currentTimeMillis());
				Assert.assertFalse(nextSatisfied.compareTo(thisCheckTime) <= 0);
			}
			
			{
				// nextSatisfied is equal to thisCheckTime:
				Date lastCheckTime = new Date(System.currentTimeMillis() - 1000 * 60);
				Date nextSatisfied = new Date(System.currentTimeMillis());
				Date thisCheckTime = new Date(System.currentTimeMillis());
				Assert.assertTrue(nextSatisfied.compareTo(thisCheckTime) <= 0);
			}
			
			{
				String cronExpression = "00 00 01 ? * SUN";
				Assert.assertTrue(isUseableCronExpression(cronExpression));
				Assert.assertTrue(CronExpression.isValidExpression(cronExpression));
			}
			
			{
				String cronExpression = "00 00 01 00 * SUN";
				Assert.assertFalse(isUseableCronExpression(cronExpression));
				Assert.assertFalse(CronExpression.isValidExpression(cronExpression));
			}
			
			{
				String cronExpression = "00 00 01 * * SUN";
				// This fails, isValidExpression returns true for an unsupported feature.
				//Assert.assertFalse(isUseableCronExpression(cronExpression));
				//Assert.assertFalse(CronExpression.isValidExpression(cronExpression));
				System.out.println("'" + cronExpression + "' isUseableCronExpression: " + isUseableCronExpression(cronExpression));
				System.out.println("'" + cronExpression + "' isValidExpression:       " + CronExpression.isValidExpression(cronExpression));
			}
			
			// Other tests. These will cause errors but must not affect others.
			// insert into unittriggers values(null, 'asdf1', 'hdfs', '/path/not/found', null);
			// insert into unittriggers values(null, 'asdf2', 'hdfs', 'bad path/this will not be accepted!/:', null);
			// insert into unittriggers values(null, 'asdf3', 'cron', '00 00 01 * * SUN', null);
			
			System.out.println("TestCronTriggers.testCrons test completed");
		
	}
	
	private static boolean isUseableCronExpression(String cronExpression)
	{
		try {
			(new CronExpression(cronExpression)).getNextValidTimeAfter(new Date());
			return true;
		} catch (Exception e) {
			System.out.println("DEBUG: isUseableCronExpression is false for '" + cronExpression + "' because: " + e.toString());
			return false;
		}
	}
	
}
