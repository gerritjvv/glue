package org.glue.geluecron.hdfs.util;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.glue.geluecron.db.DBManager;

/**
 * 
 * Fast implementation for load the hdfs files to a sql table Any table is
 * excepted as long as it has a column called 'path'
 */
public final class JDBCFilesToSql implements FilesToSql<Path> {

	private static final Logger LOG = Logger.getLogger(JDBCFilesToSql.class);

	final DBManager dbManager;
	final String tbl;

	/**
	 * @param url
	 *            JDBC url
	 * @param uid
	 *            user name
	 * @param pwd
	 *            password
	 * @param tbl
	 *            table name
	 */
	public JDBCFilesToSql(DBManager dbManager, String tbl) {
		super();
		this.dbManager = dbManager;
		this.tbl = tbl;
	}

	@Override
	public final void loadFiles(Iterator<Path> it) {

		Connection conn = null;
		StringBuilder buff = new StringBuilder(1000);
		String header = "INSERT IGNORE INTO " + tbl + "(path) ";
		try {
			conn = dbManager.getConnection();
			// write the file content to a local file

			int rows = 0, i = 0;
			final int batchSize = 10000;

			// create simple insert statement
			// final java.sql.PreparedStatement st = conn
			// .prepareStatement("INSERT INTO " + tbl + "(path) VALUES(?)");
			final Statement st = dbManager.createStatement(conn);

			try {
				// for each file and directory entry add to the insert
				while (it.hasNext()) {
					// st.setString(1, it.next().toUri().getPath());
					// st.addBatch();

					final String path = it.next().toUri().getPath();
					if (i == 0) {
						i = 1;
						buff.append(header).append("VALUES");
						buff.append("('").append(path).append("')");
					} else {
						buff.append(",('").append(path).append("')");
					}

					if (++rows % batchSize == 0) {
						// for each batchSize rows execute
						st.execute(buff.toString());
						buff.delete(0, buff.length());
						i = 0;
						LOG.info("Row Count: " + rows);
					}
				}

				// add remaining:
				if (buff.length() > 0) {
					st.execute(buff.toString());
					buff.delete(0, buff.length());
				}

				// execute for remaining inserts
				st.executeBatch();

			} finally {

				st.close();
			}
			LOG.info("Total rows: " + rows);

		} catch (Throwable t) {
			RuntimeException rte = new RuntimeException(t.toString(), t);
			rte.setStackTrace(t.getStackTrace());
			throw rte;
		} finally {

			dbManager.release(conn);

		}

	}

}
