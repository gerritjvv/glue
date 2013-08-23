package org.glue.unit.repl.clojure

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
 * Builds clojure(ish) methods from the GlueContext and modules.
 * 
 */
class ClojureContextBuilder {

	@Typed(TypePolicy.MIXED)
	static List<String> buildClojureFunctions(GlueContext ctx){
		
		 List<String> fns = [] as ArrayList
		 
		 for(Entry<String, GlueModule> entry in ctx.getModuleFactory().getAvailableModules().entrySet()){
			String name = entry.key

			fns << "(defmacro ctx-${name} [m-name & args] `(-> glue/ctx .${name} (.~m-name ~@args) ))"	
           }
		
		   return fns
		 
	}
}
