package cz.mammahelp.handy.dao;

import static cz.mammahelp.handy.AndroidUtils.bundleToMap;

import java.util.Locale;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.SQLiteDataTypes;
import cz.mammahelp.model.Address;

public class AddressDao extends BaseDao<Address> {

	public static final String TABLE_NAME = "addresses";

	public static final Column URL = new Column("url", SQLiteDataTypes.TEXT);
	public static final Column ADMIN_AREA = new Column("admin_area",
			SQLiteDataTypes.TEXT);
	public static final Column COUNTRY_CODE = new Column("country_code",
			SQLiteDataTypes.TEXT);
	public static final Column COUNTRY_NAME = new Column("country_name",
			SQLiteDataTypes.TEXT);
	public static final Column EXTRAS = new Column("extras",
			SQLiteDataTypes.TEXT);
	public static final Column FEATURE_NAME = new Column("feature_name",
			SQLiteDataTypes.TEXT);
	public static final Column LATITUDE = new Column("latitude",
			SQLiteDataTypes.TEXT);
	public static final Column LONGITUDE = new Column("longitude",
			SQLiteDataTypes.TEXT);
	public static final Column LOCALITY = new Column("locality",
			SQLiteDataTypes.TEXT);
	public static final Column PHONE = new Column("phone", SQLiteDataTypes.TEXT);
	public static final Column POSTAL_CODE = new Column("postal_code",
			SQLiteDataTypes.TEXT);
	public static final Column PREMISES = new Column("premises",
			SQLiteDataTypes.TEXT);
	public static final Column SUB_ADMIN_AREA = new Column("sub_admin_area",
			SQLiteDataTypes.TEXT);
	public static final Column SUB_LOCALITY = new Column("sub_locality",
			SQLiteDataTypes.TEXT);
	public static final Column SUB_THOROUGHFARE = new Column(
			"sub_thoroughfare", SQLiteDataTypes.TEXT);
	public static final Column THOROUGHFARE = new Column("thoroughfare",
			SQLiteDataTypes.TEXT);
	public static final Column ADDRESS_LINES = new Column("address_lines",
			SQLiteDataTypes.TEXT);

	static {

		registerTable(TABLE_NAME);

		getTable().addColumn(ID);
		getTable().addColumn(URL);

		getTable().addColumn(ADMIN_AREA);
		getTable().addColumn(COUNTRY_CODE);
		getTable().addColumn(COUNTRY_NAME);
		getTable().addColumn(EXTRAS);
		getTable().addColumn(FEATURE_NAME);
		getTable().addColumn(LATITUDE);
		getTable().addColumn(LONGITUDE);
		getTable().addColumn(LOCALITY);
		getTable().addColumn(PHONE);
		getTable().addColumn(POSTAL_CODE);
		getTable().addColumn(PREMISES);
		getTable().addColumn(SUB_ADMIN_AREA);
		getTable().addColumn(SUB_LOCALITY);
		getTable().addColumn(SUB_THOROUGHFARE);
		getTable().addColumn(THOROUGHFARE);
		getTable().addColumn(ADDRESS_LINES);

	}

	public AddressDao(MammaHelpDbHelper dbHelper) {
		super(dbHelper);
	}

	public AddressDao(SQLiteDatabase db) {
		super(db);
	}

	@Override
	protected Address parseRow(Cursor cursor) {
		Address e = new Address(unpackColumnValue(cursor, ID, Long.class),
				Locale.getDefault());
		e.setUrl(unpackColumnValue(cursor, URL, String.class));

		e.setAdminArea(unpackColumnValue(cursor, ADMIN_AREA, String.class));
		e.setCountryCode(unpackColumnValue(cursor, COUNTRY_CODE, String.class));
		e.setCountryName(unpackColumnValue(cursor, COUNTRY_NAME, String.class));
		e.setBundleId(unpackColumnValue(cursor, EXTRAS, Long.class));
		e.setExtras(bundleToMap(unpackColumnValue(cursor, EXTRAS, Bundle.class)));
		e.setFeatureName(unpackColumnValue(cursor, FEATURE_NAME, String.class));
		Double lat = unpackColumnValue(cursor, LATITUDE, Double.class);
		if (lat != null)
			e.setLatitude(lat);
		e.setLocality(unpackColumnValue(cursor, LOCALITY, String.class));
		Double lon = unpackColumnValue(cursor, LONGITUDE, Double.class);
		if (lon != null)
			e.setLongitude(lon);
		e.setPhone(unpackColumnValue(cursor, PHONE, String.class));
		e.setPostalCode(unpackColumnValue(cursor, POSTAL_CODE, String.class));
		e.setPremises(unpackColumnValue(cursor, PREMISES, String.class));
		e.setSubAdminArea(unpackColumnValue(cursor, SUB_ADMIN_AREA,
				String.class));
		e.setSubLocality(unpackColumnValue(cursor, SUB_LOCALITY, String.class));
		e.setSubThoroughfare(unpackColumnValue(cursor, SUB_THOROUGHFARE,
				String.class));
		e.setThoroughfare(unpackColumnValue(cursor, THOROUGHFARE, String.class));
		e.setUrl(unpackColumnValue(cursor, URL, String.class));

		String a = unpackColumnValue(cursor, ADDRESS_LINES, String.class);

		if (a != null) {
			String[] lines = a.split("\\r?\\n");
			for (int i = 0; i < lines.length; i++)
				e.setAddressLine(i, lines[i]);
		}

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
	protected ContentValues getValues(Address obj, boolean updateNull) {
		TypedContentValues values = new TypedContentValues(updateNull);
		values.put(ID, obj.getId());
		values.put(URL, obj.getUrl());
		values.put(ADMIN_AREA, obj.getAdminArea());
		values.put(COUNTRY_CODE, obj.getCountryCode());
		values.put(COUNTRY_NAME, obj.getCountryName());
		values.put(EXTRAS, obj.getBundleId());
		values.put(FEATURE_NAME, obj.getFeatureName());
		if (obj.hasLatitude())
			values.put(LATITUDE, obj.getLatitude());
		if (obj.hasLongitude())
			values.put(LONGITUDE, obj.getLongitude());
		values.put(LOCALITY, obj.getLocality());
		values.put(PHONE, obj.getPhone());
		values.put(POSTAL_CODE, obj.getPostalCode());
		values.put(PREMISES, obj.getPremises());
		values.put(SUB_ADMIN_AREA, obj.getSubAdminArea());
		values.put(SUB_LOCALITY, obj.getSubLocality());
		values.put(SUB_THOROUGHFARE, obj.getSubThoroughfare());
		values.put(THOROUGHFARE, obj.getThoroughfare());

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i <= obj.getMaxAddressLineIndex(); i++) {
			sb.append(obj.getAddressLine(i));
			if (obj.getMaxAddressLineIndex() > i)
				sb.append("\n");
		}

		values.put(ADDRESS_LINES, sb.toString());

		return values.getValues();
	}

	public static Table getTable() {
		return getTable(TABLE_NAME);
	}

}
