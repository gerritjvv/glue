package org.glue.unit.om;

/**
 * 
 * Defines a trigger item.<br/>
 * Each GlueUnit can have multiple trigger definitions associated with it. The<br/>
 * actual trigger definitions is written in a single property inside the Glue<br/>
 * Unit but then parsed into a list of TriggerDef objects.<br/>
 * <p/>
 * e.g. triggers="hdfs:/mypath/*;cron:cronstring here"
 */
@Typed
public interface TriggerDef {

	String getType();
	String getValue();
	String getGroupIdentifier();
	
}
