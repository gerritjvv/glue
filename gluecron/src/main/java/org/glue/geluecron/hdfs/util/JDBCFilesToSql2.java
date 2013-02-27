package org.glue.geluecron.hdfs.util;

import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.hadoop.fs.FileStatus;
import org.apache.log4j.Logger;
import org.glue.geluecron.db.DBManager;

/**
 * 
 * Fast implementation for load the hdfs files to a sql table Any table is
 * excepted as long as it has a column called 'path' and if useDate == true a
 * datetime column is required.
 * 
 */
public final class JDBCFilesToSql2 implements FilesToSql<FileStatus> {

	private static final Logger LOG = Logger.getLogger(JDBCFilesToSql2.class);

	final DBManager dbManager;
	final String tbl;
	final boolean useDate;

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
	public JDBCFilesToSql2(DBManager dbManager, String tbl, boolean useDate) {
		super();
		this.dbManager = dbManager;
		this.tbl = tbl;
		this.useDate = useDate;
	}

	@Override
	public final void loadFiles(Iterator<FileStatus> it, boolean resetTS) {

		Connection conn = null;
		StringBuilder buff = new StringBuilder(1000);
		String header = "INSERT IGNORE INTO " + tbl;
		
		if(useDate)
			header += "(path, datetime) ";
		else
			header += "(path)";
		
		try {
			conn = dbManager.getConnection();
			// write the file content to a local file

			int rows = 0, i = 0;
			final int batchSize = 10000;

			// create simple insert statement
			// final java.sql.PreparedStatement st = conn
			// .prepareStatement("INSERT INTO " + tbl + "(path) VALUES(?)");
			final Statement st = dbManager.createStatement(conn);
			
			//include set if useDate == true
			final SimpleDateFormat format = (useDate) ? new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss") : null;

			try {
				// for each file and directory entry add to the insert
				while (it.hasNext()) {
					// st.setString(1, it.next().toUri().getPath());
					// st.addBatch();

					final FileStatus status = it.next();
					final String path = status.getPath().toUri().getPath();

					if (i == 0) {
						i = 1;
						buff.append(header).append("VALUES");
						buff.append("('").append(path).append("'");

						if (useDate)
							buff.append(",'")
									.append(format.format(new Date(status
											.getModificationTime())))
									.append("'");

						buff.append(")");
					} else {
						buff.append(",('").append(path).append("'");
						if (useDate)
							buff.append(",'")
									.append(format.format(new Date(status
											.getModificationTime())))
									.append("'");

						buff.append(")");
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
