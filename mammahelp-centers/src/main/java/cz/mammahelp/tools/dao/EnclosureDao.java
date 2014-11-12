package cz.mammahelp.tools.dao;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Types;

import cz.mammahelp.model.Enclosure;

public class EnclosureDao extends AbstractFilesystemDao<Enclosure> {

	public EnclosureDao() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		super();
	}

	public static final String TABLE_NAME = "enclosures";

	public static final Column<Integer> URL = new JdbcColumn("url",
			Types.VARCHAR);
	public static final Column<Integer> LENGTH = new JdbcColumn("length",
			Types.INTEGER);
	public static final Column<Integer> TYPE = new JdbcColumn("type",
			Types.VARCHAR);
	public static final Column<Integer> DATA = new JdbcColumn("data",
			Types.BINARY);
	public static final Column<Integer> UPDATED = new JdbcColumn("updated",
			Types.VARCHAR);

	static {

		Table table = registerTable(TABLE_NAME);

		table.addColumn(ID);
		table.addColumn(URL);
		table.addColumn(LENGTH);
		table.addColumn(TYPE);
		table.addColumn(DATA);
		table.addColumn(UPDATED);

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

	public Enclosure findByExactUrl(String externalForm) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

	@Override
	public File getFile(Enclosure object) throws MalformedURLException {
		return new File(makePathFromUrl(new URL(object.getUrl())));
	}

}
