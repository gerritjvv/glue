package org.glue.unit.repl.clojure;

import javax.script.ScriptEngineManager

import org.glue.unit.om.GlueContext
import org.glue.unit.om.ScriptRepl
import org.glue.unit.om.impl.DefaultGlueContextBuilder
import org.glue.unit.repo.GlueUnitRepository

import clojure.lang.RT
import clojure.lang.Compiler

/**
 * Runs the clojure repl.
 * 
 */
public class ClojureRepl implements ScriptRepl{

	static final ScriptEngineManager factory = new ScriptEngineManager(Thread.currentThread().getContextClassLoader());

	static {
		RT.init()
	}
	
	public void run(GlueUnitRepository repo, GlueContext ctx, String... cmds){

		// Pass the repo and ctx variables to the script engine
		GlueContext ctx1 = DefaultGlueContextBuilder.buildStaticGlueContext(ctx)
        

		RT.var("glue", "ctx", ctx1)
		RT.var("glue", "context", ctx1)
		RT.var("glue", "repo", repo)
		
		
		String str = """
			(ns glue)
			(defn help [] 
			  (prn "Glue adds three vars to interact with it")
                          (prn "glue/ctx ")
                          (prn "glue/context -- same as glue/ctx")
                          (prn "glue/repo")
			         )
               
                """
		

		Compiler.load(new StringReader(str))
		
		
		println(  RT.var("glue", "help").invoke() )
	        Compiler.load(new StringReader("(use 'clojure.main)(repl)"))
	
//		Compiler.load(new StringReader("(use 'clojure.main main/repl)"));
		
	}
}
