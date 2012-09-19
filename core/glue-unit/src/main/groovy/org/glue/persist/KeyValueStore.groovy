package org.glue.persist;

/**
 *
 * Generic interface of a key value store.
 * The default implementation for this is the org.glue.modules.DbStoreModule
 *
 */
@Typed
public interface KeyValueStore {

	public String getValue(Object key);
	public void setValue(Object key, Object value);
	
}
