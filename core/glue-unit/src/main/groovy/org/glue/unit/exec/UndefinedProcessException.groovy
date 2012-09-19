package org.glue.unit.exec

/**
 *
 * Thrown when a process is not defined
 *
 */
@Typed
class UndefinedProcessException extends RuntimeException{

	public UndefinedProcessException() {
		super();
	}

	public UndefinedProcessException(String msg, Throwable t) {
		super(msg, t);
	}

	public UndefinedProcessException(String msg) {
		super(msg);
	}

	public UndefinedProcessException(Throwable t) {
		super(t);
	}

	
	
}
