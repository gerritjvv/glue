package org.glue.unit.exceptions;

/**
 * Thrown to indicate a stop in the process.<br/>
 * Does not indicate an error.
 *
 */
@Typed
public class UnitSubmissionException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnitSubmissionException(){}
	public UnitSubmissionException(String msg){ super(msg); }
	public UnitSubmissionException(Throwable t){ super(t); }
	public UnitSubmissionException(String msg, Throwable t){ super(msg,t); }
	
}
