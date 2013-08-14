package org.glue.gluecron.test.hdfs.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.glue.geluecron.hdfs.util.DirectoryListIterator;
import org.glue.geluecron.hdfs.util.DirectoryListIterator.DirectoryOnlyFilter;
import org.junit.Test;

public class TestDirectoryListIterator {

	@Test
	public void testList() throws Throwable {

		File baseDir = new File("target/testDirectoryListIterator/testList");
		baseDir.mkdirs();
		File dir1 = new File(baseDir, "dir1");
		dir1.mkdirs();
		File dir2 = new File(baseDir, "dir2");
		dir2.mkdirs();
		File dira = new File(dir1, "dira");
		dira.mkdirs();
		File dirb = new File(dir2, "dirb");
		dirb.mkdirs();

		FileSystem fs = FileSystem.getLocal(new Configuration());

		DirectoryListIterator it = new DirectoryListIterator(fs, new Path(
				baseDir.getAbsolutePath()));

		int count = 0;
		while (it.hasNext()) {
			System.out.println(it.next());
			count++;
		}

		assertEquals(4, count);
		
	}
	
	@Test
	public void testListDirsOnly() throws Throwable {

		File baseDir = new File("target/testDirectoryListIterator/listDirsOnly");
		baseDir.mkdirs();
		File dir1 = new File(baseDir, "dir1");
		dir1.mkdirs();
		File dir2 = new File(baseDir, "dir2");
		dir2.mkdirs();
		File dira = new File(dir1, "filea");
		dira.createNewFile();
		File dirb = new File(dir2, "fileb");
		dirb.createNewFile();

		FileSystem fs = FileSystem.getLocal(new Configuration());

		DirectoryListIterator it = new DirectoryListIterator(fs, new Path(
				baseDir.getAbsolutePath()), new DirectoryOnlyFilter());

		int count = 0;
		while (it.hasNext()) {
			System.out.println(it.next().getPath().getName());
			count++;
		}

		assertEquals(2, count);
		
	}

}
