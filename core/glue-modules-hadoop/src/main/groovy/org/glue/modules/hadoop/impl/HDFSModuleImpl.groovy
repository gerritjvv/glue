package org.glue.modules.hadoop.impl;

import static groovyx.gpars.actor.Actors.*
import groovy.time.TimeCategory
import groovyx.gpars.actor.Actor
import groovyx.gpars.dataflow.DataFlow

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.pool.impl.GenericKeyedObjectPool
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.ContentSummary
import org.apache.hadoop.fs.FSDataInputStream
import org.apache.hadoop.fs.FSDataOutputStream
import org.apache.hadoop.fs.FileChecksum
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.FileUtil
import org.apache.hadoop.fs.Path
import org.apache.hadoop.fs.PathFilter
import org.apache.hadoop.fs.permission.FsPermission
import org.apache.hadoop.io.compress.CompressionInputStream
import org.apache.log4j.Logger
import org.glue.modules.hadoop.ClosureStopException
import org.glue.modules.hadoop.HDFSModule
import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

/**
 *
 * Implements the HDFSModule using the FileSystem abstraction from hadoop.<br/>
 * This module can also be used with any other implementation of the Hadoop FileSystem API.<br/>
 * <p/>
 * Note that this module should not be a singleton because each glue unit might have its own hdfs configuration.
 *
 */
@Typed(TypePolicy.DYNAMIC)
public class HDFSModuleImpl implements HDFSModule {

	private static final Logger LOG = Logger.getLogger(HDFSModuleImpl)

	Map<String, ConfigObject> hdfsConfigurations=[:]
	String defaultConfiguration=""

	/**
	 * Each cluster has its own file systems
	 */
	Map<String, FileSystem> clusterFileSystems = new ConcurrentHashMap<String, FileSystem>()

	GenericKeyedObjectPool decompressorPool


	public HDFSModuleImpl(){
	}

	void destroy(){
		close()
	}

	public void onProcessKill(GlueProcess process, GlueContext context){
	}

	String cat(String hadoopPath){
		cat(null, hadoopPath, null)
	}

	String cat(String hadoopPath, String fileName){
		cat(null, hadoopPath, fileName)
	}
	
	
	void downloadChunked(Collection<String> hdfsDir, String localDir, Runnable callback){
		downloadChunked(hdfsDir, localDir, 1073741824, "gz", callback)
	}
	
	void downloadChunked(Collection<String> hdfsDir, String localDir, int chunkSize, String compression, Runnable callback){
		downloadChunked(null, hdfsDir, localDir, chunkSize, compression, callback)
	}
	
	void downloadChunked(String clusterName, Collection<String> hdfsDir, String localDir, int chunkSize, String compression, Runnable callback){
		
		def chunkedOutput = new ChunkedOutput(new File(localDir), "parts", chunkSize, compression, callback)
		try{
			hdfsDir.each { dir ->
			   eachLine(clusterName, dir, { String line ->
				chunkedOutput.write(line + '\n');
			})}
		}finally{
		   chunkedOutput.close()
		}
		
		
	}

	String cat(String clusterName, String hadoopPath, String fileName) {

		def f=null;
		if(!fileName) {
			f = File.createTempFile(new File("cat_"+new Date().getTime()).getName(), ".hadoopcat")
			f.deleteOnExit();
		}
		else {
			f = new File(fileName);
			if(f.exists()){
				f.delete()
			}

			f.getParentFile()?.mkdirs()
			f.createNewFile();
			f.deleteOnExit()
		}

		println "Opening ${f.getAbsolutePath()} ";

		f.withWriter { Writer writer ->
			eachLine(clusterName, hadoopPath, { String line ->
				writer.append(line).append("\n");
			});
		}

		return f.getAbsolutePath();
	}

	boolean timeSeries(String n, String tableHDFSDir, Date nowdate, String modifyTime, Callable<String> partitionFormatter, Runnable<Date> dateIncrement, Callable collector = null){
		timeSeries(null, n, tableHDFSDir, nowdate, modifyTime, partitionFormatter, dateIncrement, collector)	
	}
	
	/**
	 * 
	 * @param clusterName
	 * @param n String Amount of hours, days, weeks or months to look back at expression must be 1.days, 2.hours etc
	 * @param tableHDFSDir The table's or data's root dir just before the partitions start
	 * @param nowdate the date to start counting backwards i.e. nowdate - n
	 * @param modifyTime any valid TimeCategory expression as "20.minutes" "2.hours" etc. Files in hdfs will be checked and if they have not been modified since this time unit they are ok'ed.
	 * @param partitionFormatter Receives the date value and should return the partition path
	 * @param dateIncrement Receives the date value and should return the incremented date e.g. date + 1.hours
	 * @param collector for every file that meet the condition the closure is called with date, file
	 */
	boolean timeSeries(String clusterName, String n, String tableHDFSDir, Date nowdate, String modifyTime, Callable<String> partitionFormatter, Callable<Date> dateIncrement, Callable collector = null){
		
		final String[] modTimeParsed = modifyTime.toLowerCase().split('\\.')
		final String[] nParsed = n.toLowerCase().split('\\.')
		
		use(TimeCategory){
			final long lastUpdateTime = (nowdate - (modTimeParsed[0] as int)."${modTimeParsed[1]}" ).getTime()
			Date prevdate = (nowdate - (nParsed[0] as int)."${nParsed[1]}")
			try{
				while(prevdate <  nowdate){
					
					final String partitionPath = "$tableHDFSDir/${partitionFormatter(prevdate)}"
					int countA = 0, countB = 0
					list(clusterName, partitionPath, { file ->
						countA++ 
						if(getFileStatus(clusterName,file)?.modificationTime < lastUpdateTime){
							countB++
							if(collector) collector(prevdate, file)
						}
					})
					
					if (countA < 1 || countA != countB )
						throw new ClosureStopException("Empty partition or the partition is still edited $prevDate")
					
					//the closure must return a Date object
					prevdate = dateIncrement(prevdate)
						
				}//eof while
			}catch(ClosureStopException exc){
				println exc
				return false
			}
		}//eof use
		
		return true
	}

	void put(String localSource, String hdfsDest) throws IOException{
		put(null, localSource, hdfsDest)
	}

	/**
	 * Uses FileSystem copyFromLocalFile
	 */
	void put(String clusterName, String localSource, String hdfsDest) throws IOException{
		def fs = getFileSystem(clusterName)
		fs.copyFromLocalFile new Path(localSource), new Path(hdfsDest)
	}

	void move(String hdfsSource, String hdfsDest) throws IOException{
		move(null, hdfsSource, hdfsDest)
	}

	/**
	 * Moves or renames the hdfsSource file to the hdfsDest file
	 * @param hdfsSource
	 * @param hdfsDest
	 */
	void move(String clusterName, String hdfsSource, String hdfsDest) throws IOException{
		def fs = getFileSystem(clusterName)
		if(!fs.rename(new Path(hdfsSource), new Path(hdfsDest))){
			throw new RuntimeException("Could not move file $hdfsSource to $hdfsDest")
		}
	}

	/**
	 * untar a local file
	 * @param localFile
	 * @param untarDir
	 */
	void unTar(String localFile, String untarDir) throws IOException{
		FileUtil.unTar(new File(localFile), new File(untarDir))
	}

	/**
	 * unzip a local file
	 * @param localFile
	 * @param unzipDir
	 */
	void unZip(String localFile, String unzipDir) throws IOException{
		FileUtil.unZip(new File(localFile), new File(unzipDir))
	}

	FileStatus getFileStatus(String file) throws IOException{
		getFileStatus(null, file)
	}

	/**
	 * Returns a Hadoop FileStatus instance for the path
	 * @param file
	 * @return FileStatus
	 */
	FileStatus getFileStatus(String clusterName, String file) throws IOException{
		def fs = getFileSystem(clusterName)
		fs.getFileStatus new Path(file)
	}

	void setTimes(String file, long mtime, long atime)throws IOException{
		setTimes(null, file, mtime, atime)
	}
	/**
	 * Sets the modification and access time of a file
	 * @param file 
	 * @param mtime modification time
	 * @param atime last access time
	 */
	void setTimes(String clusterName, String file, long mtime, long atime)throws IOException{
		def fs = getFileSystem(clusterName)
		fs.setTimes new Path(file), mtime, atime
	}

	void setOwner(String file, String username, String groupname) throws IOException{
		setOwner(null, file, username, groupname)
	}
	/**
	 * Sets the group and owner of a file
	 * @param file
	 * @param username
	 * @param groupname
	 */
	void setOwner(String clusterName, String file, String username, String groupname) throws IOException{
		def fs = getFileSystem(clusterName)
		fs.setOwner(new Path(file), username, groupname)
	}

	void setPermissions(String file, String unixStylePermissions) throws IOException{
		setPermissions(null, file, unixStylePermissions)
	}

	/**
	 * Sets permissions for a file 
	 * @param file
	 * @param unixStylePermissions unix style type permissions
	 */
	void setPermissions(String clusterName, String file, String unixStylePermissions) throws IOException{
		def fs = getFileSystem(clusterName)
		fs.setPermission new Path(file), FsPermission.valueOf(unixStylePermissions)
	}

	void get(String hdfsSource, String localDest) throws IOException{
		get(null, hdfsSource, localDest)
	}
	/**
	 * Copies a file from hdfs to the local file system
	 * @param hdfsSource
	 * @param localDest
	 */
	void get(String clusterName, String hdfsSource, String localDest) throws IOException{
		def fs = getFileSystem(clusterName)
		fs.copyToLocalFile new Path(hdfsSource), new Path(localDest)
	}

	void delete(String file) throws IOException{
		delete(null, file)
	}

	/**
	 * Removes a file, any error or if the operation returns false and IOException is thrown
	 * @param file
	 */
	void delete(String clusterName, String file) throws IOException{
		delete(clusterName, file, false)
	}

	void delete(String file, boolean recursive) throws IOException{
		delete(null, file, recursive)
	}
	/**
	 * Removes a file, any error or if the operation returns false and IOException is thrown
	 * @param file
	 * @param recursive
	 */
	void delete(String clusterName, String file, boolean recursive) throws IOException{
		def fs = getFileSystem(clusterName)

		if( ! fs.delete(new Path(file), recursive) ){

			throw new IOException("Unable to delete file " + file)
		}
	}

	boolean exist(String file) throws IOException{
		exist(null, file)
	}
	/**
	 *
	 * @param file
	 * @return true if exists
	 */
	boolean exist(String clusterName, String file) throws IOException{
		def fs = getFileSystem(clusterName)
		fs.exists new Path(file)
	}

	FileChecksum checksum(String file) throws IOException{
		checksum(null, file)
	}

	/**
	 * Returns the checksum for a file
	 * @param file
	 * @return
	 */
	FileChecksum checksum(String clusterName, String file) throws IOException{
		getFileSystem(clusterName).getFileChecksum(new Path(file))
	}

	ContentSummary getContentSummary(String file) throws IOException{
		getContentSummary(null, file)
	}

	/**
	 * Returns the hadoop content summary
	 * @param file
	 * @return
	 */
	ContentSummary getContentSummary(String clusterName, String file) throws IOException{
		getFileSystem(clusterName).getContentSummary(new Path(file))
	}

	/**
	 * Returns the default block size
	 * @return long
	 */
	long getDefaultBlockSize(String clusterName = null){
		getFileSystem(clusterName).getDefaultBlockSize()
	}


	/**
	 * Returns the default replication
	 * @return short
	 */
	short getDefaultReplication(String clusterName = null) {
		getFileSystem(clusterName).getDefaultReplication()
	}


	boolean isDirectory(String file) throws IOException{
		isDirectory(null, file)
	}

	/**
	 *
	 * @param file
	 * @return true if is directory
	 */
	boolean isDirectory(String clusterName, String file) throws IOException{
		getFileSystem(clusterName).getFileStatus(new Path(file)).isDir()
	}

	boolean isFile(String file)throws IOException{
		isFile(null, file)
	}
	/**
	 *
	 * @param file
	 * @return true if is file
	 */
	boolean isFile(String clusterName, String file)throws IOException{
		!isDirectory(clusterName, file)
	}

	void mkdirs(String file) throws IOException{
		mkdirs(null, file)
	}

	/**
	 * Create a directory(s)
	 * @param file
	 * @return true if the directories could be created
	 */
	void mkdirs(String clusterName, String file) throws IOException{
		if( ! getFileSystem(clusterName).mkdirs(new Path(file)) ){
			throw new IOException("Unable to create directory $file")
		}
	}

	void setReplication(String file, short replication){
		setReplication(null, replication)
	}

	/**
	 * Sets the replication for a file
	 * @param file
	 * @param replication
	 * @return
	 */
	void setReplication(String clusterName, String file, short replication) throws IOException{
		if(! getFileSystem(clusterName).setReplication(new Path(file), replication) ){
			throw new IOException("Unable to set replication to " + replication)
		}
	}

	void withDecompressedInputStream(String file, Runnable closure) throws IOException{
		withDecompressedInputStream(null, file, closure)
	}

	void withDecompressedInputStream(String clusterName, String file, Runnable closure) throws IOException{
	
			def key = new DecompressorKey(clusterName:clusterName, file:file)
			DecompressorValue decVal = decompressorPool.borrowObject(key)

			if(decVal == null){
				FSDataInputStream input = open(clusterName, file)
				try{
					closure(input)
				}finally{
					input.close()
				}
			}else{
				try{
					
					CompressionInputStream input = decVal.createInputStream(open(clusterName, file))
					try{
						closure(input)	
					}finally{
						input.close()
					}
				}finally{
					decompressorPool.returnObject(key, decVal)
				}
			}
	
	}
	

	FSDataInputStream open(String file) throws IOException{
		open(null, file)
	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	FSDataInputStream open(String clusterName, String file) throws IOException{
		getFileSystem(clusterName).open(new Path(file))
	}


	void open(String file, Runnable closure) throws IOException{
		open(null, file, closure)
	}
	/**
	 * Opens an FSDataInputputStream and passes it to the closure
	 * @param file
	 * @param closure
	 * @throws IOException
	 */
	void open(String clusterName, String file, Runnable closure) throws IOException{
		FSDataInputStream input = open(clusterName, file)
		try{
			closure.call(input)
		}finally{
			input.close()
		}
	}


	FSDataOutputStream create(String file) throws IOException{
		create(null, file)
	}

	/**
	 * Open an output stream to the new file created
	 * @param file
	 * @return
	 * @throws IOException
	 */
	FSDataOutputStream create(String clusterName, String file) throws IOException{
		getFileSystem(clusterName).create (new Path(file))
	}

	void createWithWriter(String file, Runnable closure) throws IOException{
		createWithWriter(null, file, closure)
	}

	/**
	 * Creates a file and passes the BufferedWriter to the closure
	 * @param file
	 * @param closure
	 * @throws IOException
	 */
	void createWithWriter(String clusterName, String file, Runnable closure) throws IOException{
		FSDataOutputStream fsDataOut = getFileSystem(clusterName).create (new Path(file))
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fsDataOut))

		try{
			closure(writer)
		}finally{
			writer.close()
			fsDataOut.close()
		}
	}

	void create(String file, Runnable closure) throws IOException{
		create(null, file, closure)
	}

	/**
	 * Creates a file and passes the FSDataOutputStream to the closure
	 * @param file
	 * @param closure
	 * @throws IOException
	 */
	void create(String clusterName, String file, Runnable closure) throws IOException{
		FSDataOutputStream fsDataOut = getFileSystem(clusterName).create (new Path(file))
		try{
			closure(fsDataOut)
		}finally{
			fsDataOut.close()
		}
	}

	void eachLine(String file, Runnable closure) throws IOException{
		eachLine(null, file, closure)
	}

	/**
	 * Opens the file and sends each line to the closure<br/>
	 * This is recursive by default.
	 * @param file Must point to a text file
	 * @param closure
	 * @throws IOException
	 */
	void eachLine(String clusterName, String file, Runnable closure) throws IOException{
		eachLine(clusterName, file, true, closure)
	}

	void eachLine(String file, boolean recursive, Runnable closure) throws IOException{
		eachLine(null, file, recursive, closure)
	}

	/**
	 * Opens the file and sends each line to the closure<br/>
	 * This is recursive by default.
	 * @param file Must point to a text file
	 * @param recursive
	 * @param closure
	 * @throws IOException
	 */
	void eachLine(String clusterName, String file, boolean recursive, Runnable closure) throws IOException{
		def eachLineFile = {

			DecompressorValue decVal
			
			def key = new DecompressorKey(clusterName:clusterName, file:it)
			try{
			decVal = decompressorPool.borrowObject(key)
			}catch(Throwable t){
			  throw new RuntimeException("Error creating decompressor: " + file, t)
			}
			
			if(decVal == null){
				LOG.debug("Reading $file as plain text")
				def reader = new BufferedReader(new InputStreamReader( open(clusterName, it as String)))
				try{
					reader.eachLine(closure)
				}finally{
					reader.close()
				}
			}else{
				try{
					LOG.debug("Reading $file using ${decVal.codec}")

					CompressionInputStream input = decVal.createInputStream(open(clusterName, it as String))
					BufferedReader reader = new BufferedReader(new InputStreamReader(input))
					try{

						try{
							reader.eachLine {  line -> closure(line)  }
						}catch(NullPointerException npe){
							//end of stream was reached.
							//this error is given by the compression codec when end of stream
							//has been reached before an end of line character.
							//for our purposes this means end of line and file.
						}
					}finally{
						reader.close()
						input.close()
					}
				}catch(Throwable t){
				    throw new RuntimeException("Error creating inputstream for: " + file  + " " + to.toString(), t);
				}finally{
					decompressorPool.returnObject(key, decVal)
				}
			}
		}

		if(isGlob(clusterName, file) || isDirectory(clusterName, file)){

			list(clusterName, file, recursive, { eachLineFile(it) } )
		}else{
			eachLineFile(file)
		}
	}

	void list(String file, Runnable closure){
		list(null, file, closure)
	}

	/**
	 * Iterate through the files in the directory specified.<br/>
	 * This is recursive by default.
	 * 
	 * @param file
	 *            can be a file glob
	 * @param closure
	 */
	void list(String clusterName, String file, Runnable closure){
		list(clusterName, file, true, closure)
	}

	void list(String file, boolean recursive, Runnable closure){
		list(null, file, recursive, closure)
	}

	/**
	 * This method will go through new files those are files where the fileIsNew closure returns true.<br/>
	 * A simple logic is used to not traverse directories where the directory or its sub directories has not been modified.<br/>
	 * If dirHasBeenModified returns false on a directory, that directory and its sub directories are skipped.
	 * @param clusterName
	 * @param file
	 * @param dirHasBeenModified
	 * @param closure
	 */
	void findNewFiles(String clusterName = null, String file, Callable<Boolean> dirHasBeenModified, Runnable closure){
		
		def findClosure = null
		findClosure = { path, FileStatus status ->
			   //if it is a directory, and the directory hasBeenModified returns true
			   //then call the list method recursively
					
			   if(status.isDir() && dirHasBeenModified(status)){
				 list(clusterName, path, false, findClosure)  
			   }else{
			      //closure should decide if the file is new or not
			   	  closure(status)
			   }
			
		}
		
		list(clusterName, file, false, findClosure)
		
	}

	/**
	 * Iterate through the files in the directory specified.<br/>
	 * This is recursive by default.
	 * @param file can be a file glob
	 * @param recursive default is true
	 * @param closure
	 */
	void list(String clusterName, String file, boolean recursive, Runnable closure){
		PathFilter pathFilter =  { Path path ->
			!path.name.startsWith("_")
		} as PathFilter

		def listStatus

		def fs = getFileSystem(clusterName)

		if(isGlob(clusterName, file)){
			//use globing
			listStatus = {
				fs.globStatus(it as Path, pathFilter)
			}
		}else{
			listStatus = {
				fs.listStatus(it as Path, pathFilter)
			}
		}

		AtomicBoolean hasStop = new AtomicBoolean(false)

		listStatus(new Path(file))?.each { FileStatus status ->
			if(hasStop.get()){
				return
			}

			try{
				if(status.isDir()){
					if(recursive)
						listStatusRecursiveHelper(clusterName, status, pathFilter, closure)
					else{
						
						if(closure.getMaximumNumberOfParameters() > 1){
							closure.call(status.path.toUri().toString(), status)
						}else{
							closure.call(status.path.toUri().toString())
						}
					}
					
					
				}else{
					if(closure.getMaximumNumberOfParameters() > 1){
						closure.call(status.path.toUri().toString(), status)
					}else{
						closure.call(status.path.toUri().toString())
					}
				}
			}catch(ClosureStopException exc){
				hasStop.set(true)
			}
		}
	}

	void list(String file, long lastUpdated, Runnable closure){
		list(null,file, lastUpdated,  closure)
	}

	/**
	 * Iterate through the files in the directory specified.<br/>
	 * This is recursive by default.
	 * @param file can be a file glob
	 * @param closure passed a String file name
	 * @param lastUpdated only files with update time bigger or equal than this value will be included
	 */
	void list(String clusterName, String file, long lastUpdated, Runnable closure){
		list(clusterName, file, true, lastUpdated, closure)
	}

	void list(String file, boolean recursive, long lastUpdated, Runnable closure){
		list(null, file, recursive, lastUpdated, closure)
	}

	/**
	 * Iterate through the files in the directory specified.<br/>
	 * This is recursive by default.
	 * @param file can be a file glob
	 * @param closure passed a String file name, FileStatus
	 * @param recursive
	 * @param lastUpdated only files with update time bigger or equal than this value will be included
	 */
	@Typed(TypePolicy.MIXED)
	void list(String clusterName, String file, boolean recursive, long lastUpdated, Runnable closure){

		Runnable clsToCall = closure

		if(closure.getMaximumNumberOfParameters() == 1){
			clsToCall = { f, m ->
				closure(f)
			}
		}
		def out=[];
		AtomicInteger count = new AtomicInteger(0)

		//note on performance
		//its important that we use the fileStatus and not do fs.getFileStatus
		//this call incurs another expensive cost and deters performance
		list clusterName, file, recursive, { String fileName, FileStatus fileStatus ->
			//			long start = System.currentTimeMillis()
			//----- Start
			long modificationTime = fileStatus?.modificationTime
			///--- takes as long to do as a call to clsToCall.call
			//			println "HdfsModuleImpl,list(file,recursive,lastUpdated,closure)-AA,${System.currentTimeMillis()-start}"

			if(lastUpdated < modificationTime){

				out << ['m': modificationTime, 'f':fileName, 's':fileStatus];
				count.incrementAndGet()
			}
		}

		println("Found ${count.get()} elements");
		count.set(0)
		out.findAll({a -> a!=null}).sort({ a,b -> a['m'] <=> b['m'] }).each{ it->
			clsToCall.call(it['f'], it['s']);
			count.incrementAndGet()
		}
		println("returned ${count.toString()} elements");


	}


	/**
	 * Helper for iterating through a directory recursively 
	 * @param dirStatus
	 * @param filter
	 * @param closure
	 */
	private void listStatusRecursiveHelper(String clusterName, FileStatus dirStatus, PathFilter filter, Runnable closure){

		List<Actor> actors = []
		FileSystem fs = getFileSystem(clusterName)
		AtomicBoolean isStop = new AtomicBoolean(false)

		fs.listStatus(dirStatus.path, filter).each { FileStatus status ->
			if(status.isDir()){
				//note on performance
				//its important that we use the fileStatus and not do fs.getFileStatus
				//this call incurs another expensive cost and deters performance
				if(isStop.get()){
					return
				}

				actors << DataFlow.start {
					try{
						listStatusRecursiveHelper(clusterName, status, filter, closure)
					}catch(ClosureStopException excp){
						isStop.set(true)
					}
				}
			}else{
				if(closure.getMaximumNumberOfParameters() > 1){
					closure.call(status.path.toUri().toString(), status)
				}else{
					closure.call(status.path.toUri().toString())
				}
			}
		}

		actors.each { Actor actor -> actor.join() }
	}

	private static boolean isGlob(String clusterName = null, String file){
		file.find("[\\[\\]\\^\\{\\}\\?\\*]") != null
	}

	@Override
	public Boolean canProcessRun(GlueProcess process, GlueContext context) {
		return true;
	}

	@Override
	public void configure(String unitId, ConfigObject config) {
		//		if(config.defaultCluster){
		//			this.defaultConfiguration=config.defaultCluster.toString()
		//
		//			//close previous file system instance
		//			if(fs){
		//				fs.close()
		//			}
		//
		//			fs = FileSystem.get(getHdfsConf())
		//		}
	}

	/**
	 * Returns the correct hadoop FileSystem.<br/>
	 * The clusterName must match that of a cluster specified in the hdfs config
	 * @param clusterName
	 * @return FileSystem
	 */
	FileSystem getFileSystem(String clusterName){

		if(!clusterName){
			clusterName = defaultConfiguration
		}

		FileSystem fs = clusterFileSystems[clusterName]

		if(!fs){

			synchronized (clusterFileSystems) {
				fs = clusterFileSystems[clusterName]
				if(!fs){
					fs = FileSystem.get(getHdfsConf())
					clusterFileSystems[clusterName] = fs
				}
			}
		}

		return fs
	}


	@Override
	public String getName() {
		return "hdfs";
	}


	@Override
	public void init(ConfigObject config) {

		if(!config.clusters) {
			println "Can't find any clusters in config!"
		}

		config.clusters.each { String key, ConfigObject c ->

			print "loading cluster $key"
			if(c.isDefault) {
				defaultConfiguration=key;
			}
			hdfsConfigurations[key]=c.hdfsProperties
			println "Loaded $key as ${c.hdfsProperties}"

			if(!c.hdfsProperties){
				throw new ModuleConfigurationException("No property hdfsProperties defined for HdfsModule in $key")
			}
		}

		if(defaultConfiguration == null){
			throw new ModuleConfigurationException("No default configuration was specified for the hdfs module configuration $config")
		}

		//creates a default compressor pool that contains at most 100 decompressors, and will
		//block if none are available for up to 60 seconds.
		decompressorPool = new GenericKeyedObjectPool(new  DecompressorPoolableObjectFactory(defaultConfiguration, hdfsConfigurations),
				100, GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK, 60000L, -1)
	}

	@Override
	public void onProcessFail(GlueProcess process, GlueContext context,
	Throwable t) {
	}

	@Override
	public void onProcessFinish(GlueProcess process, GlueContext context) {
	}

	@Override
	public void onProcessStart(GlueProcess process, GlueContext context) {
	}

	@Override
	public void onUnitFail(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {
	}

	public void close(){

		clusterFileSystems.each { FileSystem fs -> fs?.close() }
	}

	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
	}



	private Configuration getHdfsConf(String configName=null) {
		//Loading hdfsConfig
		println "Loading HdfsModule Configuration form scratch $configName"
		try{

			Properties props = new Properties();

			def hdfsPropFileName= (configName) ? hdfsConfigurations[configName] : null;

			if(!hdfsPropFileName) hdfsPropFileName=hdfsConfigurations[defaultConfiguration];

			if(!hdfsPropFileName){
				throw new ModuleConfigurationException("Cannot find file  $hdfsPropFileName please make sure a default cluster is defined")
			}

			File myFile = new File(hdfsPropFileName.toString())
			if(!myFile.exists()){
				throw new ModuleConfigurationException("Cannot find file  $hdfsPropFileName")
			}

			new File (hdfsPropFileName.toString()).withInputStream { InputStream input ->
				props.load(input);
			}


			Configuration conf = new Configuration();
			props.each { String key, String val ->
				conf.set( key, val)
			}

			return conf
		}catch(Throwable t){
			throw new ModuleConfigurationException(t.toString(), null, t)
		}
	}

	@Override
	public Map getInfo() {
		Map<String, String> info = [:]

		Configuration conf = getHdfsConf()
		if(conf){
			for(Map.Entry<String, String> entry in conf){

				info[entry.key] = entry.value
			}
		}

		return info
	}
}
