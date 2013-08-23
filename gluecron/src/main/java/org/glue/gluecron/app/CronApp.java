package org.glue.gluecron.app;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.glue.geluecron.db.DBManager;
import org.glue.geluecron.db.DBManagerImpl;
import org.glue.gluecron.trigger.TriggersManager;
import org.glue.gluecron.trigger.impl.CronTriggersManager;
import org.glue.gluecron.trigger.impl.HdfsHistoryTriggersManager;
import org.glue.gluecron.trigger.impl.HdfsTriggersManager;

import com.mysql.jdbc.Driver;

public class CronApp {

	private static final Logger LOG = Logger.getLogger(CronApp.class);

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Throwable {

		final Configuration conf = new PropertiesConfiguration(
				new File(args[1]));

		final int metricsPort = conf.getInt("metrics.port", 7001);
		
		final DBManager dbManager = getDBManager(conf);
		final TriggersManager hfdsTriggerManager = getHdfsTriggerManager(
				dbManager, conf);
		final TriggersManager cronTriggersManager = getCronTriggerManager(
				dbManager, conf);

		final TriggersManager hdfsHistoryTriggersManager = getHdfsHistoryTriggerManager(
				dbManager, conf);

		GlueTriggerWatch watch = new GlueTriggerWatch(conf, hfdsTriggerManager);
		GlueTriggerWatch watch2 = new GlueTriggerWatch(conf,
				cronTriggersManager);
		GlueTriggerWatch watch3 = new GlueTriggerWatch(conf,
				hdfsHistoryTriggersManager);

		System.out.println("Started");

		Metrics.start(metricsPort);
		
		// wait until the current thread has bee interrupted.
		while (!Thread.interrupted()) {
			Thread.sleep(Long.MAX_VALUE);
		}

		hfdsTriggerManager.close();
		cronTriggersManager.close();
		hdfsHistoryTriggersManager.close();
		
		dbManager.destroy();

	}

	private static TriggersManager getCronTriggerManager(DBManager dbManager,
			Configuration conf) {
		return new CronTriggersManager(dbManager, conf);
	}

	private static TriggersManager getHdfsTriggerManager(DBManager dbManager,
			Configuration conf) {
		return new HdfsTriggersManager(dbManager, conf);
	}

	private static TriggersManager getHdfsHistoryTriggerManager(
			DBManager dbManager, Configuration conf) {
		return new HdfsHistoryTriggersManager(dbManager, conf);
	}

	private static DBManager getDBManager(Configuration conf)
			throws ClassNotFoundException {

		final String url = conf.getString("db.jdbc",
				"jdbc:mysql://localhost:3306");
		final String driver = conf.getString("db.driver",
				Driver.class.getName());
		final String uid = conf.getString("db.uid", "glue");
		final String pwd = conf.getString("db.pwd", "glue");

		Thread.currentThread().getContextClassLoader().loadClass(driver);

		LOG.info("Connecting to DB " + url + " user: " + uid);
		return new DBManagerImpl(url, uid, pwd);

	}

}
