package org.glue.geluecron.hdfs.util;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.hadoop.fs.FileStatus;
import org.apache.log4j.Logger;
import org.glue.geluecron.db.DBManager;

/**
 * 
 * Fast implementation for load the hdfs files to a sql table Any table is
 * excepted as long as it has a columns called 'path, ts, seen'
 */
public final class JDBCFilesToSql implements FilesToSql<FileStatus> {

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
	public final void loadFiles(Iterator<FileStatus> it, boolean resetTS) {

		Connection conn = null;
		StringBuilder buff = new StringBuilder(1000);
		String header = "INSERT INTO " + tbl + "(path, ts) ";
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
					final FileStatus fileStatus = it.next();
					final String path = fileStatus.getPath().toUri().getPath();
					final long modTS = fileStatus.getModificationTime();

					if (i == 0) {
						i = 1;
						buff.append(header).append("VALUES");
						buff.append("('").append(path)
								.append("'," + modTS + ")");
					} else {
						buff.append(",('").append(path)
								.append("'," + modTS + ")");
					}

					if (++rows % batchSize == 0) {
						// for each batchSize rows execute

						// on duplicate key check the modification time stamp,
						// if the new timestamp is higher set the seen flag to 0
						// meaning that this modified version has not been seen
						// before
						if(resetTS)
							buff.append(" on duplicate key update path=path, seen=if(ts < values(ts), 0, 1), ts=values(ts)");
						else 
							buff.append(" on duplicate key update path=path, ts=values(ts)");
						
						
						st.execute(buff.toString());
						buff.delete(0, buff.length());
						i = 0;
						LOG.info("Row Count: " + rows);
					}
				}

				// add remaining:
				if (buff.length() > 0) {
					if(resetTS)
						buff.append(" on duplicate key update path=path, seen=if(ts < values(ts), 0, 1), ts=values(ts)");
					else 
						buff.append(" on duplicate key update path=path, ts=values(ts)");
					
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
			LOG.error(buff.toString());
			t.printStackTrace();
			RuntimeException rte = new RuntimeException(t.toString(), t);
			rte.setStackTrace(t.getStackTrace());
			throw rte;
		} finally {

			dbManager.release(conn);

		}

	}
	
}
