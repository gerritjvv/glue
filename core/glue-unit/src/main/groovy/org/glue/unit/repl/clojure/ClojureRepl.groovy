package org.glue.unit.repl.clojure;

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

import org.glue.unit.om.impl.jython.PythonContextAdaptor
import org.glue.unit.om.GlueContext
import org.glue.unit.om.ScriptRepl
import org.glue.unit.om.impl.DefaultGlueContextBuilder

import org.glue.unit.repo.GlueUnitRepository
import org.python.util.InteractiveInterpreter;

import org.python.core.PySystemState
import org.python.util.InteractiveConsole

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

/**
 * Runs the clojure repl.
 * 
 */
public class ClojureRepl implements ScriptRepl{

	static final ScriptEngineManager factory = new ScriptEngineManager(Thread.currentThread().getContextClassLoader());

	public void run(GlueUnitRepository repo, GlueContext ctx, String... cmds){

		// Pass the repo and ctx variables to the script engine
		ScriptEngine engine = factory.getEngineByName("Clojure");
		GlueContext ctx1 = DefaultGlueContextBuilder.buildStaticGlueContext(ctx)

		def bindings = engine.createBindings()
		bindings.put("user/context", ctx1)
		bindings.put("user/ctx", ctx1)
		bindings.put("user/repo", repo)

		

		cmds.each { line ->
				engine.eval(line)
		}

		engine.eval("(use 'clojure.main)(repl)")
		
		
	}
}
