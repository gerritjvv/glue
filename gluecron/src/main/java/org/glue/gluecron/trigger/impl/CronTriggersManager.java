package org.glue.gluecron.trigger.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.glue.geluecron.db.DBManager;
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
		final List<String> names = new ArrayList<String>(10);

		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;

		LOG.info("Checking crons");
		final long start = System.currentTimeMillis();
		
		final Date thisCheckTime = new Date();
		try {
			conn = dbManager.getConnection();
			st = dbManager.createStatement(conn);

			rs = dbManager.query(st, "SELECT unit, data FROM " + unitTriggersTbl
					+ " WHERE type = \"cron\" ");

			if (rs.first()) {
				do {
					try {
						final String name = rs.getString(1);
						final String data = rs.getString(2);
	
						if (data.trim().length() > 0
								&& CronExpression.isValidExpression(data)) {
							// evaluate the cron
							try {
								CronExpression ce = new CronExpression(data);
								Date nextSatisfied = ce.getNextValidTimeAfter(lastCheckTime);
								if (nextSatisfied.compareTo(thisCheckTime) <= 0) {
									names.add(name);
								}
							} catch (ParseException e) {
								LOG.error(e.toString(), e);
							}
	
						} else {
							LOG.warn("No valid cron expression " + data + " for "
									+ name);
						}
					} catch (Exception e) {
						// Don't allow one cron's error to stop the rest, such as UnsupportedOperationException.
						LOG.warn(e.toString());
					}
				} while (rs.next());
			}

		} catch (SQLException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		} finally {
			lastCheckTime = thisCheckTime;
			dbManager.close(rs);
			dbManager.close(st);
			dbManager.release(conn);
		}
		
		LOG.info("Took " + (System.currentTimeMillis() - start) + "ms");
		
		return names.toArray(new String[0]);
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
