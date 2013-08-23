package org.glue.unit.repl.clojure;


import org.glue.unit.om.GlueContext
import org.glue.unit.om.ScriptRepl
import org.glue.unit.om.impl.DefaultGlueContextBuilder

import clojure.lang.RT
import clojure.lang.Compiler

/**
 * Runs the clojure repl.
 * 
 */
@Typed
public class ClojureRepl implements ScriptRepl{

	static {
		RT.init()
	}
	
	public static dorun(GlueContext ctx, String script){
		new ClojureRepl().run(ctx, script)
	}
	
	/**
	 * Runs either the clojure repl or the script passed in as argument.
	 */
	public void run(GlueContext ctx, String... cmds){
		
		//here the ctx.unit.name is already repl
		
		GlueContext ctx1 = DefaultGlueContextBuilder.buildStaticGlueContext(ctx)
        

		RT.var("glue", "ctx", ctx1)
		RT.var("glue", "context", ctx1)
		
		
		StringBuilder str = new StringBuilder("""
			(ns glue)
			(defn help [] 
			  (prn "Glue adds three vars to interact with it")
                          (prn "glue/ctx ")
                          (prn "glue/context -- same as glue/ctx")
			         )
               
                """)
		
		ClojureContextBuilder.buildClojureFunctions(ctx).each { fn -> str.append(fn).append("\n") }

		println "Loading ${str.toString()}"
	
		Compiler.load(new StringReader(str.toString()))
		
		
		println(  RT.var("glue", "help").invoke() )
		if(cmds){
		  StringBuilder script = new StringBuilder()
		  cmds.each { script.append(it) }
		  Compiler.load(new StringReader(script.toString()))
		  
		}else
	      reply.ReplyMain.main()
		  //Compiler.load(new StringReader("(use 'replay.main)(launch-nrepl)"))
	      //Compiler.load(new StringReader("(use 'clojure.main)(repl)"))
	
//		Compiler.load(new StringReader("(use 'clojure.main main/repl)"));
		
	}
}
