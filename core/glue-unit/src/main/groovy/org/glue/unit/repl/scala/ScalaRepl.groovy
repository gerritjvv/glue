package org.glue.unit.repl.scala;


import org.glue.unit.om.GlueContext
import org.glue.unit.om.ScriptRepl
import org.glue.unit.om.impl.DefaultGlueContextBuilder

import scala.Function0
import scala.Function1
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain

import scala.tools.nsc.interpreter.Results
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
						 override lazy val formatting = new Formatting {
						      def prompt = scala.tools.nsc.Properties.shellPromptString
						 }
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
			
			def res = v.interpret(script.toString())
			if(!res.class.name.contains('Success'))
				throw new RuntimeException("error while runnin scala script res: " + res)
		}else{
		
			imain.bind(new NamedParamClass("imain", "scala.tools.nsc.interpreter.IMain", v))
			
			imain.interpret("""

					import scala.collection.JavaConversions._

					def getILoop(s:Settings) = { 
                                 new ILoop(){
								  override def createInterpreter() {
								    if (addedClasspath != "")
								      settings.classpath append addedClasspath
								
								    intp = imain
								  }
								}.process(s)
                    }
			        val iloop = getILoop _

			""")
			//run the repl		
			imain.valueOfTerm("iloop").get().asType(Function1).apply(v.settings())
		}
	
		
	
	}
}
