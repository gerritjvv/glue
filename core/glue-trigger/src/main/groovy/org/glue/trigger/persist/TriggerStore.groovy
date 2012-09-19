package org.glue.trigger.persist

import groovy.lang.Closure

/**
 * 
 * TriggerStore is used to abstract the persistence implementation for hdfs triggers.
 *
 */
abstract class TriggerStore {

	abstract void destroy()

	/**
	* Gets the status of a file, if it does not exist null is returned
	* @param unitName
	* @param path
	* @return boolean
	*/
    abstract String getStatus(String unitName, String path)
   
	/**
	 * All operations inside the closure done with the TriggerStore is within the same transaction.
	 * @param closure a transactional TriggerStore instance is passed as parameter
	 */
	abstract void withTransaction(Closure closure);

	/**
	 * Configuration is sent to this method to setup to trigger store implementation.
	 */
	abstract void init(ConfigObject config)

	abstract void storeCheckPoints(String unitName, Map<String, CheckPoint> checkPoints)
	
	/**
	 * Store checkpoint variable for the current configured unit name.<br/>
	 * @param unitName 
	 * @param path This is the directory path to which the trigger was registered.
	 * @param checkpoint
	 */
	abstract void storeCheckPoint(String unitName, String path, CheckPoint checkpoint)

	/**
	 * Removes a checkpoint variable for the current configured unit name.<br/>
	 * @param unitName 
	 * @param path This is the directory path to which the trigger was registered.
	 */
	abstract void removeCheckPoint(String unitName, String path)
	/**
	 * Returns a map of key = string, value = date<br/>
	 * The key is the path for which the trigger was listening.
	 * @param unitName
	 * @return Map
	 */
	abstract Map<String, CheckPoint> getCheckPoints(String unitName)

	/**
	 * Get the check point date for the path.
	 * @param unitName 
	 * @param path
	 * @return Date
	 */
	abstract CheckPoint getCheckPoint(String unitName, String path)
	
	/**
	 * List all files that where updated as READY by the trigger<br/>
	 * The closure is called with (entityName:String, filePath:String)<br/>
	 * The closure will be called within the scope of a database transaction.<br/>
	 * 
	 * Only files that are not currently executing should be returned.
	 * @param unitName
	 * @param closure
	 * @param lock default is true. It true the TriggerStore must lock the ready files, the set as processed method should unlock any locked files.
	 *  
	 */
	abstract void listReadyFiles(String unitName, Closure closure, boolean lock = true)

	/**
	 * List all files that were found by the trigger<br/>
	 * The closure is called with (entityName:String, status:String, filePath:String)<br/>
	 * The closure will be called withing the scope of a database transaction.<br/>
	 * @param unitName
	 * @param closure
	 */
	abstract void listAllFiles(String unitName, Closure closure)

	/**
	 * List all files that were updated as PROCESSED by the work flows<br/>
	 * The closure is called with (entityName:String, filePath:String)<br/>
	 * The closure will be called withing the scope of a database transaction.<br/>
	 * @param unitName
	 * @param closure
	 */
	abstract void listProcessedFiles(String unitName, Closure closure)

	/**
	 * Marks a file as processed
	 * @param unitName
	 * @param path
	 */
	abstract void deleteFile(String unitName, String path)

	/**
	 * Marks a file as processed
	 * @param unitName
	 * @param path
	 */
	abstract void markFileAsProcessed(String unitName, String unitId, String path)

	/**
	* Marks a collection or list of files as processed
	* @param unitName
	* @param path
	*/
   abstract void markFilesAsProcessed(String unitName, String unitId, Collection<String> paths)
   
	/**
	 * True if the file has already been marked as processed
	 * @param unitName
	 * @param path
	 * @return boolean
	 */
	abstract boolean isFileProcessed(String unitName, String path)

	
	abstract void markFilesAsReady(String unitName, Collection<String> paths)
	
	/**
	 * Marks a file as ready, if the file doesn't already exist it will be created, else the entry is updated.
	 * @param unitName
	 * @param path
	 */
	abstract void markFileAsReady(String unitName, String path)

	/**
	 * This method will pass the ready files one by one to the closure, if the closure returns true the file is updated, else any
	 * changes made by the closure is discarded.
	 * @param unitName
	 * @param closure
	 */
	abstract void updateReadyFiles(String unitName, Closure closure)
}
