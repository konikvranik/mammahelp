package cz.mammahelp.handy.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SortedSet;

import android.content.ContentValues;
import android.database.Cursor;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.SQLiteDataTypes;
import cz.mammahelp.handy.model.Bundle;

public class BundleDao extends BaseDao<Bundle> {

	public static final String TABLE_NAME = "bundle";

	public static final Column KEY = new Column("key", SQLiteDataTypes.TEXT);
	public static final Column VALUE = new Column("value", SQLiteDataTypes.BLOB);

	static {

		registerTable(TABLE_NAME);

		getTable().addColumn(ID);
		getTable().addColumn(KEY);
		getTable().addColumn(VALUE);

	}

	public BundleDao(MammaHelpDbHelper dbHelper) {
		super(dbHelper);
	}

	@Override
	public SortedSet<Bundle> findAll() {
		// TODO Auto-generated method stub
		return super.findAll();
	}

	@Override
	public Bundle findById(long obj) {
		// TODO Auto-generated method stub
		return super.findById(obj);
	}

	@Override
	protected Bundle parseRow(Cursor cursor) {
		Bundle a = new Bundle(unpackColumnValue(cursor, ID, Long.class));

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
	protected ContentValues getValues(Bundle obj, boolean updateNull) {
		TypedContentValues values = new TypedContentValues(updateNull);
		values.put(ID, obj.getId());
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
