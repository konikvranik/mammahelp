package cz.mammahelp.handy.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.SQLiteDataTypes;
import cz.mammahelp.handy.model.Address;
import cz.mammahelp.handy.model.LocationPoint;

public class LocationPointDao extends BaseDao<LocationPoint> {

	public static final String TABLE_NAME = "location_points";

	public static final Column ADDRESS = new Column("address",
			AddressDao.ID.getType(), new ForeignKey(AddressDao.class));
	public static final Column NAME = new Column("name", SQLiteDataTypes.TEXT);
	public static final Column DESCRIPTION = new Column("description",
			SQLiteDataTypes.TEXT);
	public static final Column URL = new Column("url", SQLiteDataTypes.TEXT);

	static {

		registerTable(TABLE_NAME);

		getTable().addColumn(ID);
		getTable().addColumn(URL);
		getTable().addColumn(ADDRESS);
		getTable().addColumn(NAME);
		getTable().addColumn(DESCRIPTION);

	}

	public LocationPointDao(MammaHelpDbHelper dbHelper) {
		super(dbHelper);
	}

	public LocationPointDao(SQLiteDatabase db) {
		super(db);
	}

	@Override
	protected LocationPoint parseRow(Cursor cursor) {
		LocationPoint e = new LocationPoint(unpackColumnValue(cursor, ID,
				Long.class));
		e.setUrl(unpackColumnValue(cursor, URL, String.class));
		e.setLocation(unpackColumnValue(cursor, ADDRESS, Address.class));
		e.setName(unpackColumnValue(cursor, NAME, String.class));
		e.setDescription(unpackColumnValue(cursor, DESCRIPTION, String.class));

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
	protected ContentValues getValues(LocationPoint obj, boolean updateNull) {
		TypedContentValues values = new TypedContentValues(updateNull);
		values.put(ID, obj.getId());
		values.put(URL, obj.getUrl());
		values.put(ADDRESS, obj.getLocation());
		values.put(NAME, obj.getName());
		values.put(DESCRIPTION, obj.getDescription());

		return values.getValues();
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

}
