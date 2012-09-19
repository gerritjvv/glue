package org.glue.gluetest.mail

import java.util.concurrent.atomic.AtomicInteger;

import org.glue.gluetest.MockGlueModule

/**
 * 
 * Implements the same methods as that of the mail module, but does nothing.
 *
 */
class MockMailModule extends MockGlueModule{

	/**
	 * On each mail method call this counter is incremented.
	 */
	AtomicInteger counter = new AtomicInteger(0)
	
	public void mail(String[] recipients, String subject, String body) {
		counter.incrementAndGet()
		println "--- mock send mail $recipients  $subject"
	}

}
