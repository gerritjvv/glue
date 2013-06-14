package org.glue.unit.script;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.collections.keyvalue.AbstractMapEntry;

public class GlueDefaultMapEntry extends AbstractMapEntry implements Iterable<Object>{

	public GlueDefaultMapEntry(Object key, Object value) {
		super(key, value);
	}

	@Override
	public Iterator<Object> iterator() {
		return Arrays.asList(key, value).iterator();
	}

	
}
