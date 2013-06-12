package org.glue.unit.om;


import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.om.GlueContext

/**
 * 
 * Different Repl(s) can implement this interface to interact with the ReplRunner
 * 
 */
public interface ScriptRepl {
	
	public void run(GlueUnitRepository repo, GlueContext ctx, String... cmds);

}
