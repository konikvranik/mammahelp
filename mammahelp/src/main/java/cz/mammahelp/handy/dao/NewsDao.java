package cz.mammahelp.handy.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.SortedSet;

import android.content.ContentValues;
import android.database.Cursor;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.SQLiteDataTypes;
import cz.mammahelp.handy.model.Enclosure;
import cz.mammahelp.handy.model.News;

public class NewsDao extends BaseDao<News> {

	public static final String TABLE_NAME = "news";

	public static final Column TITLE = new Column("title", SQLiteDataTypes.TEXT);
	public static final Column UPDATED = new Column("updated",
			SQLiteDataTypes.TEXT);
	public static final Column URL = new Column("url", SQLiteDataTypes.TEXT);
	public static final Column BODY = new Column("body", SQLiteDataTypes.TEXT);
	public static final Column ANNOTATION = new Column("annotation",
			SQLiteDataTypes.TEXT);
	public static final Column CATEGORY = new Column("category",
			SQLiteDataTypes.TEXT);
	public static final Column ENCLOSURE = new Column("enclosure",
			EnclosureDao.ID.getType(), new ForeignKey(EnclosureDao.class));

	static {

		registerTable(TABLE_NAME);

		getTable().addColumn(ID);
		getTable().addColumn(TITLE);
		getTable().addColumn(UPDATED);
		getTable().addColumn(URL);
		getTable().addColumn(BODY);
		getTable().addColumn(ANNOTATION);
		getTable().addColumn(CATEGORY);
		getTable().addColumn(ENCLOSURE);

	}

	public NewsDao(MammaHelpDbHelper dbHelper) {
		super(dbHelper);
	}

	@Override
	protected News parseRow(Cursor cursor) {
		News a = new News(unpackColumnValue(cursor, ID, Long.class));
		a.setTitle(unpackColumnValue(cursor, TITLE, String.class));
		a.setSyncTime(unpackColumnValue(cursor, UPDATED, Calendar.class));
		a.setUrl(unpackColumnValue(cursor, URL, String.class));
		a.setBody(unpackColumnValue(cursor, BODY, String.class));
		a.setAnnotation(unpackColumnValue(cursor, ANNOTATION, String.class));
		a.setCategory(unpackColumnValue(cursor, CATEGORY, String.class));
		a.setEnclosure(unpackColumnValue(cursor, ENCLOSURE, Enclosure.class));

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
	protected ContentValues getValues(News obj, boolean updateNull) {
		TypedContentValues values = new TypedContentValues(updateNull);
		values.put(ID, obj.getId());
		values.put(TITLE, obj.getTitle());
		values.put(UPDATED, new SimpleDateFormat(DATE_FORMAT, LOCALE)
				.format(obj.getSyncTime().getTime()));
		values.put(URL, obj.getUrl());
		values.put(BODY, obj.getBody());
		values.put(ANNOTATION, obj.getAnnotation());
		values.put(CATEGORY, obj.getCategory());
		values.put(ENCLOSURE, obj.getEnclosure());

		return values.getValues();
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

	public Collection<News> findOlder(Date syncTime) {
		SortedSet<News> result = query(UPDATED + " < ?",
				new String[] { new SimpleDateFormat(DATE_FORMAT, LOCALE)
						.format(syncTime) }, null, null, null);
		if (result.isEmpty())
			return null;
		return result;

	}

	public Collection<News> findNewerOrSame(Date syncTime) {
		SortedSet<News> result = query(UPDATED + " >= ?",
				new String[] { new SimpleDateFormat(DATE_FORMAT, LOCALE)
						.format(syncTime) }, null, null, null);
		if (result.isEmpty())
			return null;
		return result;

	}

}
