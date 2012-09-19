package org.glue.unit.process.util

import groovy.lang.Closure;

/**
 * A simple utility appendable implementation, that takes a closure
 * and all the appendable methods when called with result in the call to the
 * closure, passing a String argument.
 *
 */
@Typed
class ClosureAppendable extends OutputStream implements Appendable{

	Closure closure


	void flush(){

	}
	void close(){

	}
	void write(byte[] b){
		closure.call(new String(b))
	}

	void write(byte[] b, int off, int len){
		closure.call(new String(b, off, len))
	}

	void write(int b){
		closure.call(b)
	}

	/**
	 * 
	 * @param closure
	 */
	public ClosureAppendable(Closure closure) {
		super();
		this.closure = closure;
	}

	@Override
	Appendable	append(char c){
		closure.call(String.valueOf(c))
	}

	@Override
	Appendable	append(CharSequence csq){
		closure.call(csq.toString())
	}

	@Override
	Appendable	append(CharSequence csq, int start, int end){

		closure.call(csq.subSequence(start,end).toString())
	}
}