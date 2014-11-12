package cz.mammahelp.tools.dao;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Types;

import cz.mammahelp.model.Articles;

public class ArticlesDao extends AbstractFilesystemDao<Articles> {

	public ArticlesDao() throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		super();
	}

	public static final String TABLE_NAME = "articles";

	public static final Column<Integer> TITLE = new JdbcColumn("title",
			Types.VARCHAR);
	public static final Column<Integer> UPDATED = new JdbcColumn("updated",
			Types.VARCHAR);
	public static final Column<Integer> URL = new JdbcColumn("url",
			Types.VARCHAR);
	public static final Column<Integer> BODY = new JdbcColumn("body",
			Types.VARCHAR);
	public static final Column<Integer> CATEGORY = new JdbcColumn("category",
			Types.VARCHAR);

	static {

		Table table = registerTable(TABLE_NAME);

		table.addColumn(ID);
		table.addColumn(TITLE);
		table.addColumn(UPDATED);
		table.addColumn(URL);
		table.addColumn(BODY);
		table.addColumn(CATEGORY);

	}

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@Override
	protected String[] getColumnNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public Articles findByExactUrl(String externalForm) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

	@Override
	public File getFile(Articles object) throws MalformedURLException {
		return new File(makePathFromUrl(new URL(object.getUrl())));
	}

}
