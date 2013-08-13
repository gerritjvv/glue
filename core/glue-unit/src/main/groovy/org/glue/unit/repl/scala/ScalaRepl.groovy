package org.glue.unit.repl.scala;


import org.glue.unit.om.GlueContext
import org.glue.unit.om.ScriptRepl
import org.glue.unit.om.impl.DefaultGlueContextBuilder

import scala.Function1
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.NamedParamClass

/**
 * Runs the scala repl.
 * 
 */
public class ScalaRepl implements ScriptRepl{

	
	public static dorun(GlueContext ctx, String script){
		new ScalaRepl().run(ctx, script)
	}
	
	/**
	 * Runs either the scala repl or the script passed in as argument.
	 */
	public void run(GlueContext ctx, String... cmds){
		
		def settings = new Settings()
		settings.embeddedDefaults(Thread.currentThread().getContextClassLoader())
		def imain = new IMain(settings)
		imain.setContextClassLoader()
		
		imain.interpret("""
                   import scala.tools.nsc.interpreter._
                                   import scala.tools.nsc._
                   def getIMain(cls:ClassLoader) = {
                                           val s = new Settings
                       s.ignoreMissingInnerClass.value = true
                       s.usejavacp.value = true
                       s.embeddedDefaults(cls)
                       new IMain(s){
                         override protected def parentClassLoader:ClassLoader = cls
                       }
                   }
                                   val m = getIMain _
                """)
		
		def v = imain.valueOfTerm("m").get().asType(Function1).apply(
				ctx.class.classLoader)
		v.setContextClassLoader()

		
		def ctx1 = DefaultGlueContextBuilder.buildStaticGlueContextMap(ctx)
		imain.bind(new NamedParamClass("ctx", "java.util.Map[String, Object]", ctx1))
		
		v.bind(new NamedParamClass("ctx", "java.util.Map[String, Object]", ctx1))
		
		if(cmds){
			StringBuilder script = new StringBuilder()
			script.append("import scala.collection.JavaConversions._\n")
			cmds.each { script.append(it).append("\n") }
			
			v.interpret(script.toString())

		}else{
		
			//run the repl
		}
	
		
	
	}
}
