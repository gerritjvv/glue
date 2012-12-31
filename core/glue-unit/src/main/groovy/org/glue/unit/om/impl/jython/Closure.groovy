package org.glue.unit.om.impl.jython

import org.python.core.PyObject


/**
 * Mask jython methods as closures
 */
@Typed
class Closure<V> extends groovy.lang.Closure<V>{

	PyObject pyObj;
	
	Closure(PyObject v){
		super(v)
		
		if(v == null)
		  throw new IllegalArgumentException("Parameter for Closure cannot be null")
		  
		this.pyObj = v
	}
	
	def V call(Object... args){
		PyObject[] pyObjs = new PyObject[args.length];
		
		for(int i = 0; i < args.length; i++){
			pyObjs[i] = PythonContextAdaptor.derive(args[i])
		}	
			
		pyObj.__call__(pyObjs)
	}
	
}

