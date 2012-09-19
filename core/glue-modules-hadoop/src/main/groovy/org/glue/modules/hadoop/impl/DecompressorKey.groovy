package org.glue.modules.hadoop.impl

import org.apache.commons.lang.StringUtils;

/**
 * 
 * Key class for finding the decompressors
 *
 */
@Typed
class DecompressorKey implements Comparable<DecompressorKey>, Serializable{

	String clusterName
	String file

	int compareTo(DecompressorKey otherKey){
		"$clusterName$file" <=> "${otherKey.clusterName}${otherKey.file}"
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clusterName == null) ? 0 : clusterName.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
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
		DecompressorKey other = (DecompressorKey) obj;
		if (clusterName == null) {
			if (other.clusterName != null)
				return false;
		} else if (!clusterName.equals(other.clusterName))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}	
	
}
