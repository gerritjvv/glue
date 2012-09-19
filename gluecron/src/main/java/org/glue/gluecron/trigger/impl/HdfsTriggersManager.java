package org.glue.gluecron.trigger.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.glue.geluecron.db.DBManager;
import org.glue.geluecron.hdfs.util.DirectoryListIterator;
import org.glue.geluecron.hdfs.util.JDBCFilesToSql;
import org.glue.gluecron.trigger.TriggerListener;
import org.glue.gluecron.trigger.TriggersManager;

/**
 * 
 * Performs the following actions each refresh period:<br/>
 * <ul>
 * <li>Fill the hdfsfile table with updates from hadoop</li>
 * <li>Fill unitfiles table with paths that match and have not been inserted</li>
 * <li>If any files for a unit in the unifiles table are found in ready state,
 * launch the workflow</li>
 * </ul>
 * 
 * CREATE TABLE `hdfsfiles1` ( `id` int(11) NOT NULL AUTO_INCREMENT, `path`
 * varchar(1000) NOT NULL, seen TINYINT DEFAULT 0, INDEX `seen1` (`seen`),
 * UNIQUE KEY `path` (`path`), PRIMARY KEY `id1` (`id`) );
 * 
 * create table unittriggers ( id int(11) NOT NULL AUTO_INCREMENT, unit
 * varchar(100), type varchar(10), data varchar(100), lastrun date, PRIMARY KEY
 * (id));
 * 
 * create table unitfiles (unitid int(11), fileid int(11), status varchar(10),
 * FOREIGN KEY (unitid) REFERENCES unittriggers(id), UNIQUE KEY `a` (unitid,
 * fileid));
 * 
 * insert into unittriggers ( unit, type, data) VALUES('test1', 'hdfs',
 * '/log/raw'), ('test1', 'hdfs', '/log/raw'), ('test1', 'hdfs', '/log/raw')
 * 
 * insert ignore into unitfiles (unitid, fileid, status) select ut.id, hf.id,
 * 'ready' from hdfsfiles hf, unittriggers ut where ut.type = 'hdfs' AND
 * hf.seen=0 AND SUBSTRING(hf.path, 1, LENGTH(ut.data)) = ut.data AND
 * LENGTH(hf.path) >= LENGTH(ut.data);
 * 
 * UPDATE hdfsfiles hf,unitfiles uf SET hf.seen = 1 WHERE uf.fileid = hf.id AND
 * uf.status='ready';
 * 
 * 
 * select distinct unit from unittriggers ut, unitfiles uf WHERE ut.id=uf.unitid
 * and uf.status='ready' limit 10;
 * <p/>
 *
 */
public class HdfsTriggersManager implements TriggersManager, Runnable {

	private static final Logger LOG = Logger
			.getLogger(HdfsTriggersManager.class);

	private final ScheduledExecutorService schedule;
	private final DBManager dbManager;

	private final JDBCFilesToSql jdbcFilesToSQL;

	private final String hdfsfilesTbl;
	private final String unitTriggersTbl;
	private final String unitfilesTbl;

	private final FileSystem fs;

	TriggerListener listener;

	@SuppressWarnings("rawtypes")
	public HdfsTriggersManager(DBManager dbManager, Configuration conf) {
		this.dbManager = dbManager;

		final int minutes = conf.getInt("refresh.freq", 5);
		hdfsfilesTbl = conf.getString("hdfsfiles.table", "hdfsfiles");
		unitTriggersTbl = conf.getString("unittriggers.table", "unittriggers");
		unitfilesTbl = conf.getString("unitfiles.table", "unitfiles");

		jdbcFilesToSQL = new JDBCFilesToSql(dbManager, hdfsfilesTbl);

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
			String[] paths = getPaths();
			if (paths.length > 0) {
				pollHdfs(paths);
				fillUnitFiles();
			}

			LOG.info("End " + (System.currentTimeMillis() - start) + "ms");
		} catch (Throwable t) {
			LOG.error(t.toString(), t);
		}
	}

	/**
	 * Query the unitTriggersTbl to retrieve a list of paths for hdfs
	 * 
	 * @return
	 */
	private final String[] getPaths() {
		List<String> paths = new ArrayList<String>(10);

		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			conn = dbManager.getConnection();
			st = dbManager.createStatement(conn);

			rs = dbManager.query(st, "SELECT DISTINCT data FROM " + unitTriggersTbl
					+ " WHERE type='hdfs'");

			if (rs.first()) {
				do {
					paths.add(rs.getString(1));
				} while (rs.next());
			}

		} catch (SQLException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		} finally {
			dbManager.close(rs);
			dbManager.close(st);
			dbManager.release(conn);
		}

		return paths.toArray(new String[0]);
	}

	private final void pollHdfs(String[] paths) {
		// get all of the paths to poll.

		final long start = System.currentTimeMillis();
		
		for (int i = 0; i < paths.length; i++) {
			try {
				LOG.info("Polling for Path " + paths[i]);
				Path dirpath = new Path(paths[i]);
				if (!fs.exists(dirpath)) {
					throw new FileNotFoundException("Not found: " + paths[i]);
				}
				jdbcFilesToSQL.loadFiles(new DirectoryListIterator(fs, dirpath));
			} catch (Exception e) {
				// Don't allow one trigger's error to stop the rest, such as IllegalArgumentException.
				LOG.warn(e.toString());
			}
		}
		LOG.info("\tpollHdfs : " + (System.currentTimeMillis() - start ) + "ms");
	}

	/**
	 * Run a series of queries to fill the unit files
	 */
	private final void fillUnitFiles() {
		
		long start = System.currentTimeMillis();
		
		Connection conn = null;
		Statement st = null;
		try {
			conn = dbManager.getConnection();
			st = dbManager.createStatement(conn);

			// insert entries into unitfiles
			st.execute("insert ignore into "
					+ unitfilesTbl
					+ " (unitid, fileid, status) select ut.id, hf.id, 'ready' from "
					+ hdfsfilesTbl
					+ " hf, "
					+ unitTriggersTbl
					+ " ut where ut.type = 'hdfs' AND hf.seen=0 AND SUBSTRING(hf.path, 1, LENGTH(ut.data)) = ut.data AND LENGTH(hf.path) >= LENGTH(ut.data)");

			// ads the seen flag that marks the file as having been processed
			// this does cause any new trigger not be notified of already seen
			// files
			// but ads so much performance to the above query that its worth the
			// effort.
			st.execute("UPDATE "
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

		} catch (SQLException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		} finally {
			dbManager.close(st);
			dbManager.release(conn);
		}
		LOG.info("\tfillUnitFiles : " + (System.currentTimeMillis() - start ) + "ms");
	}

	public String[] getUnitNames() {
		Connection conn = null;
		try {
			conn = dbManager.getConnection();
			return getUnitNames(conn);
		} finally {
			dbManager.release(conn);
		}
	}

	private final String[] getUnitNames(Connection conn) {
		// query the database for a list of unit names to submit
		List<String> names = new ArrayList<String>(10);

		Statement st = null;
		ResultSet rs = null;
		try {
			st = dbManager.createStatement(conn);

			rs = dbManager.query(st, "select distinct unit from "
					+ unitTriggersTbl + " ut, " + unitfilesTbl
					+ " uf WHERE ut.id=uf.unitid and uf.status='ready'");

			if (rs.first()) {
				do {
					names.add(rs.getString(1));
				} while (rs.next());
			}

		} catch (SQLException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		} finally {
			dbManager.close(rs);
			dbManager.close(st);
		}

		return names.toArray(new String[0]);
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
