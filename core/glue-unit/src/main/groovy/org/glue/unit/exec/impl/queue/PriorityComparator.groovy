package org.glue.unit.exec.impl.queue

/**
 * 
 * Compares the QueuedWorkflow(s) by priority, and if equal by seq (sequence number)
 *
 */
@Typed
class PriorityComparator implements Comparator<QueuedWorkflow>{

	static private final INSTANCE = new PriorityComparator()
	
	int compare(QueuedWorkflow q1,  QueuedWorkflow q2){
		int i = q1.priority <=> q2.priority
		if(i == 0){
			//compare by sequence
			i = q1.seq <=> q2.seq
		}
		return i
	}

	boolean equals(o){
		this.equals(o)
	}
	
	public static PriorityComparator getInstance(){ INSTANCE }
	
}
