package cz.mammahelp.handy.dao;

import android.content.ContentValues;
import android.database.Cursor;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.SQLiteDataTypes;
import cz.mammahelp.handy.model.Enclosure;

public class EnclosureDao extends BaseDao<Enclosure> {

	public static final String TABLE_NAME = "enclosures";

	public static final Column URL = new Column("url", SQLiteDataTypes.TEXT);
	public static final Column LENGTH = new Column("length",
			SQLiteDataTypes.INTEGER);
	public static final Column TYPE = new Column("type", SQLiteDataTypes.TEXT);
	public static final Column DATA = new Column("data", SQLiteDataTypes.BLOB);

	static {

		registerTable(TABLE_NAME);

		getTable().addColumn(ID);
		getTable().addColumn(URL);
		getTable().addColumn(LENGTH);
		getTable().addColumn(TYPE);
		getTable().addColumn(DATA);

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

		return values.getValues();
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

}
