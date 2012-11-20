package org.glue.geluecron.hdfs.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

/**
 * 
 * Iterates through a directory path going into child paths Benchmarks suggest
 * that the api can list roughly 500K files in +- 25 seconds.<br/>
 * Instead of storing the Path this class stores the FileStatus Instance
 */
public final class DirectoryListIterator2 implements Iterator<FileStatus> {

	static final Logger LOG = Logger.getLogger(DirectoryListIterator2.class);

	final List<FileStatus> pathCache = new ArrayList<FileStatus>(100);

	final FileStatus baseDir;
	final FileSystem fs;

	final LinkedList<FileStatus> pathStack = new LinkedList<FileStatus>();

	/**
	 * 
	 * @param fs
	 * @param baseDir
	 * 
	 */
	public DirectoryListIterator2(FileSystem fs, FileStatus baseDir) {
		super();
		this.fs = fs;
		this.baseDir = baseDir;
		pathStack.push(baseDir);
	}

	@Override
	public final boolean hasNext() {

		if (pathCache.size() < 1) {
			// load next files into cache
			while (pathCache.size() < 1 && pathStack.size() > 0) {
				try {
					final FileStatus path = pathStack.pop();

					if (path == null) {
						LOG.warn("Path not expected to be null here");
						continue;
					}

					final FileStatus[] statusArr = fs
							.listStatus(path.getPath());

					if (statusArr == null) {
						LOG.warn(path + " listStatus: Null");
						continue;
					}

					final int len = statusArr.length;
					FileStatus status;
					for (int i = 0; i < len; i++) {
						status = statusArr[i];
						pathCache.add(status);
						if (status.isDir())
							pathStack.push(status);
					}

				} catch (IOException e) {
					RuntimeException exp = new RuntimeException(e.toString(), e);
					exp.setStackTrace(e.getStackTrace());
					throw exp;
				}

			}

		}

		return pathCache.size() > 0;
	}

	@Override
	public final FileStatus next() {
		return (pathCache.size() > 0) ? pathCache.remove(0) : null;
	}

	@Override
	public void remove() {

	}

}
