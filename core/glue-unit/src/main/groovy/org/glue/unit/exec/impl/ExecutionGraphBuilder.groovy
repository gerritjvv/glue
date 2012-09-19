package org.glue.unit.exec.impl


import org.glue.unit.exec.CyclicDependencyException
import org.glue.unit.exec.ExecNode
import org.glue.unit.exec.UndefinedProcessException
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

/**
 *
 * Builds an execution graph for a GlueUnit.<br/>
 * This class is thread safe so that the method build can be called from any number of threads.<br/>
 *
 */
@Typed
class ExecutionGraphBuilder {

	private static final ExecutionGraphBuilder _INSTANCE = new ExecutionGraphBuilder()

	/**
	 * Builds an execution graph made up of nodes with dependencies between them.<br/>
	 * This building process also checks for cyclic dependencies, throwing a CyclicDependencyException if detected. 
	 * @param glueUnit
	 * @return Map key=processName, value=Node
	 * @throws CyclicDependencyException
	 */
	public Map<String, ExecNode> build(GlueUnit glueUnit) throws CyclicDependencyException, UndefinedProcessException{


		Map<String, ExecNode> nodeMap = [:]

		glueUnit?.processes?.each { String name, GlueProcess process ->

			addToNodes(name, process, nodeMap)
		}

		//check for null processes
		//if any process is null then it was not defined
		nodeMap.each { String name, ExecNode node ->
			
			if(!node?.glueProcess){
				throw new UndefinedProcessException("Process ${node.name} is not defined referenced by ${node?.children}")
			}
			
		}
		
		return nodeMap
	}

	public static ExecutionGraphBuilder getInstance(){
		return _INSTANCE;
	}

	/**
	 * Adds the GlueProcess to the nodes graph.
	 * @param name
	 * @param process
	 * @param nodeMap Used for easy access to nodes via their name
	 */
	private final static void addToNodes(String name, GlueProcess process, Map<String,ExecNode> nodeMap){

		Collection<String> parents = process.dependencies

		//check for -> means depends on
		//A -> B, B <- A
		//A -> B, B -> C, C -> A
		ExecNode child = getFromMap(name, process, nodeMap)

		parents?.each { String parentName ->

			//if the parent is not created, we create a node for it but with an empty GlueProcess field.
			ExecNodeImpl parent = getFromMap(parentName, null, nodeMap)
			child.parents << parent
			parent.children << child

		}

		child.checkCyclicDependencies()
	}

	/**
	 * Gets a Node instance by name from the nodeMap. If the node does not exist a node instance is created
	 * @param name
	 * @param nodeMap
	 * @return
	 */
	static private final ExecNodeImpl getFromMap(String name, GlueProcess process = null, Map<String,ExecNodeImpl> nodeMap){
		ExecNodeImpl node = nodeMap[name]
		if(!node){
			node = new ExecNodeImpl(name:name)

			//add the new node to nodeMap and nodes
			nodeMap[name] = node
		}
		//we only assign the process if its not null
		if( process && !node.glueProcess){
			node.glueProcess = process
		}

		return node
	}
}
