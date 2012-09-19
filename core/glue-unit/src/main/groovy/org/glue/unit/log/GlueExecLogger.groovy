package org.glue.unit.log



/**
 * 
 * Define the interface for logging of a GlueUnit's execution.<br/>
 * Each workflow is executed in a separate process and the output should be<br/>
 * logged by an implementation of this class<br/>
 * <p/>
 * Correct usage:
 * <pre>
 * def log = get GlueExecLogger
 * log.out('hi')
 * log.close()
 * <pre/>
 * A provide should be used to create a GlueExecLogger per GlueContext.
 * i.e. one GlueExecLogger should exist per context.
 */
@Typed
interface GlueExecLogger {

	/**
	 * Gets the output stream used in the logger
	 * @param key String
	 * @return OutputStream
	 */
	OutputStream getOutputStream(String key)

	/**
	 * Gets a Reader on the log file for the processName.
	 * @param processName If null the mail process file is returned
	 * @return BufferedReader
	 */
	BufferedReader getLogReader(String key)

	/**
	 * Gets the LogText between bytes tartBytes and endBytes
	 * @param processName
	 * @param startBytes
	 * @param endBytes -1 means to end of file.
	 * @return String
	 */
	 String tailLog(String processName, int maxLines)

   
	/**
	*
	* @param processName
	* @param startLine
	* @param maxLines max lines if -1 the file is read to the end
	* @return String
	*/
   String getLogText(String processName, int startLine, int maxLines)

	/**
	 * Gets the output stream used in the logger
	 * @return OutputStream
	 */
	OutputStream getOutputStream()

	/**
	 * Will set the current process name to a ThreadLocal variable
	 * @param processName
	 */
	void setCurrentThreadProcessName(String processName);

	/**
	 * The current process name in the ThreadLocal variablefor this logger
	 * @return String
	 */
	String getCurrentThreadProcessName();

	/**
	 * Not all implementations might support this method. And the value returned may be null.<br/>
	 * This is used for processes that require a physical file name to log to. 
	 * @return File
	 */
	File getLogFile();

	/**
	 * Not all implementations might support this method. And the value returned may be null.<br/>
	 * This is used for processes that require a physical file name to log to. 
	 * @param processName
	 * @return File
	 */
	File getLogFile(String processName);

	/**
	 * Std out message
	 * @param str
	 */
	void out(String str);

	/**
	 * Std out message
	 * @param processName
	 * @param str
	 */
	void out(String processName, String str);


	/**
	 * Error message
	 * @param str
	 */
	void err(String str);

	/**
	 * Error message
	 * @param processName
	 * @param str
	 */
	void err(String processName, String str);

	/**
	 * Close output for this context
	 */
	void close()
}
