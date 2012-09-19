package org.glue.unit.exec

import org.glue.unit.exec.impl.ExecNodeImpl

/**
 *
 * When a GlueUnit is created we can construct an execution graph.<br/>
 * Any process that via its dependencies will cause a cyclic dependency<br/>
 * should be detected and throw this exception.
 * <p/>
 * The child property shows the child that has the cyclic dependency.<br/>
 * The parent property shows the parent that is the cyclic dependency with the child.<br/>
 */
@Typed
class CyclicDependencyException extends RuntimeException{

	ExecNode child
	ExecNode parent
	
	public CyclicDependencyException(ExecNodeImpl child, ExecNode parent) {
		this.child = child
		this.parent = parent
	}

	public CyclicDependencyException(String msg, Throwable t, ExecNode child, ExecNode parent) {
		super(msg, t);
		this.child = child
		this.parent = parent
	}

	public CyclicDependencyException(String msg, ExecNode child, ExecNode parent) {
		super(msg)
		this.child = child
		this.parent = parent
	}

	public CyclicDependencyException(Throwable t, ExecNode child, ExecNode parent) {
		super(t)
		this.child = child
		this.parent = parent
	}
	
	
}
