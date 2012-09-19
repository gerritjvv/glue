package org.glue.unit.exceptions

import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueUnit


/**
 *
 * Exception is thrown when the GlueUnitValidator cannot validate the GlueUnit.<br/>
 * i.e. for what ever reason in the validation rules the GlueUnit should not run.
 *
 */
@Typed
class UnitValidationException extends UnitSubmissionException{

	GlueUnit unit
	GlueContext context
	String msg
	
	public UnitValidationException(GlueUnit unit, GlueContext context, String msg, Throwable t) {
		super(msg, t);
		this.unit = unit
		this.context = context
		this.msg = msg
	}

	
	String toString(){
		
		String reason = getCause()?.getMessage()
		String contextStr = context.write(new StringWriter()).toString()
		
		"""
		
			##Glue Workflow Validation Error##
			Work Flow Name: ${unit?.name}
			${msg}
			--------------------------------------------------------
			${(reason) ? reason : ''}
			--------------------------------------------------------
			<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			${(contextStr)? contextStr : ''}
			<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				
		"""
		
	}
	
	
}
