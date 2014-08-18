package cz.mammahelp.handy.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.database.Cursor;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.SQLiteDataTypes;
import cz.mammahelp.handy.model.Articles;

public class ArticlesDao extends BaseDao<Articles> {

	public static final String TABLE_NAME = "articles";

	public static final Column TITLE = new Column("title", SQLiteDataTypes.TEXT);
	public static final Column UPDATED = new Column("updated",
			SQLiteDataTypes.TEXT);
	public static final Column URL = new Column("url", SQLiteDataTypes.TEXT);
	public static final Column BODY = new Column("body", SQLiteDataTypes.TEXT);

	static {

		registerTable(TABLE_NAME);

		getTable().addColumn(ID);
		getTable().addColumn(TITLE);
		getTable().addColumn(UPDATED);
		getTable().addColumn(URL);
		getTable().addColumn(BODY);

	}

	public ArticlesDao(MammaHelpDbHelper dbHelper) {
		super(dbHelper);
	}

	@Override
	protected Articles parseRow(Cursor cursor) {
		Articles a = new Articles(unpackColumnValue(cursor, ID, Long.class));
		a.setTitle(unpackColumnValue(cursor, TITLE, String.class));
		a.setSyncTime(unpackColumnValue(cursor, UPDATED, Calendar.class));
		a.setUrl(unpackColumnValue(cursor, URL, String.class));
		a.setBody(unpackColumnValue(cursor, BODY, String.class));

		return a;
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
	protected ContentValues getValues(Articles obj, boolean updateNull) {
		MammaHelpContentValues values = new MammaHelpContentValues(updateNull);
		values.put(ID, obj.getId());
		values.put(TITLE, obj.getTitle());
		values.put(TITLE, obj.getTitle());
		values.put(UPDATED, new SimpleDateFormat(DATE_FORMAT, LOCALE)
				.format(obj.getSyncTime().getTime()));
		values.put(URL, obj.getUrl());
		values.put(BODY, obj.getBody());
		return values.getValues();
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

}
