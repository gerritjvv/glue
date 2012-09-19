package org.glue.gluecron.app;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.glue.geluecron.hdfs.util.DirectoryListIterator;

/**
 * Helper app that lists the recursively from an hdfs directory
 * 
 */
public class ListFiles {

	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		conf.addResource(args[1]);

		FileSystem fs = FileSystem.get(conf);
		
		
		DirectoryListIterator it = new DirectoryListIterator(fs, new Path(args[2]));
		
		while(it.hasNext()){
			System.out.println(it.next().toUri().getPath());
		}
		
	}
	
}
