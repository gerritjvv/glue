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
public final class DirectoryListIterator implements Iterator<FileStatus> {

	static final Logger LOG = Logger.getLogger(DirectoryListIterator.class);

	final List<FileStatus> pathCache = new ArrayList<FileStatus>(1000);

	final Path baseDir;
	final FileSystem fs;

	final LinkedList<FileStatus> pathStack = new LinkedList<FileStatus>();
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
		try {
			pathStack.push(fs.getFileStatus(baseDir));
		} catch (IOException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		}
		
		this.filter = filter;
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

					final FileStatus[] statusArr = fs.listStatus(path.getPath());

					if (statusArr == null) {
						LOG.warn(path + " listStatus: Null");
						continue;
					}

					final int len = statusArr.length;
					for (int i = 0; i < len; i++) {
						final FileStatus status = statusArr[i];

						if (filter != null)
							if (!filter.accept(status))
								continue; // we skip this file/dir if the filter
											// does not accept it

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
