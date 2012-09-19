package org.glue.gluecron.trigger;

/**
 * 
 * Interface used to get Unit Names to run
 * 
 */
public interface TriggersManager {

	String[] getUnitNames();

	void close();

	void setTriggerListener(TriggerListener listener);

}
