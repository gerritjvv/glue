package org.glue.unit.om.impl.jython

import org.python.core.PyObject


/**
 * Mask jython methods as closures
 */
@Typed
class Closure<V> extends groovy.lang.Closure<V>{

	PyObject pyObj;
	PyObject[] ctxArgs = null;
	
	Closure(PyObject v){
		this(v, null)
	}
	
	
	Closure(PyObject v, PyObject... ctxArgs){
		super(v)
		
		if(v == null)
		  throw new IllegalArgumentException("Parameter for Closure cannot be null")
		  
		
		this.pyObj = v
		if(ctxArgs != null && ctxArgs.length > 0 && ctxArgs[0] != null)
			this.ctxArgs = ctxArgs
	}
	
	def V call(Object... args){
		
		int ctxLen = (ctxArgs == null) ? 0 : ctxArgs.length
		
		PyObject[] pyObjs = new PyObject[ctxLen + args.length];
		
		int i  = 0;
		
		for(; i < ctxLen; i++)
		   pyObjs[i] = ctxArgs[i]
		
		
		for(; i < args.length + ctxLen; i++)
			pyObjs[i] = PythonContextAdaptor.derive(args[ i-ctxLen ])
			
		pyObj.__call__(pyObjs)
	}
	
}

