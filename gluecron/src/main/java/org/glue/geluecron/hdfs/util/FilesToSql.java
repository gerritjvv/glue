package org.glue.geluecron.hdfs.util;

import java.util.Iterator;

/**
 * 
 * Load files into a mysql table.
 * 
 * 
 */
public interface FilesToSql<T> {

	/**
	 * 
	 * @param it
	 * @param resetTS
	 *            if true the timestamp of a file can cause the seen flag to be
	 *            reset.
	 */
	void loadFiles(Iterator<T> it, boolean resetTS);

}
