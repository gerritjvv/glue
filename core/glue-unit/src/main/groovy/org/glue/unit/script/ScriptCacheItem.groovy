package org.glue.unit.script

/**
 * 
 * Stores the GroovyClassLoader, check sum and Script class.
 * The fileName property determines the uniqueness of each instance.
 */
@Typed
class ScriptCacheItem {

	Class<Script> script
	GroovyClassLoader loader
	String checksum
    
	String fileName

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
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
		ScriptCacheItem other = (ScriptCacheItem) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		return true;
	}	
    	
}
