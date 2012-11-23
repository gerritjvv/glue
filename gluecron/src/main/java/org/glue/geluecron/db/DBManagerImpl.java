package org.glue.geluecron.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

	public <T> List<T> map(String query, final DBQueryMapClosure<T> closure) {
		final List<T> list = new ArrayList<T>(10);
		query(query, new DBQueryClosure() {

			@Override
			public void call(ResultSet rs) throws Exception {
				while (rs.next()) {
					final T obj = closure.call(rs);
					if (obj != null)
						list.add(obj);
				}
			}
		});

		return list;
	}

	public void each(String query, final DBQueryEachClosure closure) {
		query(query, new DBQueryClosure() {

			@Override
			public void call(ResultSet rs) throws Exception {
				while (rs.next())
					closure.call(rs);
			}
		});
	}

	public void query(String query, DBQueryClosure closure) {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			conn = getConnection();
			st = createStatement(conn);

			rs = query(st, query);
			try {
				closure.call(rs);
			} catch (Exception excp) {
				RuntimeException rte = new RuntimeException(excp.toString(),
						excp);
				rte.setStackTrace(excp.getStackTrace());
				throw rte;
			}

		} finally {
			close(rs);
			close(st);
			release(conn);
		}

	}

	public void exec(String... exec) {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			conn = getConnection();
			st = createStatement(conn);

			try {
				for (String sql : exec)
					st.execute(sql);
			} catch (SQLException excp) {
				RuntimeException rte = new RuntimeException(excp.toString(),
						excp);
				rte.setStackTrace(excp.getStackTrace());
				throw rte;
			}

		} finally {
			close(rs);
			close(st);
			release(conn);
		}

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
