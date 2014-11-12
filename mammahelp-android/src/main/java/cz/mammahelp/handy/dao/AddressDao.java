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

	public static final AndroidSQLiteColumn URL = new AndroidSQLiteColumn(
			"url", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn ADMIN_AREA = new AndroidSQLiteColumn(
			"admin_area", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn COUNTRY_CODE = new AndroidSQLiteColumn(
			"country_code", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn COUNTRY_NAME = new AndroidSQLiteColumn(
			"country_name", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn EXTRAS = new AndroidSQLiteColumn(
			"extras", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn FEATURE_NAME = new AndroidSQLiteColumn(
			"feature_name", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn LATITUDE = new AndroidSQLiteColumn(
			"latitude", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn LONGITUDE = new AndroidSQLiteColumn(
			"longitude", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn LOCALITY = new AndroidSQLiteColumn(
			"locality", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn PHONE = new AndroidSQLiteColumn(
			"phone", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn POSTAL_CODE = new AndroidSQLiteColumn(
			"postal_code", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn PREMISES = new AndroidSQLiteColumn(
			"premises", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn SUB_ADMIN_AREA = new AndroidSQLiteColumn(
			"sub_admin_area", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn SUB_LOCALITY = new AndroidSQLiteColumn(
			"sub_locality", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn SUB_THOROUGHFARE = new AndroidSQLiteColumn(
			"sub_thoroughfare", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn THOROUGHFARE = new AndroidSQLiteColumn(
			"thoroughfare", SQLiteDataTypes.TEXT);
	public static final AndroidSQLiteColumn ADDRESS_LINES = new AndroidSQLiteColumn(
			"address_lines", SQLiteDataTypes.TEXT);

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
		Address e = new Address(unpackColumnValue(cursor, (AndroidSQLiteColumn)ID, Long.class),
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
