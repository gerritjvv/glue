package org.glue.unit.log.impl

import groovyx.gpars.actor.Actor

import org.apache.log4j.Logger
import org.glue.unit.log.GlueExecLogger

/**
 * 
 * Instead of using the Thread.currentThread() this class uses to Actor.threadBoundActor() method.<br/>
 * It does mean that this stream can only ever be called inside an Actor's execution.
 *
 */
@Typed
public class ActorDistributedOutputStream extends DistributedOutputStream {

	private static final Logger LOG = Logger.getLogger(ActorDistributedOutputStream.class)
	
	public ActorDistributedOutputStream(GlueExecLogger logger) {
		super(logger);
	}

	@Override
	public String threadId() {
		
		Actor actor = Actor.threadBoundActor()
		String id = (actor) ? String.valueOf(actor.hashCode()) : Thread.currentThread().getName()
		
		return id
	}
}
