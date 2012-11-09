package org.glue.geluecron.hdfs.util;

import java.util.Iterator;

import org.apache.hadoop.fs.Path;

/**
 * 
 * Load files into a mysql table.
 * 
 *
 */
public interface FilesToSql<T> {

	void loadFiles(Iterator<T> it);
	
}
