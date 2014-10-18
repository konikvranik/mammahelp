package cz.mammahelp.handy.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.SQLiteDataTypes;
import cz.mammahelp.handy.dao.BaseDao.Column;
import cz.mammahelp.handy.model.Articles;
import cz.mammahelp.handy.model.Enclosure;

@SuppressWarnings("unused")
public class EnclosureDao extends BaseDao<Enclosure> {

	public static Logger log = LoggerFactory.getLogger(EnclosureDao.class);

	public static final String TABLE_NAME = "enclosures";

	public static final Column URL = new Column("url", SQLiteDataTypes.TEXT);
	public static final Column LENGTH = new Column("length",
			SQLiteDataTypes.INTEGER);
	public static final Column TYPE = new Column("type", SQLiteDataTypes.TEXT);
	public static final Column DATA = new Column("data", SQLiteDataTypes.BLOB);
	public static final Column UPDATED = new Column("updated",
			SQLiteDataTypes.TEXT);


	static {

		registerTable(TABLE_NAME);

		getTable().addColumn(ID);
		getTable().addColumn(URL);
		getTable().addColumn(LENGTH);
		getTable().addColumn(TYPE);
		getTable().addColumn(DATA);
		getTable().addColumn(UPDATED);

	}

	public EnclosureDao(MammaHelpDbHelper dbHelper) {
		super(dbHelper);
	}

	@Override
	protected Enclosure parseRow(Cursor cursor) {
		Enclosure e = new Enclosure(unpackColumnValue(cursor, ID, Long.class));
		e.setUrl(unpackColumnValue(cursor, URL, String.class));
		e.setLength(unpackColumnValue(cursor, LENGTH, Long.class));
		e.setType(unpackColumnValue(cursor, TYPE, String.class));
		e.setData(unpackColumnValue(cursor, DATA, byte[].class));
		e.setSyncTime(unpackColumnValue(cursor, UPDATED, Calendar.class));
		
		return e;
	}

	@Override
	protected String getTableName() {
		return getTable().getName();
	}

	@Override
	protected String[] getColumnNames() {
		return getTable().getColumnNames();
	}

	@Override
	protected ContentValues getValues(Enclosure obj, boolean updateNull) {
		TypedContentValues values = new TypedContentValues(updateNull);
		values.put(ID, obj.getId());
		values.put(URL, obj.getUrl());
		values.put(LENGTH, obj.getLength());
		values.put(TYPE, obj.getType());
		values.put(DATA, obj.getData());
		values.put(
				UPDATED,
				obj.getSyncTime() == null ? null : new SimpleDateFormat(
						DATE_FORMAT, LOCALE)
						.format(obj.getSyncTime().getTime()));
	

		return values.getValues();
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

	public Enclosure findByExactUrl(String url) {

		if (url != null && url.endsWith("/"))
			url = url.substring(0, url.length() - 1);

		SortedSet<Enclosure> result = query(URL.getName() + " = ?",
				new String[] { url }, null, null, null);
		if (result.size() > 1)
			throw new SQLiteConstraintException();
		else if (result.isEmpty())
			return null;
		return result.first();
	}

}
