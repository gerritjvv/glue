package org.glue.unit.om

import java.util.concurrent.Callable

import org.jruby.RubyProc
import org.python.core.PyObject

import clojure.lang.ArraySeq
import clojure.lang.IFn

/**
 * 
 * Help with calls that need to be made to either Closures, Runnable, Callable, Jython, Ruby, Clojure objects etc.
 * 
 * Closure functions are all Runnable and Callable.
 */
@Typed
final class CallHelper {

	/**
	 * 
	 * @param obj
	 * @param arg
	 * @return
	 */
	public static final Closure makeCallable(final Object obj){
		if(obj instanceof groovy.lang.Closure){
			return obj
		}else if(obj instanceof PyObject){
			def cls = new org.glue.unit.om.impl.jython.Closure((PyObject)obj)
			return { Object[] args -> cls.call(args)}
		}else if(obj instanceof IFn){
			return { Object[] args -> ((IFn)obj).applyTo(ArraySeq.create(args)) }
		}else if( obj instanceof RubyProc){
			def cls = new org.glue.unit.om.impl.jruby.Closure((RubyProc)obj)
			return { Object[] args -> cls.call(args) }
		}else if(obj instanceof Callable){
		    return { Object[] args -> ((Callable)obj).call() }
		}else if(obj instanceof Runnable){
		    return { Object[] args -> ((Runnable)obj).run() }
		}else{
		    throw new IllegalArgumentException("Argument must be a IFn, PyObject, Closure, Runnable or Callable but instead is: " + obj)
		}
	}

}

