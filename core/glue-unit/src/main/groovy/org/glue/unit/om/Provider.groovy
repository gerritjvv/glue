package org.glue.unit.om

/**
 * 
 * Generic provider interface. Rather than having multiple factory interfaces running around, <br/>
 * we just provide one single concise interface.
 * 
 */
@Typed
abstract class Provider<T> {

	
	/**
	 * Provides an instance
	 * @return
	 */
	abstract T get()
	
	/**
	 * Provides an instance with the option of sending arguments to it
	 * @param args
	 * @return
	 */
	T get(Object... args){
		get()
	}
}
