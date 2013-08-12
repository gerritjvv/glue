package org.glue.unit.repl.scala

import java.util.Map.Entry

import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.GlueUnit
import org.glue.unit.status.GlueUnitStatusManager

/**
 * 
 * Dynamically construct the scala source code that represents the glue context.
 * The context itself is represented as an Object
 * 
 */
class ScalaGlueContextBuilder {

	@Typed(TypePolicy.MIXED)
	static String buildStaticGlueContext(GlueContext ctx){

		String s = """
		   import _root_.org.glue.unit.om.GlueContext
           import _root_.org.glue.unit.om.impl.GlueContextWrapper

		   class GlueContextStatic(parent:GlueContext) extends GlueContextWrapper(parent){
            
		 	
         """


		for(Entry<String, GlueModule> entry in ctx.getModuleFactory().getAvailableModules().entrySet()){
			String name = entry.key
			GlueModule module = entry.value
			String clsName
			if(module.getClass().name.endsWith("GlueModuleProxy_delegateProxy"))
				clsName = module.module.getClass().name
			else
				clsName = module.getClass().name

			s += """

				
				def ${name}:$clsName = {
                  	val module = parent.getModuleFactory().getModule(\"${name}\")

					if(module.getClass().getName().endsWith("GlueModuleProxy_delegateProxy"))
						return module.getModule()
					else
                        return module

                } 

			"""
			
		}

		s += """
               
           }

		   
			def getCtx(parent:GlueContext) = new GlueContextStatic(parent)
			val get_ctx = getCtx _
		 """

	   return s
	}

	
}
