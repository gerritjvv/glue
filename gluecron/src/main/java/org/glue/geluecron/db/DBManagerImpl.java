package org.glue.geluecron.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManagerImpl implements DBManager {

	final String url;
	final String uid;
	final String pwd;

	public DBManagerImpl(String url, String uid, String pwd) {
		super();
		this.url = url;
		this.uid = uid;
		this.pwd = pwd;
	}

	@Override
	public Connection getConnection() {
		try {
			return DriverManager.getConnection(url, uid, pwd);
		} catch (SQLException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		}
	}

	public void destroy() {

	}

	@Override
	public void release(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close(Statement st) {
		try {
			if (st != null)
				st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Statement createStatement(Connection conn) {
		try {
			return conn.createStatement();
		} catch (SQLException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		}

	}

	@Override
	public ResultSet query(Statement st, String sql) {
		try {
			return st.executeQuery(sql);
		} catch (SQLException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		}
	}

	@Override
	public void close(ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			RuntimeException rte = new RuntimeException(e.toString(), e);
			rte.setStackTrace(e.getStackTrace());
			throw rte;
		}
	}

}
