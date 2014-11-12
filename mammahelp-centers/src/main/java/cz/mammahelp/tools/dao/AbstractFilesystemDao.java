package cz.mammahelp.tools.dao;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.SortedSet;

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
	public void insert(T obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(T obj, boolean updateNull) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Collection<T> objs, boolean updateNull) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public T findById(long obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SortedSet<T> query(String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(Collection<T> objs) {
		// TODO Auto-generated method stub

	}

	@Override
	protected SortedSet<T> rawQuery(String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String makePathFromUrl(URL url) {
		return url.getFile();
	}

	public abstract File getFile(T object) throws MalformedURLException;

	public static class JdbcColumn extends Column<Integer> {

		public JdbcColumn(String name, Integer type, boolean pk,
				cz.mammahelp.handy.dao.GenericDao.ForeignKey fk) {
			super(name, type, pk, fk);
		}

		public JdbcColumn(String name, Integer type, boolean pk) {
			super(name, type, pk);
		}

		public JdbcColumn(String name, Integer type,
				cz.mammahelp.handy.dao.GenericDao.ForeignKey fk) {
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
