package org.glue.unit.exceptions;

/**
 * Thrown to indicate a stop in the process.<br/>
 * Does not indicate an error.
 *
 */
@Typed
public class ProcessStopException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcessStopException(String msg){ super(msg); }
	
}
