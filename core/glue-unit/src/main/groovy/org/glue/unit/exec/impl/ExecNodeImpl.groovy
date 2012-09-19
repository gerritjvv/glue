package org.glue.unit.exec.impl




import groovyx.gpars.dataflow.DataFlowVariable

import org.glue.unit.exec.CyclicDependencyException
import org.glue.unit.exec.ExecNode
import org.glue.unit.om.GlueProcess

/**
 *
 * Implements the execution node.<br/>
 * Contains a flow variable that allows each node to wait for its parents to complete execution.<br/>
 * The setDone method indicates the node has completed execution.<br/>
 * <b>Note:</b>No execution is done inside the node, its only used for coordination.<br/>
 * <p/>
 * To wait for all the parents of this execution node to completed call the waitForParents method.
 * <br/>
 * The checkCyclicDependencies method will check that this node has no cyclic relations with any of its parents.<br/>
 * <p/>
 * This class is not Thread Safe and is meant to be used once only per process execution.
 * 
 */
@Typed
class ExecNodeImpl implements ExecNode{


	String name

	DataFlowVariable<ExecNode> flowVariable = new DataFlowVariable<ExecNode>()

	GlueProcess glueProcess

	Set<ExecNode> parents = []
	Set<ExecNode> children = []

	volatile Throwable error

	String toString(){
		name
	}

	void setDone(Throwable error = null){
		this.error = error
		//no execution or setting af variables after the line below.
		flowVariable << this
	}
	
	boolean hasError(){ return error != null }

	void waitForParents(){

		parents.each{ ExecNodeImpl parent ->
			parent.flowVariable.val
		}
	}

	/**
	 * Cyclic detection between two nodes can be simple or complex.<br/>
	 * Simple:<br/> 
	 * e.g A depends on B, B depends on A<br/>
	 * Complex:<br/>
	 * e.g. B depends on A, C depends on B, A depends on C.<br/>
	 *  
	 * @return Set of Node if null no cyclic dependencies were found.
	 */
	void checkCyclicDependencies() throws CyclicDependencyException{

		cyclicParentCheck(this, parents, [this])
	}

	/**
	 * Does a recursive cyclic detection by checking the child's parent and each parent in turn to its parents.<br/> 
	 * @param child
	 * @param parents
	 * @param callGraph Allows the exception to print out the exact call graph information.
	 */
	private final static void cyclicParentCheck(ExecNodeImpl child, Set<ExecNode> parents, List<ExecNode> callGraph){

		parents?.each {	ExecNode parent ->

			callGraph << parent

			if(parent.parents){

				//check that this parent has no parents that forms a cyclic dependency
				if(parent.parents.contains(child)){
					callGraph << child
					throw new CyclicDependencyException("Cyclic dependency ${callGraph}", child, parent)
				}

				//go one up in the node graph
				cyclicParentCheck(child, parent.parents, callGraph)
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecNodeImpl other = (ExecNodeImpl) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
