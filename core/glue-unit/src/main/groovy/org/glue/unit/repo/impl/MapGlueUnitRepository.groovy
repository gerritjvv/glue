package org.glue.unit.repo.impl

import java.util.Iterator;
import java.util.Map;

import org.glue.unit.om.GlueUnit;
import org.glue.unit.repo.GlueUnitRepository;

/**
 *
 * A Map implementation of the GlueUnitRepository.<br/>
 * This implements an in-memory repository, and data will not be persisted.
 *
 */
@Typed(TypePolicy.MIXED)
class MapGlueUnitRepository implements GlueUnitRepository{
	
	Map<String, GlueUnit> mapRepo = [:]
	
	public MapGlueUnitRepository() {
	}
	
	public MapGlueUnitRepository(Map<String, GlueUnit> mapRepo) {
		super();
		this.mapRepo = mapRepo;
	}
	
	public void leftShift(GlueUnit glueUnit){
		add(glueUnit)
	}
	
	public GlueUnit remove(String name){
		mapRepo.remove name
	}
	
	public void clear(){
		mapRepo.clear()
	}
	
	public void add(GlueUnit glueUnit){
		mapRepo[glueUnit.name] = glueUnit
	}
	
	public void add(GlueUnit[] glueUnits){
		glueUnits.each { GlueUnit unit -> add(unit) }
	}
	
	@Override
	public Iterator<GlueUnit> iterator() {
		return mapRepo?.values().iterator();
	}
	
	@Override
	public Iterator<GlueUnit> iterator(int from, int max) {
		
		if(from < 0){
			from = 0
		}

		if(max == 0){
			max = -1
		}

		Collection units = mapRepo?.values()
		
		
		if(!units){
			units = []
		}else if(from > units.size()){
			units = []
		}else if(max > 0){
			//only apply range if max is larger than zero

			if(from > units.size()){
				units = []
			}else{
				//ensure that the max is within bounds
				int toIndex = from + max
				if(toIndex > units.size()){
					toIndex = units.size() - 1
				}

				units = units[from..toIndex]
			}
		}

		units.iterator()
	}
	
	@Override
	public int size() {
		mapRepo.size()
	}
	
	@Override
	public GlueUnit find(String name) {
		mapRepo[name]
	}
}
