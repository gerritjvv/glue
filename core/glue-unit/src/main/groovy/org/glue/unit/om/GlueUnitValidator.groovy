package org.glue.unit.om

import org.glue.unit.exceptions.UnitValidationException

/**
 * 
 * This interface is a simple abstraction that allows us to plugin pre GlueUnit execution validation.<br/>
 * We can add things like:<br/>
 * <ul>
 *  <li>Required modules are available</li>
 *  <li>User access e.g. ensure that the user is allowed to execute the query etc.<br/>
 * </ul>
 * 
 *
 */
@Typed
interface GlueUnitValidator {

	/**
	 * If the GlueUnit should not run this method must throw a UnitValidationException exception
	 * @param unit
	 * @param context
	 */
	void validate(GlueUnit unit, GlueContext context)throws UnitValidationException;
	
}
