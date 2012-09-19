package org.glue.unit.repo;

import java.util.Iterator;

import org.glue.unit.om.GlueUnit;


/**
 * 
 * Abstracts the logic of how and where GlueUnits are stored.<br/>
 * The simplest and default provided implementation if from the local file
 * system, but this can be easily extended to support content management systems
 * or remote storage etc.
 * 
 */
@Typed
public interface GlueUnitRepository extends Iterable<GlueUnit>{
	
	/**
	 * Returns an Iterator for the glue units.
	 * @return Iterator of GlueUnit
	 */
	Iterator<GlueUnit> iterator();
	
	/**
	 * This returns an iterator that iterates over a range of units.
	 * @param from the index from where to start e.g. 0-100 start at 50
	 * @param max the maximum amount of items to iterate over
	 * @param sortByClosure a closure to define the ordering, GlueUnit would be passed to it.
	 * @return Iterator of GlueUnit
	 */
	Iterator<GlueUnit> iterator(int from, int max);
	
   
	/**
	 * Returns the total number of glue units 
	 * @return int
	 */
	int size();
	/**
	 * Searches for a glue unit definition by its name
	 * @param name
	 * @return GlueUnit
	 */
	GlueUnit find(String name);
	
}
