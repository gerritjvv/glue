package org.glue.unit.exceptions;

/**
 * 
 * This exception is never thrown but rather passed on in the<br/>
 * GParallizerUnitExecutor, when a dependency of a process has failed.
 * 
 */
public class DependencyFailedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
