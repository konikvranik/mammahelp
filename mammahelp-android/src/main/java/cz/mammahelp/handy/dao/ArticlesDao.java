package cz.mammahelp.handy.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.SQLiteDataTypes;
import cz.mammahelp.model.Articles;

public class ArticlesDao extends BaseDao<Articles> {

	public static Logger log = LoggerFactory.getLogger(ArticlesDao.class);

	public static final String TABLE_NAME = "articles";

	public static final Column<SQLiteDataTypes> TITLE = new AndroidSQLiteColumn(
			"title", SQLiteDataTypes.TEXT);
	public static final Column<SQLiteDataTypes> UPDATED = new AndroidSQLiteColumn(
			"updated", SQLiteDataTypes.TEXT);
	public static final Column<SQLiteDataTypes> URL = new AndroidSQLiteColumn(
			"url", SQLiteDataTypes.TEXT);
	public static final Column<SQLiteDataTypes> BODY = new AndroidSQLiteColumn(
			"body", SQLiteDataTypes.TEXT);
	public static final Column<SQLiteDataTypes> CATEGORY = new AndroidSQLiteColumn(
			"category", SQLiteDataTypes.TEXT);

	static {

		registerTable(TABLE_NAME);

		getTable().addColumn(ID);
		getTable().addColumn(TITLE);
		getTable().addColumn(UPDATED);
		getTable().addColumn(URL);
		getTable().addColumn(BODY);
		getTable().addColumn(CATEGORY);

	}

	public ArticlesDao(MammaHelpDbHelper dbHelper) {
		super(dbHelper);
	}

	public ArticlesDao(SQLiteDatabase db) {
		super(db);
	}

	@Override
	protected Articles parseRow(Cursor cursor) {
		Articles a = new Articles(unpackColumnValue(cursor,
				(AndroidSQLiteColumn) ID, Long.class));
		a.setTitle(unpackColumnValue(cursor, TITLE, String.class));
		a.setSyncTime(unpackColumnValue(cursor, UPDATED, Calendar.class));
		a.setUrl(unpackColumnValue(cursor, URL, String.class));
		a.setCategory(unpackColumnValue(cursor, CATEGORY, String.class));
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
		TypedContentValues values = new TypedContentValues(updateNull);
		values.put(ID, obj.getId());
		values.put(TITLE, obj.getTitle());
		values.put(
				UPDATED,
				obj.getSyncTime() == null ? null : new SimpleDateFormat(
						DATE_FORMAT, LOCALE)
						.format(obj.getSyncTime().getTime()));
		values.put(URL, obj.getUrl());
		values.put(CATEGORY, obj.getCategory());
		values.put(BODY, obj.getBody());

		return values.getValues();
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

	public SortedSet<Articles> findByCategory(String categoryCode) {
		SortedSet<Articles> result = query(CATEGORY.getName() + " = ?",
				new String[] { categoryCode }, null, null, null);
		log.trace("Category " + categoryCode + " has " + result.size()
				+ " articles.");
		return result;
	}

	public SortedSet<Articles> findByUrl(String url) {

		SortedSet<Articles> result = query(URL.getName() + " like ?",
				new String[] { url + "%" }, null, null, null);
		log.trace("URL " + url + " has " + result.size() + " articles.");
		return result;
	}

	public Articles findByExactUrl(String url) {

		if (url != null && url.endsWith("/"))
			url = url.substring(0, url.length() - 1);

		SortedSet<Articles> result = query(URL.getName() + " = ?",
				new String[] { url }, null, null, null);
		if (result.size() > 1)
			throw new SQLiteConstraintException();
		else if (result.isEmpty())
			return null;
		return result.first();
	}

}
