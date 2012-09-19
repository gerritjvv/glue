package org.glue.unit.log.impl


import java.io.IOException
import java.io.OutputStream

import org.glue.unit.log.GlueExecLogger

/**
 * This class uses the GlueExecLogger to select the correct OutputStream to log to.
 *
 */
@Typed
public class DistributedOutputStream extends OutputStream {

	final GlueExecLogger logger
	
	public DistributedOutputStream(GlueExecLogger logger) throws IOException {
		this.logger = logger
	}

	/**
	 * Gets the current thread name
	 * @return String Thread.currentThread().name
	 */
	public String threadId(){
		Thread.currentThread().name
	}

	@Override
	public void write(byte[] arr, int index, int len) throws IOException{
		logger.getOutputStream().write(arr, index, len)
	}

	@Override
	public void write(byte[] arr) throws IOException{
		logger.getOutputStream().write(arr)	
	}
	
	@Override
	public void write(int b) throws IOException {
		logger.getOutputStream().write(b)
	}

	/**
	 * Closes the GlueLogger
	 */
	public void close() throws IOException {
		logger.close()
	}

}
