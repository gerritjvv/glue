package org.glue.geluecron.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public interface DBManager {

	Connection getConnection();

	void release(Connection conn);

	void close(Statement st);

	void close(ResultSet rs);

	Statement createStatement(Connection conn);

	ResultSet query(Statement st, String sql);

	void destroy();
	
	
	void exec(String... exec);
	void query(String query, DBQueryClosure closure);
	<T> List<T> map(String query, DBQueryMapClosure<T> closure);
	
	void each(String query, final DBQueryEachClosure closure);

	public static interface DBQueryClosure{
		
		void call(ResultSet rs) throws Exception;
		
	}
	
	
	public static  interface DBQueryEachClosure{
		/**
		 * This ResultSet is at the current available cursor and you should only call the get methods on it
		 * @param rs
		 * @throws Exception
		 */
		void call(ResultSet rs) throws Exception;
		
	}
	
	public static interface DBQueryMapClosure<T>{
		/**
		 * This ResultSet is at the current available cursor and you should only call the get methods on it
		 * @param rs
		 * @return T
		 * @throws Exception
		 */
		T call(ResultSet rs) throws Exception;
		
	}


}
