package org.glue.unit.om.impl.jruby

import org.jruby.Ruby
import org.jruby.RubyProc
import org.jruby.javasupport.JavaEmbedUtils
import org.jruby.runtime.ThreadContext
import org.jruby.runtime.builtin.IRubyObject


class Closure<V> extends groovy.lang.Closure<V>{

	RubyProc obj;
	
	Closure(RubyProc v){
		super(v)
		
		println("JRuby object passed to closure: " + v.getClass().getName())
		if(v == null)
		  throw new IllegalArgumentException("Parameter for Closure cannot be null")
		  
		this.obj = v
	}
	
	def V call(Object... args){
		
		final Ruby runtime = Ruby.getGlobalRuntime() 
		final ThreadContext currentContext = runtime.getCurrentContext()
		
		final IRubyObject[] rubies = new IRubyObject[args.length]
		for(int i = 0; i < args.length; i ++){
			rubies[i] = JavaEmbedUtils.javaToRuby(runtime, args[i])
		}
		
		obj.call(currentContext, rubies)
	}
	
}