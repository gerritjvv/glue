package org.glue.modules.hadoop;

import java.util.Collection;

import org.glue.unit.om.GlueModule;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileChecksum;
import org.apache.hadoop.fs.FileStatus;

/**
 * 
 * Defines the capabilities for interactions with the Hadoop File System
 * 
 */
public interface HDFSModule extends GlueModule {

	void downloadChunked(Collection<String> hdfsDir, String localDir, Object callback)
	void downloadChunked(Collection<String> hdfsDir, String localDir, int chunkSize, String compression, Object callback)
	void downloadChunked(String clusterName, Collection<String> hdfsDir, String localDir, int chunkSize, String compression, Object callback)
	
	String cat(String hadoopPath, String fileName)
	String cat(String hadoopPath)
	
	void findNewFiles(String file, Object dirHasBeenModified, Object closure)
	void findNewFiles(String clusterName, String file, Object dirHasBeenModified, Object closure)
	
	/**
	 * Loads a local file to hdfs
	 * 
	 * @param localSource
	 * @param hdfsDest
	 */
	void put(String localSource, String hdfsDest) throws IOException;

	/**
	 * Copies a file from hdfs to the local file system
	 * 
	 * @param hdfsSource
	 * @param localDest
	 */
	void get(String hdfsSource, String localDest) throws IOException;

	/**
	 * Sets the modification and access time of a file
	 * 
	 * @param file
	 * @param mtime
	 *            modification time
	 * @param atime
	 *            last access time
	 */
	void setTimes(String file, long mtime, long atime) throws IOException;

	/**
	 * Sets the group and owner of a file
	 * 
	 * @param file
	 * @param username
	 * @param groupname
	 */
	void setOwner(String file, String username, String groupname)
			throws IOException;

	/**
	 * Sets permissions for a file
	 * 
	 * @param file
	 * @param unixStylePermissions
	 *            unix style type permissions
	 */
	void setPermissions(String file, String unixStylePermissions)
			throws IOException;

	/**
	 * Moves or renames the hdfsSource file to the hdfsDest file
	 * 
	 * @param hdfsSource
	 * @param hdfsDest
	 */
	void move(String hdfsSource, String hdfsDest) throws IOException;

	/**
	 * Removes a file, any error or if the operation returns false and
	 * IOException is thrown
	 * 
	 * @param file
	 */
	void delete(String file) throws IOException;

	/**
	 * Removes a file, any error or if the operation returns false and
	 * IOException is thrown
	 * 
	 * @param file
	 * @param recursive
	 */
	void delete(String file, boolean recursive) throws IOException;

	/**
	 * 
	 * @param file
	 * @return true if exists
	 */
	boolean exist(String file) throws IOException;

	/**
	 * Returns the hadoop content summary
	 * 
	 * @param file
	 * @return
	 */
	ContentSummary getContentSummary(String file) throws IOException;

	/**
	 * Returns the default block size
	 * 
	 * @return long
	 */
	long getDefaultBlockSize();

	/**
	 * Returns the default replication
	 * 
	 * @return short
	 */
	short getDefaultReplication();

	/**
	 * Returns the checksum for a file
	 * 
	 * @param file
	 * @return
	 */
	FileChecksum checksum(String file) throws IOException;

	/**
	 * 
	 * @param file
	 * @return true if is directory
	 */
	boolean isDirectory(String file) throws IOException;

	/**
	 * 
	 * @param file
	 * @return true if is file
	 */
	boolean isFile(String file) throws IOException;

	/**
	 * Create a directory(s)
	 * 
	 * @param file
	 * @return true if the directories could be created
	 */
	void mkdirs(String file) throws IOException;

	/**
	 * Sets the replication for a file
	 * 
	 * @param file
	 * @param replication
	 * @return
	 */
	void setReplication(String file, short replication) throws IOException;

	/**
	 * Returns a Hadoop FileStatus instance for the path
	 * 
	 * @param file
	 * @return FileStatus
	 */
	FileStatus getFileStatus(String file) throws IOException;

	/**
	 * untar a file
	 * 
	 * @param localFile
	 * @param untarDir
	 */
	void unTar(String localFile, String untarDir) throws IOException;

	/**
	 * unzip a file
	 * 
	 * @param localFile
	 * @param unzipDir
	 */
	void unZip(String localFile, String unzipDir) throws IOException;

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	FSDataInputStream open(String file) throws IOException;

	/**
	 * Opens an FSDataInputputStream and passes it to the closure
	 * 
	 * @param file
	 * @param closure
	 * @throws IOException
	 */
	void open(String file, Object closure) throws IOException;

	/**
	 * Opens the file and sends each line to the closure<br/>
	 * Default is recursive == true
	 * 
	 * @param file
	 *            Must point to a text file, this can be directory, but the
	 *            directory must contain only text files. File starting with '_'
	 *            will be ignored
	 * @param closure
	 * @throws IOException
	 */
	void eachLine(String file, Object closure) throws IOException;

	/**
	 * Opens the file and sends each line to the closure<br/>
	 * Default is recursive == true
	 * 
	 * @param file
	 *            Must point to a text file, this can be directory, but the
	 *            directory must contain only text files. File starting with '_'
	 *            will be ignored
	 * @param closure
	 * @throws IOException
	 */
	void eachLine(String file, boolean recursive, Object closure)
			throws IOException;

	/**
	 * Open an output stream to the new file created
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	FSDataOutputStream create(String file) throws IOException;

	/**
	 * Creates a file and passes the FSDataOutputStream to the closure
	 * 
	 * @param file
	 * @param closure
	 * @throws IOException
	 */
	void create(String file, Object closure) throws IOException;

	/**
	* Creates a file and passes the BufferedWriter to the closure
	* @param file
	* @param closure
	* @throws IOException
	*/
	void createWithWriter(String file, Object closure) throws IOException
	
	/**
	 * Iterate through the files in the directory specified.<br/>
	 * This is recursive by default.
	 * 
	 * @param file
	 *            can be a file glob
	 * @param closure
	 */
	void list(String file, Object closure);

	/**
	 * Iterate through the files in the directory specified.<br/>
	 * Default is recursive == true
	 * 
	 * @param file
	 *            can be a file glob
	 * @param recursive
	 *            default is true
	 * @param closure
	 */
	void list(String file, boolean recursive, Object closure);

	/**
	 * Iterate through the files in the directory specified.<br/>
	 * This is recursive by default.
	 * 
	 * @param file
	 *            can be a file glob
	 * @param lastUpdated
	 *            only files with update time bigger than this value will be
	 *            included
	 * @param closure
	 *            passed a String file name
	 */
	void list(String file, long lastUpdated, Object closure);

	/**
	 * Iterate through the files in the directory specified.
	 * 
	 * @param file
	 *            can be a file glob
	 * @param recursive
	 * @param lastUpdated
	 *            only files with update time bigger than this value will be
	 *            included
	 * @param closure
	 *            passed a String file name
	 */
	void list(String file, boolean recursive, long lastUpdated, Object closure);

}
