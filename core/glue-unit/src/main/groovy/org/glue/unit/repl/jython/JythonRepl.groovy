package org.glue.unit.repl.jython;

import org.glue.unit.om.impl.jython.PythonContextAdaptor
import org.glue.unit.om.GlueContext
import org.glue.unit.om.ScriptRepl
import org.glue.unit.om.impl.DefaultGlueContextBuilder

import org.glue.unit.repo.GlueUnitRepository
import org.python.util.InteractiveInterpreter;

import org.python.core.PySystemState
import org.python.util.InteractiveConsole

/**
 * Runs the jython repl.
 * 
 */
public class JythonRepl implements ScriptRepl{

	public void run(GlueUnitRepository repo, GlueContext ctx, String... cmds){

		//		PySystemState.initialize(null, null, [""], null,
		//			new PythonContextAdaptor())
		//
		def repo1 = PythonContextAdaptor.derive(repo)
		def ctx1 = PythonContextAdaptor.derive(DefaultGlueContextBuilder.buildStaticGlueContext(ctx))

		InteractiveConsole.initialize(System.getProperties(), null, new String[0] );
		
		def sh = new InteractiveConsole()
		sh.set("ctx", ctx1)
		sh.set("repo", repo1)
		sh.set("context", ctx1)
		
		boolean exit = false
		cmds.each { line ->
			if(line == "exit" || line == "exit()") {
		      exit = true
		    }else{
				sh.push(line)
			}
		}
	    if(!exit)
		  sh.interact()
		
	
	}
}
