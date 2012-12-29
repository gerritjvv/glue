package org.glue.unit.repo.impl

import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder

import com.mongodb.DBCursor

/**
 * 
 * Iterates over a DBCursor created by MongoDBGlueUnitRepository.
 *
 */
class MongoDBIterator implements Iterator<GlueUnit>{

	final DBCursor cursor;
	final GlueUnitBuilder builder;
	
	public MongoDBIterator(DBCursor cursor, GlueUnitBuilder builder) {
		super();
		this.cursor = cursor;
		this.builder = builder;
	}

	@Override
	public boolean hasNext() {
		return cursor.hasNext()
	}

	@Override
	public GlueUnit next() {
		return builder.build(cursor.next().get("data").toString())
	}

	@Override
	public void remove() {
		cursor.remove()
	}
	
}
