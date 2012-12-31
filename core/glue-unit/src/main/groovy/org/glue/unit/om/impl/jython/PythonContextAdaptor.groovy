package org.glue.unit.om.impl.jython

import org.python.core.PyObject
import org.python.core.PyObjectDerived
import org.python.core.PyType
import org.python.core.adapter.ClassicPyObjectAdapter

/**
 * Initialize the Python Interpreter with the custom PythonContextAdaptor.<br/>
 * The problem is that all groovy interfaces when the method getDeclaredClasses is called<br/>
 * return a strange [Name]$1 class, which does not exist and cause a ClassNotFoundException in Java.<br/>
 * The PythonContextAdaptor use a work-around<br/>
 * <p/>
 * The work-around is:
 * <code>
 * def obj = new PyObjectDerived(PyType.fromClassSkippingInners(o.getClass(), new HashSet()))
 obj.javaProxy = o
 return obj
 * </code>
 */
class PythonContextAdaptor extends ClassicPyObjectAdapter{

	public static PyObjectDerived derive(Object o){
		def obj = new PyObjectDerived(PyType.fromClassSkippingInners(findClass(o), new HashSet()))

		obj.javaProxy = o
		return obj
	}

	public PyObject adapt(Object o) {
		try{
			return super.adapt(o)
		}catch(Throwable excp){
			//workaround to the class not found exception is to sip the inner classes
			def obj = new PyObjectDerived(PyType.fromClassSkippingInners(findClass(o), new HashSet()))

			obj.javaProxy = o
			return obj
		}
	}


	private static final Class<?> findClass(Object obj){
		Class cls
		try{
			cls = obj.getClass()
		}catch(MissingMethodException excp){
			//this is a dynamic object with dynamic methods, and for some reason its not
			//realizing we want to get the class name
			cls = obj.getMetaClass().methods.find { it.name == "getClass" }.doMethodInvoke(obj, null)
			
		}
		return cls
	}

}
