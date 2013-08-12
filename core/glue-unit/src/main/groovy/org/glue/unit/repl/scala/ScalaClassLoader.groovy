package org.glue.unit.repl.scala

import java.lang.reflect.Method

@Typed
class ScalaClassLoader extends java.lang.ClassLoader{

	final ClassLoader parent
	
	public ScalaClassLoader(ClassLoader parent){
		super(parent)
		this.parent = parent
		
	}
	
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		parent.loadClass(parseName(name))
	}

	private String parseName(String name) {
		int len = name.length()
		String clsName = name
		
		
		if(name.charAt(len-1) == '$')
			clsName = name.substring(0, len-1)
		else if(name.charAt(len-2 ) == '$')
		    clsName = name.substring(0, len-2)
		
			
	    if(name.contains('$'))
			 println "!!!!!! Class: $name returning : $clsName"
			 
		return clsName
	}
	
}
