
package org.glue.unit.om.impl

import org.glue.unit.exceptions.UnitValidationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModuleFactory;
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitValidator

/**
 * 
 * Default GlueUnit validator that checks that the required modules are available in the module factory
 *
 */
@Typed
class DefaultGlueUnitValidator implements GlueUnitValidator{

	/**
	* If the GlueUnit should not run this method must throw a UnitValidationException exception
	* @param unit
	* @param context
	*/
   void validate(GlueUnit unit, GlueContext context)throws UnitValidationException{
	   
	    Collection<String> requiredModules = unit.requiredModules
		
		if(requiredModules){
			//validate that the required modules are available
			GlueModuleFactory moduleFactory = context.moduleFactory
		   	
			def modulesNotFound = []
			
			requiredModules?.each { String module ->
				
				if(!moduleFactory?.getModule(module)){
					modulesNotFound << module
				}	
				
			}
			
			if(requiredModules){
				throw new UnitValidationException(unit, context, "The modules ${requiredModules.join(',')} are not available", null)
			}
		}
		   
		
   }
   
	
}
