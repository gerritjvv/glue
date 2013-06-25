package org.glue.trigger.persist

import groovy.lang.Closure

/**
 * 
 * TriggerStore is used to abstract the persistence implementation for hdfs triggers.
 *
 */
abstract class TriggerStore2 {

	abstract void destroy()

	
	/**
	 * List all files that where updated as READY by the trigger<br/>
	 * The closure is called with (entityName:String, filePath:String)<br/>
	 * The closure will be called within the scope of a database transaction.<br/>
	 * 
	 * Only files that are not currently executing should be returned.
	 * @param unitName
	 * @param closure sends the fileid and file name
	 * @param lock default is true. It true the TriggerStore must lock the ready files, the set as processed method should unlock any locked files.
	 *  
	 */
	abstract void listReadyFiles(String unitName, Closure closure, boolean lock = true)
	abstract Collection listReadyFiles(String unitName, boolean lock = true)
	
	/**
	* Marks a collection or list of files as processed
	* @param unitName
	* @param path
	*/
   abstract void markFilesAsProcessed(String unitName,  fileIds)
   

}
