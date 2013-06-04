package org.glue.unit.exec;

import org.glue.unit.om.GlueContext

/**
 * 
 * Interface for a type that provides methods showing the workflow execution status.
 * 
 */
public interface WorkflowsStatus {

	List<GlueContext> runningWorkflows()
	Set<String> queuedWorkflows()
		
}
