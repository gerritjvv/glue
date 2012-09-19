package org.glue.unit.repo

import java.util.Iterator;

import org.glue.unit.om.GlueUnit;

/**
 * 
 * Allows any module to listen to repository events.
 *
 */
@Typed
interface GlueUnitRepositoryListener {
	
	/**
	 * Called when a repository refresh event is triggered.
	 */
	void update(Iterator<GlueUnit> iterator);

}
