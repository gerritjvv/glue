package org.glue.unit.repl.groovy;

import org.glue.unit.om.GlueContext
import org.glue.unit.om.ScriptRepl

import org.glue.unit.repo.GlueUnitRepository

/**
 * Runs the groovysh.
 * 
 */
public class GroovyRepl implements ScriptRepl{

	public void run(GlueUnitRepository repo, GlueContext ctx, String... cmds){

		def io = new org.codehaus.groovy.tools.shell.IO()
		def sh = new org.codehaus.groovy.tools.shell.Groovysh(
				new Binding([ctx:ctx, context:ctx, repo:repo]
				), io)

		try{
			cmds.each { line ->
				sh.execute(line)
			}

			sh.run([] as String[])
		}catch(org.codehaus.groovy.tools.shell.ExitNotification ex){
			;//ignore
		}
	}
}
