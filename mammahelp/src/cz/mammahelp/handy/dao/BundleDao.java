package cz.mammahelp.handy.dao;

import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
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
	protected Bundle parseRow(Cursor cursor) {
		Bundle a = new Bundle(unpackColumnValue(cursor, ID, Long.class));

		a.getBundle().putByteArray(
				unpackColumnValue(cursor, KEY, String.class),
				unpackColumnValue(cursor, VALUE, byte[].class));

		return a;
	}

	@Override
	protected SortedSet<Bundle> parseResults(Cursor cursor) {
		TreeMap<Long, Bundle> results = new TreeMap<Long, Bundle>();
		try {

			while (!cursor.isAfterLast()) {
				if (cursor.isBeforeFirst())
					cursor.moveToNext();
				if (cursor.isAfterLast())
					break;

				Bundle b = parseRow(cursor);
				Bundle b1 = results.get(b.getId());
				if (b1 == null)
					results.put(b.getId(), b);
				else
					b1.getBundle().putAll(b.getBundle());
				cursor.moveToNext();
			}
		} finally {
			cursor.close();
		}
		return new TreeSet<Bundle>(results.values());
	}

	@Override
	protected String getTableName() {
		return getTable().getName();
	}

	@Override
	protected String[] getColumnNames() {
		return getTable().getColumnNames();
	}

	protected void insert(SQLiteDatabase db, Bundle obj, boolean updateNull) {

		android.os.Bundle b = obj.getBundle();

		boolean inserted = false;

		for (String key : b.keySet()) {

			if (inserted) {
				int result = db.update(getTableName(),
						getValues(obj, key, updateNull), "id = ? and key = ?",
						new String[] { Long.toString(obj.getId()), key });
				if (result != 1)
					throw new RuntimeException("Updated " + result
							+ " rows while inserting. 1 expected.");
			} else {
				long result = db.insert(getTableName(), null,
						getValues(obj, key, updateNull));
				if (result == -1)
					throw new SQLException();
				obj.setId(result);
			}
		}

	}

	@Override
	protected void update(SQLiteDatabase db, Bundle obj, boolean updateNull) {

		android.os.Bundle b = obj.getBundle();

		for (String key : b.keySet()) {
			int result = db.update(getTableName(),
					getValues(obj, key, updateNull), "id = ? and key = ?",
					new String[] { Long.toString(obj.getId()), key });
			if (result != 1)
				throw new RuntimeException("Updated " + result
						+ " rows. 1 expected.");
		}

		super.update(db, obj, updateNull);
	}

	protected ContentValues getValues(Bundle obj, String key, boolean updateNull) {
		TypedContentValues values = new TypedContentValues(updateNull);
		values.put(ID, obj.getId());
		values.put(KEY, key);
		values.put(VALUE, obj.getBundle().getByteArray(key));

		return values.getValues();
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

	@Override
	protected ContentValues getValues(Bundle obj, boolean updateNull) {
		throw new RuntimeException(
				"getValues(T, boolean) for BundleDAo is not implemented. Use getValues(T, String, boolean) instead.");
	}

}
