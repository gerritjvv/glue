package org.glue.unit.om;

import java.util.Set;

import groovy.lang.Closure;

/**
 * 
 * A GlueProcess represents on execution Stage/State/Function of a GlueUnit. How the GlueProcess(es) are run
 * depends entirely on the UnitExecutor.
 *
 */
@Typed
public interface GlueProcess {

	String getName();
	
	Closure getTask();
	
	Closure getSuccess();
	
	Closure getError();
	String getDescription();
	Set<String> getDependencies();
	
	
}
