package cz.mammahelp.tools.dao;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mammahelp.handy.dao.GenericDao;
import cz.mammahelp.model.Identificable;

public abstract class AbstractFilesystemDao<T extends Identificable<T>> extends
		GenericDao<T> {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractFilesystemDao.class);

	private static final String JDBC_URL = "jdbc:hsqldb:mem:files";
	private static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
	private static Connection conn;
	static {
		ID = new JdbcColumn("id", Types.INTEGER, true);
	}

	AbstractFilesystemDao() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		prepareDb();
	}

	public static Connection getDbConnection() throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (conn == null) {
			Class.forName(DB_DRIVER).newInstance();
			conn = DriverManager.getConnection(JDBC_URL + ";create=true");
		}
		return conn;
	}

	public static void shutdownDb() throws SQLException {
		DriverManager.getConnection(JDBC_URL + ";shutdown=true");
	}

	public void prepareDb() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {

		Connection c = getDbConnection();

		Statement s = c.createStatement();
		s.execute(getTable(getTableName()).createClausule());

	}

	@Override
	public void insert(T obj) throws Exception {
		PreparedStatement st = prepareInsertStatement(obj);
		st.executeUpdate();
	}

	protected PreparedStatement prepareInsertStatement(T obj) throws Exception {
		StringBuffer sb = new StringBuffer("insert into ");
		sb.append(getTableName());
		sb.append(" (");
		Column<T>[] c = getCols(obj, false);
		for (int i = 0; i < c.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append(c[i].getName());
		}
		sb.append(") VALUES (");
		for (int i = 0; i < c.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append("?");
		}
		PreparedStatement st = getDbConnection()
				.prepareStatement(sb.toString());
		log.debug("Insert statement: " + sb);
		setValues(st, obj, c);
		return st;
	}

	protected PreparedStatement prepareDeleteStatement(Long id)
			throws Exception {
		StringBuffer sb = new StringBuffer("delete from ");
		sb.append(getTableName());
		sb.append(" where " + ID + "=?");
		PreparedStatement st = getDbConnection()
				.prepareStatement(sb.toString());
		log.debug("Delete statement: " + sb);
		st.setLong(1, id);
		return st;
	}

	protected PreparedStatement prepareUpdateStatement(T obj, boolean updateNull)
			throws Exception {

		StringBuffer sb = new StringBuffer("update ");
		sb.append(getTableName());
		Column<T>[] c = getCols(obj, updateNull);
		for (int i = 0; i < c.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append("set ");
			sb.append(c[i].getName());
			sb.append("=?");
		}
		sb.append(" where " + ID + "=?");
		PreparedStatement st = getDbConnection()
				.prepareStatement(sb.toString());
		log.debug("Update statement: " + sb);
		setValues(st, obj, c);
		st.setLong(c.length + 1, obj.getId());
		return st;
	}

	protected abstract void setValues(PreparedStatement statement, T obj,
			cz.mammahelp.handy.dao.GenericDao.Column<T>[] c);

	protected abstract Column<T>[] getCols(T obj, boolean updateNull);

	@Override
	public void update(T obj, boolean updateNull) throws Exception {
		PreparedStatement st = prepareUpdateStatement(obj, updateNull);
		st.executeUpdate();
	}

	@Override
	public void delete(Long id) throws Exception {
		PreparedStatement st = prepareDeleteStatement(id);
		st.executeUpdate();
	}

	@Override
	public T findById(long id) throws Exception {
		SortedSet<T> res = query("id=?", new String[] { String.valueOf(id) },
				null, null, null);

		if (res != null && res.size() > 1)
			throw new SQLIntegrityConstraintViolationException(
					"More than one rows with id " + id);
		return res.first();
	}

	protected abstract T getValues(ResultSet res, String[] c);

	public T findByExactUrl(String externalForm) throws Exception {

		SortedSet<T> res = query("url=?", new String[] { externalForm }, null,
				null, null);

		if (res != null && res.size() > 1)
			throw new SQLIntegrityConstraintViolationException(
					"More than one rows with url " + externalForm);
		return res.first();
	}

	@Override
	protected SortedSet<T> query(String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) throws Exception {

		if (selection == null)
			selectionArgs = null;

		StringBuffer sb = new StringBuffer();
		if (selection != null) {
			sb.append(" where ");
			sb.append(selection);
		}

		if (groupBy != null) {
			sb.append(" group by ");
			sb.append(groupBy);
		}

		if (having != null) {
			sb.append(" having ");
			sb.append(having);
		}
		if (orderBy != null) {
			sb.append(" order by ");
			sb.append(orderBy);
		}
		return rawQuery(selection, selectionArgs);

	}

	@Override
	protected SortedSet<T> rawQuery(String selection, String[] selectionArgs)
			throws Exception {

		StringBuffer sb = new StringBuffer("select ");
		String[] cn = getColumnNames();
		for (int i = 0; i < cn.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append(cn[i]);
		}
		sb.append(" from ");
		sb.append(getTableName());

		PreparedStatement st = null;
		if (selection != null) {
			sb.append(selection);
			st = getDbConnection().prepareStatement(sb.toString(),
					selectionArgs);
		} else {
			st = getDbConnection().prepareStatement(sb.toString());
		}
		ResultSet res = st.executeQuery();
		if (res.isAfterLast())
			return null;

		SortedSet<T> results = new TreeSet<T>();
		while (res.next()) {
			results.add(getValues(res, cn));
		}
		return results;

	}

	public static String makePathFromUrl(URL url) {
		return url.getFile();
	}

	public abstract File getFile(T object) throws MalformedURLException;

	public static class JdbcColumn extends Column<Integer> {

		public JdbcColumn(String name, Integer type, boolean pk,
				ForeignKey fk) {
			super(name, type, pk, fk);
		}

		public JdbcColumn(String name, Integer type, boolean pk) {
			super(name, type, pk);
		}

		public JdbcColumn(String name, Integer type,
				ForeignKey fk) {
			super(name, type, fk);
		}

		public JdbcColumn(String name, Integer type) {
			super(name, type);
		}

		public JdbcColumn(String name, int varchar) {
			super(name, varchar);
		}

		@Override
		protected String getTypeName(Integer type) {
			try {
				DatabaseMetaData md = getDbConnection().getMetaData();
				ResultSet rs = md.getTypeInfo();
				if (type == null)
					return null;
				while (rs.next()) {
					if (rs.getInt(2) == type)
						return rs.getString(1);
				}
			} catch (SQLException | InstantiationException
					| IllegalAccessException | ClassNotFoundException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}

	}

}
