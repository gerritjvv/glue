package org.glue.modules.hadoop.pig.counters

/**
 * 
 * Creates a typed report by loading all of the data from the different projections.<br/>
 * Adding the column type.<br/>
 * Type is taken as the last directory name in the path of each projection.<br/>
 * Then stores the data into a single directory
 *
 */
@Typed
class TypeReport {

	String path
	
	public TypeReport(){}
	public TypeReport(String path){
		this.path = path
	}
	
}
