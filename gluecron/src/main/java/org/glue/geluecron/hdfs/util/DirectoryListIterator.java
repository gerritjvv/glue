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
 * that the api can list roughly 500K files in +- 25 seconds.
 */
public final class DirectoryListIterator implements Iterator<Path> {

	static final Logger LOG = Logger.getLogger(DirectoryListIterator.class);

	final List<Path> pathCache = new ArrayList<Path>(100);

	final Path baseDir;
	final FileSystem fs;

	final LinkedList<Path> pathStack = new LinkedList<Path>();
	final Filter filter;

	/**
	 * @param fs
	 * @param baseDir
	 */
	public DirectoryListIterator(FileSystem fs, Path baseDir) {
		this(fs, baseDir, null);
	}

	/**
	 * 
	 * @param fs
	 * @param baseDir
	 * 
	 */
	public DirectoryListIterator(FileSystem fs, Path baseDir, Filter filter) {
		super();
		this.fs = fs;
		this.baseDir = baseDir;
		pathStack.push(baseDir);
		this.filter = filter;
	}

	@Override
	public final boolean hasNext() {

		if (pathCache.size() < 1) {
			// load next files into cache
			while (pathCache.size() < 1 && pathStack.size() > 0) {
				try {
					final Path path = pathStack.pop();

					if (path == null) {
						LOG.warn("Path not expected to be null here");
						continue;
					}

					final FileStatus[] statusArr = fs.listStatus(path);

					if (statusArr == null) {
						LOG.warn(path + " listStatus: Null");
						continue;
					}

					final int len = statusArr.length;
					FileStatus status;
					for (int i = 0; i < len; i++) {
						status = statusArr[i];

						if (filter != null)
							if (!filter.accept(status))
								continue; // we skip this file/dir if the filter
											// does not accept it

						pathCache.add(status.getPath());
						if (status.isDir())
							pathStack.push(status.getPath());
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
	public final Path next() {
		return (pathCache.size() > 0) ? pathCache.remove(0) : null;
	}

	@Override
	public void remove() {

	}

	public static interface Filter {
		boolean accept(FileStatus status);
	}

	/**
	 * Only returns true if the Path is a Directory
	 */
	public final static class DirectoryOnlyFilter implements Filter {
		public boolean accept(FileStatus status) {
			return status.isDir();
		}
	}

}
