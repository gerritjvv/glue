package org.glue.gluecron.trigger.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.glue.geluecron.db.DBManager;
import org.glue.geluecron.db.DBManager.DBQueryMapClosure;
import org.glue.geluecron.hdfs.util.DirectoryListIterator2;
import org.glue.geluecron.hdfs.util.JDBCFilesToSql2;
import org.glue.gluecron.trigger.HDFSHistoryDataAttribute;
import org.glue.gluecron.trigger.TriggerListener;
import org.glue.gluecron.trigger.TriggersManager;

/**
 * 
 * Will run a workflow whenever a file is older than N hours
 * 
 * <p/>
 * 
 */
public class HdfsHistoryTriggersManager implements TriggersManager, Runnable {

	private static final Logger LOG = Logger
			.getLogger(HdfsHistoryTriggersManager.class);

	private final ScheduledExecutorService schedule;
	private final DBManager dbManager;

	private final JDBCFilesToSql2 jdbcFilesToSQL;

	private final String hdfsfilesTbl;
	private final String unitTriggersTbl;
	private final String unitfilesTbl;

	private final FileSystem fs;

	TriggerListener listener;

	@SuppressWarnings("rawtypes")
	public HdfsHistoryTriggersManager(DBManager dbManager, Configuration conf) {
		this.dbManager = dbManager;

		final int minutes = conf.getInt("history.refresh.freq", 60 * 24);
		hdfsfilesTbl = conf.getString("hdfsfiles-history.table",
				"hdfsfiles_history");
		unitTriggersTbl = conf.getString("unittriggers.table", "unittriggers");
		unitfilesTbl = conf.getString("unitfiles-history.table",
				"unitfiles_history");

		jdbcFilesToSQL = new JDBCFilesToSql2(dbManager, hdfsfilesTbl, true);

		org.apache.hadoop.conf.Configuration hdfsconf = new org.apache.hadoop.conf.Configuration();
		final Iterator keyit = conf.getKeys();
		while (keyit.hasNext()) {
			String key = keyit.next().toString();
			hdfsconf.set(key, conf.getString(key));
		}

		try {
			fs = FileSystem.get(hdfsconf);
		} catch (IOException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		}

		LOG.info("Connected to FS: " + fs.getUri());

		schedule = Executors.newScheduledThreadPool(1);
		schedule.scheduleWithFixedDelay(this, 0, minutes, TimeUnit.MINUTES);

	}

	public void run() {
		try {
			LOG.info("Start polling");
			final long start = System.currentTimeMillis();
			// get paths
			HDFSHistoryDataAttribute[] attributes = getDataAttributes();

			if (attributes.length > 0) {
				pollHdfs(attributes);
				fillUnitFiles();
				checkFiles();
			}

			LOG.info("End " + (System.currentTimeMillis() - start) + "ms");
		} catch (Throwable t) {
			LOG.error(t.toString(), t);
		}
	}

	/**
	 * Check files against their data logic and mark status as "process" when
	 * the logic returns true
	 */
	private void checkFiles() {
		long start = System.currentTimeMillis();

		// first collect the data fields for each unit with type = hdfs-history
		HDFSHistoryDataAttribute[] attributes = getDataAttributes();

		for (HDFSHistoryDataAttribute attr : attributes) {
			try {
				dbManager.exec("UPDATE " + unitfilesTbl
						+ " set status='process' where unitid="
						+ attr.getUnitId() + " and "
						+ attr.getDateChooseScript());
			} catch (Throwable t) {
				LOG.error(t.toString(), t);
			}
		}
		LOG.info("Checkfiles: " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * 
	 * 
	 * @return
	 */
	private final HDFSHistoryDataAttribute[] getDataAttributes() {
		return dbManager.map(
				"SELECT DISTINCT id, unit, data FROM " + unitTriggersTbl
						+ " WHERE type='hdfs-history'",
				new DBQueryMapClosure<HDFSHistoryDataAttribute>() {

					public HDFSHistoryDataAttribute call(ResultSet rs)
							throws Exception {
						return new HDFSHistoryDataAttribute(rs.getLong(1), rs
								.getString(2), rs.getString(3));
					}
				}).toArray(new HDFSHistoryDataAttribute[0]);

	}

	private final void pollHdfs(HDFSHistoryDataAttribute[] attributes) {
		// get all of the paths to poll.

		final long start = System.currentTimeMillis();

		for (int i = 0; i < attributes.length; i++) {
			try {
				final String path = attributes[i].getPath();
				LOG.info("Polling for Path " + path);
				Path dirpath = new Path(path);
				if (!fs.exists(dirpath)) {
					throw new FileNotFoundException("Not found: " + path);
				}

				jdbcFilesToSQL.loadFiles(new DirectoryListIterator2(fs, fs
						.getFileStatus(dirpath)));

			} catch (Exception e) {
				// Don't allow one trigger's error to stop the rest, such as
				// IllegalArgumentException.
				LOG.error(e.toString(), e);
			}
		}
		LOG.info("\tpollHdfs : " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * Run a series of queries to fill the unit files
	 */
	private final void fillUnitFiles() {

		long start = System.currentTimeMillis();

		// insert entries into unitfiles
		dbManager
				.exec("insert ignore into "
						+ unitfilesTbl
						+ " (unitid, fileid, datetime, status) select ut.id, hf.id, hf.datetime, 'ready' from "
						+ hdfsfilesTbl
						+ " hf, "
						+ unitTriggersTbl
						+ " ut where ut.type = 'hdfs-history' AND hf.seen=0 AND SUBSTRING(hf.path, 1, LENGTH(substring_index(ut.data, ',', 1))) = substring_index(ut.data, ',', 1) AND LENGTH(hf.path) >= LENGTH(ut.data)",

						"UPDATE "
								+ hdfsfilesTbl
								+ " hf,"
								+ unitfilesTbl
								+ " uf SET hf.seen = 1 WHERE uf.fileid = hf.id AND uf.status='ready'");

		// if a listener is registered
		// and there are ready files for units,
		// call launch on listener.
		if (listener != null) {
			String[] unitNames = getUnitNames();
			if (unitNames.length > 0)
				listener.launch(unitNames);
		}

		LOG.info("\tfillUnitFiles : " + (System.currentTimeMillis() - start)
				+ "ms");
	}

	public String[] getUnitNames() {

		//
		// select unit name, datetime and the path itself.
		//
		
		String[] names = dbManager.map(
				"select distinct unit from " + unitTriggersTbl + " ut, "
						+ unitfilesTbl
						+ " uf WHERE ut.id=uf.unitid and uf.status='process'",
				new DBQueryMapClosure<String>() {
					@Override
					public String call(ResultSet rs) throws Exception {
						return rs.getString(1);
					}
				}).toArray(new String[0]);

		LOG.info("GetUnitNames: " + Arrays.toString(names));
		return names;
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
