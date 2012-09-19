package org.glue.unit.exec.impl.queue

import java.util.Comparator;

/**
 * 
 * A holder for a workflow submission.
 * This item contains the uuid, and workflow name.
 */
@Typed
class QueuedWorkflow implements Serializable, Comparable<QueuedWorkflow>{
 
	static final Comparator<QueuedWorkflow> PRIORITY_COMPARATOR = PriorityComparator.getInstance()
	
	/**
	 * This sequence number is uniquely assigned and used for FIFO ordering.
	 */
	long seq = System.nanoTime();
	
	/**
	 * Workflow name
	 */
	String name
	/**
	 * unique execution id
	 */
	String uuid

	Map<String, Object> params
	
	int priority = 0
	
	/**
	 * @param name workflow name
	 * @param uuid execution id
	 */
	public QueuedWorkflow(String name, String uuid, Map<String, Object> params) {
		super()
		this.name = name
		this.uuid = uuid
		this.params = params
	}
	
	public QueuedWorkflow(String name, String uuid, Map<String, Object> params, int priority) {
		super()
		this.name = name
		this.uuid = uuid
		this.params = params
		this.priority = priority
	}

	public int compareTo(QueuedWorkflow qwf){
		"$name$uuid" <=> "${qwf.name}${qwf.uuid}"
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		QueuedWorkflow other = (QueuedWorkflow) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}	
	
}
