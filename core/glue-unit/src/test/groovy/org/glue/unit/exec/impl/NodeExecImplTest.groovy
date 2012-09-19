package org.glue.unit.exec.impl
;

import static groovyx.gpars.dataflow.DataFlow.start
import static org.junit.Assert.*
import groovyx.gpars.dataflow.DataFlowActorGroup

import org.glue.unit.exec.CyclicDependencyException
import org.glue.unit.exec.ExecNode
import org.junit.Test


/**
 * This class tests the ExecNodeImpl to ensure it can detect cyclic dependencies.<br/>
 * Note that the graph building process is important, and this unit test forms the basis of reference on how to build the node graph.
 */
class NodeExecImplTest {


	Map<String, ExecNode> nodeMap = [:]

	/**
	 * Test that complex cyclic dependencies are detected.
	 */
	@Test(expected=CyclicDependencyException)
	public void testComplex1CyclicDependency(){
		addToNodes('A', ['B'])
		addToNodes('B', ['C'])
		addToNodes('C', ['A'])
	}

	/**
	 * Test that complex cyclic dependencies are detected.
	 */
	@Test(expected=CyclicDependencyException)
	public void testComplex2CyclicDependency(){
		addToNodes('A', ['B'])
		addToNodes('B', ['C'])
		addToNodes('C', ['D'])
		addToNodes('D', ['E'])
		addToNodes('E', ['F'])
		addToNodes('F', ['A'])
	}


	/**
	 * Test that complex cyclic dependencies are detected.
	 */
	@Test(expected=CyclicDependencyException)
	public void testComplex3CyclicDependency(){
		addToNodes('A', ['B'])
		addToNodes('Q', ['F'])
		addToNodes('B', ['C'])
		addToNodes('C', ['D'])
		addToNodes('D', ['E'])
		addToNodes('E', ['F'])
		addToNodes('F', ['A'])
	}

	/**
	 * Test that simple cyclic dependencies are detected.
	 */
	@Test(expected=CyclicDependencyException)
	public void testSimpleCyclicDependency(){
		addToNodes('A', ['B'])
		addToNodes('B', ['A'])
	}

	/**
	 * Is a reference to how nodes should be used in the glue process execution.
	 */
	@Test
	public void testReferenceGlueProcessExecution(){

		addToNodes('F', ['D', 'E', 'A'])
		addToNodes('B', ['A'])
		addToNodes('E', ['B', 'C'])
		addToNodes('D', ['A', 'P', 'C'])
		addToNodes('C', ['A', 'B'])
		addToNodes('A')
		addToNodes('Q')
		addToNodes('P')

		def lists = []
		def actorMap = [:]
		
		DataFlowActorGroup group = new DataFlowActorGroup(1)
		
		nodeMap.values().each { ExecNode node ->
			//for each node make it wait for the DataFlowVariable
			
			def actor = group.actor{

				node.waitForParents()
				println """Running $node CurrentActor: ${actorMap[node].threadBoundActor()} nodeActor:${actorMap[node]}
				        \t${Thread.currentThread().getId()}"""
				node.setDone()
			}
			
			actorMap[node] = actor
			lists << actor
		}

		
		println lists
		lists.each { l -> l.join() }
		group.shutdown()
		println "END ${groovyx.gpars.actor.Actor.threadBoundActor()}"
		
		
		
	}

	public void addToNodes(String name, Collection<String> parents = null){

		//check for -> means depends on
		//A -> B, B <- A
		//A -> B, B -> C, C -> A
		// A depends on (B,C), B depends on (C)
		ExecNode child = getFromMap(name)

		parents?.each { String parentName ->

			ExecNode parent = getFromMap(parentName)
			child.parents << parent
			parent.children << child
		}

		child.checkCyclicDependencies()
	}

	private ExecNodeImpl getFromMap(String name){
		ExecNode node = nodeMap[name]
		if(!node){
			node = new ExecNodeImpl(name:name)
			nodeMap[name] = node
		}

		return node
	}
}
