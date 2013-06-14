package org.glue.unit.script;

import java.sql.ResultSet
import java.sql.ResultSetMetaData

/**
 * Wraps a ResultSet as a Map
 * Note that the wrapper will look at the current row.
 * This wrapper is now thread safe
 */
public class GlueResultSetMapWrapper implements Map<String, Object>{

	final ResultSet rs;
	final ResultSetMetaData meta;
	
	public GlueResultSetMapWrapper(ResultSet rs){
		this.rs = rs;
		this.meta = rs.getMetaData()
	}
	
	@Override
	public int size() {
			return meta.getColumnCount();
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		try{
			return rs.getAt(key) != null
		}catch(Exception e){
			return false
		}
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public Object get(Object key) {
		return rs.getObject(key);
	}

	@Override
	public Object put(String key, Object value) {
		throw new UnsupportedOperationException()
	}

	@Override
	public Object remove(Object key) {
		throw new UnsupportedOperationException()
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		throw new UnsupportedOperationException()
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException()
	}

	@Override
	public Set<String> keySet() {
		def keys = [] as Set
		for(int i = 0; i < meta.getColumnCount(); i++)
			keys << meta.getColumnName(i+1)
			
		return keys	
	}

	@Override
	public Collection<Object> values() {
		def values = []
		for(int i = 0; i < meta.getColumnCount(); i++)
			values << rs.getObject(i+1)
	
		return values
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		def entries = [] as Set
		
		for(int i = 0; i < meta.getColumnCount(); i++){
			final String name = meta.getColumnName(i+1)
			entries.add( new GlueDefaultMapEntry(name, 
				rs.getObject(name)) )
		}
	
		return entries;
	}

}
