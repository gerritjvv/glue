package org.glue.geluecron.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public interface DBManager {

	Connection getConnection();

	void release(Connection conn);

	void close(Statement st);

	void close(ResultSet rs);

	Statement createStatement(Connection conn);

	ResultSet query(Statement st, String sql);

	void destroy();

}
