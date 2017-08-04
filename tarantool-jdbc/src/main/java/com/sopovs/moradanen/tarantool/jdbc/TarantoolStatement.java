package com.sopovs.moradanen.tarantool.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;

import com.sopovs.moradanen.tarantool.MapResult;
import com.sopovs.moradanen.tarantool.TarantoolClient;

public class TarantoolStatement implements Statement {
	private final TarantoolConnection connection;
	protected final TarantoolClient client;
	private boolean closed = false;

	public TarantoolStatement(TarantoolConnection connection) throws SQLException {
		this.connection = connection;
		this.client = connection.unwrap(TarantoolClient.class);
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		client.execute(sql);
		return new TarantoolResultSet(this, (MapResult) client.execute());
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		client.execute(sql);
		return (int) client.executeUpdate();
	}

	@Override
	public void close() throws SQLException {
		if (closed) {
			return;
		}
		closed = true;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return connection;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("not a wrapper for " + iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	@Override
	public int getMaxRows() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void cancel() throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean execute(String sql) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int getUpdateCount() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int getFetchDirection() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int getFetchSize() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int getResultSetType() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void clearBatch() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int[] executeBatch() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	@Override
	public boolean isPoolable() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

}