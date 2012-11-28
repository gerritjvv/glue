
package org.glue.trigger.persist.db

import groovy.lang.Closure
import groovy.util.ConfigObject

import java.util.Date
import java.util.Map
import java.util.Properties
import java.util.Set
import java.sql.*;
import org.glue.trigger.persist.TriggerStore2;

import org.streams.commons.zookeeper.ZConnection
import org.streams.commons.zookeeper.ZLock


/**
 * 
 * Implements the TriggerStore interface to save all history trigger events to a RDBMS.<br/>
 * All events are saved through a JPA hibernate connection.<br/>
 *
 */

class HistoryDBTriggerStore extends DBTriggerStore2{


	/**
	 * List all files that where updated as READY by the trigger<br/>
	 * The closure is called with (datetime:Date, fileId:int, filePath:String)<br/>
	 * The closure will be called within the scope of a database transaction.<br/>
	 * @param unitName
	 * @param closure
	 */
	public void listReadyFiles(String unitName, Closure closure, boolean lock = true){


		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		Connection conn = DriverManager.getConnection(url, uid, pwd);
		Statement st = conn.createStatement();
		try{
			ResultSet rs = st.executeQuery(" select datetime, fileid, path from unitfiles uf, hdfsfiles_history hf, unittriggers ut WHERE uf.status = 'process' AND uf.fileid = hf.id AND ut.id = uf.unitid AND ut.unit = '${unitName}'");

			if(rs.first()){
				while(true){
					closure(rs.getDate(1), rs.getInt(2), rs.getString(3));
					if(!rs.next())
						break;	
				}
			}
		}finally{
			st.close();
			conn.close()
		}
	}


	/**
	 * Marks a file as processed
	 * @param unitName
	 * @param fileId ids
	 */
	public void markFilesAsProcessed(String unitName, fileIds){

		if(unitName == null){
			throw new NullPointerException("No unit name was specified")
		}

		Connection conn = DriverManager.getConnection(url, uid, pwd);
		conn.setAutoCommit(false);
		Statement st = conn.prepareStatement("INSERT INTO unitfiles_history (unitid, fileid, status) SELECT id, ?, 'processed' from unittriggers where unit = '${unitName}' ON DUPLICATE KEY UPDATE status='processed'");
		try{

			for(fileId in fileIds){
				st.setInt(1, fileId);
				st.addBatch();
			}
			
			st.executeBatch();
			conn.commit();
		}catch(BatchUpdateException exc){
			conn.rollback();
			throw exc;
		}finally{
			st.close();
			conn.close()
		}
	}


}
