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
import org.mortbay.log.Log;

/**
 * 
 * Iterates through a directory path going into child paths
 * Benchmarks suggest that the api can list roughly 500K files in +- 25 seconds.
 */
public class DirectoryListIterator implements Iterator<Path> {

	static final Logger LOG = Logger.getLogger(DirectoryListIterator.class);
	
	final List<Path> pathCache = new ArrayList<Path>(100);

	final Path baseDir;
	final FileSystem fs;

	final LinkedList<Path> pathStack = new LinkedList<Path>();

	public DirectoryListIterator(FileSystem fs, Path baseDir) {
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
					final Path path = pathStack.pop();
					final FileStatus[] statusArr = fs.listStatus(path);
					
					if(statusArr == null){
						LOG.warn(path + " listStatus: Null");
						continue;
					}
					
					for (FileStatus status : statusArr) {
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

}
