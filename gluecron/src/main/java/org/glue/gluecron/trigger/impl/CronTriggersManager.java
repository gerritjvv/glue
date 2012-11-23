package org.glue.gluecron.trigger.impl;

import java.sql.ResultSet;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.glue.geluecron.db.DBManager;
import org.glue.geluecron.db.DBManager.DBQueryMapClosure;
import org.glue.gluecron.trigger.TriggerListener;
import org.glue.gluecron.trigger.TriggersManager;
import org.quartz.CronExpression;

/**
 * 
 * Performs the following actions each refresh period:<br/>
 * 
 * Each data item in the unittriggers table of type == cron, should contain a
 * valid cron expression.
 * <p/>
 * create table unittriggers ( id int(11) NOT NULL AUTO_INCREMENT, unit
 * varchar(100), type varchar(10), data varchar(100), lastrun date, PRIMARY KEY
 * (id));
 * 
 * 
 * 
 */
public class CronTriggersManager implements TriggersManager, Runnable {

	private static final Logger LOG = Logger
			.getLogger(CronTriggersManager.class);

	private final ScheduledExecutorService schedule;
	private final DBManager dbManager;

	private final String unitTriggersTbl;

	TriggerListener listener;

	private Date lastCheckTime;

	public CronTriggersManager(DBManager dbManager, Configuration conf) {
		this.dbManager = dbManager;

		final int minutes = conf.getInt("refresh.freq", 5);
		unitTriggersTbl = conf.getString("unittriggers.table", "unittriggers");

		lastCheckTime = new Date();

		schedule = Executors.newScheduledThreadPool(1);
		schedule.scheduleWithFixedDelay(this, 0, minutes, TimeUnit.MINUTES);

	}

	public void run() {
		try {
			LOG.info("Start");
			final long start = System.currentTimeMillis();
			// get paths
			if (listener != null) {
				String[] names = getToRunUnitNames();
				if (names != null && names.length > 0)
					listener.launch(names);
			}
			LOG.info("End " + (System.currentTimeMillis() - start) + "ms");
		} catch (Throwable t) {
			LOG.error(t.toString(), t);
		}
	}

	/**
	 * Query the unitTriggersTbl to retrieve the cron types
	 * 
	 * @return
	 */
	private final String[] getToRunUnitNames() {

		LOG.info("Checking crons");
		final long start = System.currentTimeMillis();

		final Date thisCheckTime = new Date();
		try {
			return dbManager.map(
					"SELECT unit, data FROM " + unitTriggersTbl
							+ " WHERE type = \"cron\" ",
					new DBQueryMapClosure<String>() {

						@Override
						public String call(ResultSet rs) throws Exception {
							try {
								final String name = rs.getString(1);
								final String data = rs.getString(2);

								if (data.trim().length() > 0
										&& CronExpression
												.isValidExpression(data)) {
									// evaluate the cron
									final CronExpression ce = new CronExpression(
											data);
									final Date nextSatisfied = ce
											.getNextValidTimeAfter(lastCheckTime);
									if (nextSatisfied.compareTo(thisCheckTime) <= 0)
										return name;

								} else {
									LOG.warn("No valid cron expression " + data
											+ " for " + name);
								}
							} catch (Exception e) {
								// Don't allow one cron's error to stop the
								// rest, such as UnsupportedOperationException.
								LOG.warn(e.toString());
							}
							return null;
						}

					}).toArray(new String[0]);

		} finally {
			lastCheckTime = thisCheckTime;
			LOG.info("Took " + (System.currentTimeMillis() - start) + "ms");
		}

	}

	public String[] getUnitNames() {
		return getToRunUnitNames();
	}

	@Override
	public void close() {
		schedule.shutdown();
	}

	@Override
	public void setTriggerListener(TriggerListener listener) {
		this.listener = listener;
	}

}
