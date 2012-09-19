package org.glue.unit.log.impl

import java.util.concurrent.ConcurrentHashMap

import org.apache.log4j.Logger
import org.glue.unit.log.GlueExecLogger
import java.util.concurrent.atomic.AtomicInteger

/**
 * 
 * A default implementation that prints output to an output stream.<br/>
 * Input Output Stream management:<br/>
 * Output Streams created are closed when the close method is called no the GlueExecLoggerImpl<br/>
 * Input Streams are not and its the responsibility of the calling code to close it always.
 *
 */
@Typed
class GlueExecLoggerImpl implements GlueExecLogger{

	private static final Logger LOG = Logger.getLogger(GlueExecLoggerImpl)

	File baseDir
	Map<String, FileOutputStream> writerMap = new ConcurrentHashMap<String, FileOutputStream>()

	static final ThreadLocal<String> currentProcessName = new ThreadLocal<String>();

	public GlueExecLoggerImpl(File baseDir) {
		super();
		this.baseDir = baseDir
	}

	/**
	 * Will set the current process name to a ThreadLocal variable
	 * @param processName
	 */
	void setCurrentThreadProcessName(String processName){
		currentProcessName.set(processName)
	}

	/**
	 * The current process name in the ThreadLocal variablefor this logger
	 * @return String
	 */
	String getCurrentThreadProcessName(){
		String name = currentProcessName.get()
		return (!name) ? 'main' : name
	}

	/**
	 * 
	 */
	public OutputStream getOutputStream(String key){
		return getOutput(key)
	}

	/**
	 * 
	 */
	public OutputStream getOutputStream(){
		return getOutput(getCurrentThreadProcessName())
	}


	/**
	 * Std out message
	 * @param processName
	 * @param str
	 */
	void out(String processName, String str){
		getOutput(processName).write(str.getBytes())
	}

	/**
	 * Std out message
	 * @param str
	 */
	void out(String str){
		out(getCurrentThreadProcessName(), str)
	}

	/**
	 * Error message
	 * @param processName
	 * @param str
	 */
	void err(String str){
		err(getCurrentThreadProcessName(), str)
	}

	/*
	 * Error message
	 * @param processName
	 * @param str
	 */
	void err(String processName, String str){
		getOutput(processName).write(str.getBytes())
	}

	/**
	 * Not all implementations might support this method. And the value returned may be null.<br/>
	 * This is used for processes that require a physical file name to log to.
	 * @return File
	 */
	File getLogFile(){
		getLogFile(null)
	}

	/**
	 * 
	 */
	public String tailLog(String processName, int maxLines){

		if(maxLines < 0){
			maxLines = Integer.MAX_VALUE
		}
		
		def file = getLogFile(processName)
		def lines = file.readLines()

		StringBuilder buff = new StringBuilder()

		int lineCount = 0
		if(lines.size() > maxLines){
			int lowerIndex = lines.size() - maxLines
			return lines.subList(lowerIndex, lines.size()).join('\n')
		}else{
			return lines.join('\n')
		}
	}

	/**
	 * 
	 * @param processName
	 * @param startLine
	 * @param max max lines if -1 the file is read to the end
	 * @return String
	 */
	public String getLogText(String processName, int startLine, int maxLines){

		def file = getLogFile(processName)
		StringBuilder buff = new StringBuilder();

		def maxLinesVal = (maxLines < 1) ? Integer.MAX_VALUE : maxLines

		final AtomicInteger index = new AtomicInteger(1);
		
		final AtomicInteger linesFound = new AtomicInteger(0);

		file.eachLine { line ->

			if(index.get() >= startLine && linesFound.get() < maxLinesVal){
				buff.append(line).append('\n')
				linesFound.getAndIncrement()
			}

			index.getAndIncrement()
		}

		return buff.toString()
	}

	/**
	 * Gets a Reader on the log file for the processName.
	 * @param processName If null the mail process file is returned
	 * @return BufferedReader
	 */
	BufferedReader getLogReader(String processName){
		if(!processName){
			processName = getCurrentThreadProcessName()
		}

		new BufferedReader(new FileReader(getLogFile(processName)))
	}

	/**
	 * Not all implementations might support this method. And the value returned may be null.<br/>
	 * This is used for processes that require a physical file name to log to.
	 * @param processName
	 * @return File
	 */
	File getLogFile(String processName){
		if(!processName){
			processName = getCurrentThreadProcessName()
		}

		new File(baseDir, processName)
	}

	/**
	 * Close output for this context
	 */
	void close(){
		writerMap.each { key, writer -> writer.flush(); writer.close() }
		writerMap.clear()
	}

	/**
	 * Gets a writer for the key
	 * @param key
	 * @return BufferedWriter
	 */
	private FileOutputStream getOutput(String key){
		def writer = writerMap[key]
		if(!writer){
			synchronized(writerMap){
				writer = writerMap[key]
				if(!writer){
					File outputFile = new File(baseDir, key)

					if(!outputFile.getParentFile().exists()){
						outputFile.getParentFile().mkdirs()
					}

					writer = new FileOutputStream(outputFile, true)
					writerMap[key] = writer
				}
			}
		}

		return writer;
	}
}
