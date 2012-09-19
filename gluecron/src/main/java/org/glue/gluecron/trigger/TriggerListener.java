package org.glue.gluecron.trigger;

/**
 * 
 * Used to listen to TriggerManager implementations when a new condition is
 * found that requires workflows to launch.
 * 
 */
public interface TriggerListener {

	void launch(String[] unitNames);

}
