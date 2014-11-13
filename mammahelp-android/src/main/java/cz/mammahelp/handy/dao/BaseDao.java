package cz.mammahelp.handy.dao;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import cz.mammahelp.Utils;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.SQLiteDataTypes;
import cz.mammahelp.model.Address;
import cz.mammahelp.model.Enclosure;
import cz.mammahelp.model.Identificable;

public abstract class BaseDao<T extends Identificable<T>> extends GenericDao<T> {

	public static Logger log = LoggerFactory.getLogger(BaseDao.class);

	private MammaHelpDbHelper dbHelper;

	private SQLiteDatabase db;

	static {
		ID = new AndroidSQLiteColumn("id", SQLiteDataTypes.INTEGER, true);
	}

	public BaseDao(MammaHelpDbHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public BaseDao(SQLiteDatabase db) {
		this.db = db;
	}

	protected MammaHelpDbHelper getDbHelper() {
		return dbHelper;
	}

	@Override
	public void insert(Collection<T> objs) {
		if (objs == null)
			return;
		SQLiteDatabase db = getDatabase(true);
		try {
			for (T obj : objs) {
				insert(db, obj);
			}
		} finally {
			// db.close();
		}
	}

	protected void insert(SQLiteDatabase db, T obj) {
		insert(db, obj, true);
	}

	protected void insert(SQLiteDatabase db, T obj, boolean updateNull) {
		long result = db.insert(getTableName(), null,
				getValues(obj, updateNull));
		if (result == -1)
			throw new SQLException();
		obj.setId(result);
	}

	@Override
	public void insert(T obj) {
		SQLiteDatabase db = getDatabase(true);
		try {
			insert(db, obj);
		} finally {
			// db.close();
		}
	}

	protected abstract ContentValues getValues(T obj, boolean updateNull);

	@Override
	public void update(T obj, boolean updateNull) {
		SQLiteDatabase db = getDatabase(true);
		try {
			update(db, obj, updateNull);
		} finally {
			// db.close();
		}
	}

	protected void update(SQLiteDatabase db, T obj, boolean updateNull) {
		int result = db.update(getTableName(), getValues(obj, updateNull),
				"id = ?", new String[] { Long.toString(obj.getId()) });
		if (result != 1)
			throw new RuntimeException("Updated " + result
					+ " rows. 1 expected.");
	}

	public void update(Collection<T> objs, boolean updateNull) {
		SQLiteDatabase db = getDatabase(true);
		try {
			for (T obj : objs) {
				update(db, obj, updateNull);
			}
		} finally {
			// db.close();
		}
	}

	private SQLiteDatabase getDatabase(boolean writable) {

		if (getDbHelper() != null)

			if (writable)
				return getDbHelper().getWritableDatabase();
			else
				return getDbHelper().getReadableDatabase();
		else if (writable && db.isReadOnly())
			throw new RuntimeException(
					"Requested RW database but RO is available!");
		else
			return db;
	}

	@Override
	public void delete(Long id) {
		SQLiteDatabase db = getDatabase(true);
		try {
			delete(db, id);
		} finally {
			// db.close();
		}
	}

	protected void delete(SQLiteDatabase db, Long id) {
		log.trace("SQL delete: " + getTableName() + ", id=" + id);
		db.delete(getTableName(), "id = ?", new String[] { id == null ? null
				: Long.toString(id) });
	}

	@Override
	public T findById(long obj) {
		SortedSet<T> result = query(ID + " = ?",
				new String[] { Long.toString(obj) }, null, null, null);
		if (result.size() > 1)
			throw new SQLiteConstraintException();
		else if (result.isEmpty())
			return null;
		return result.first();
	}

	@Override
	protected SortedSet<T> query(String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		long millis1 = System.currentTimeMillis();
		SQLiteDatabase db = getDatabase(false);
		long millis2 = System.currentTimeMillis();
		long count = -1;
		try {
			Cursor cursor = db.query(getTableName(), getColumnNames(),
					selection, selectionArgs, groupBy, having, orderBy);
			count = cursor.getCount();
			return parseResults(cursor);
		} finally {
			long now = System.currentTimeMillis();
			log.debug("SQL query("+this.getClass().getName()+"): \"" + getTableName() + ": " + selection
					+ "\" " + Arrays.toString(selectionArgs) + " / count: "
					+ count + " / times: " + (now - millis1) + ", "
					+ (now - millis2));
			// db.close();
		}
	}

	@Override
	protected SortedSet<T> rawQuery(String selection, String[] selectionArgs) {
		long millis1 = System.currentTimeMillis();
		SQLiteDatabase db = getDatabase(false);
		long millis2 = System.currentTimeMillis();
		try {
			Cursor cursor = db.rawQuery(selection, selectionArgs);
			return parseResults(cursor);
		} finally {
			long now = System.currentTimeMillis();
			log.trace("SQL raw query: " + selection
					+ Arrays.toString(selectionArgs) + " / times: "
					+ (now - millis1) + ", " + (now - millis2));
			// db.close();
		}
	}

	protected SortedSet<T> parseResults(Cursor cursor) {
		SortedSet<T> results = new TreeSet<T>();
		try {

			while (!cursor.isAfterLast()) {
				if (cursor.isBeforeFirst())
					cursor.moveToNext();
				if (cursor.isAfterLast())
					break;

				results.add(parseRow(cursor));
				cursor.moveToNext();
			}
		} finally {
			cursor.close();
		}
		return results;
	}

	protected abstract T parseRow(Cursor cursor);

	protected <V> V unpackColumnValue(Cursor cursor,
			Column<SQLiteDataTypes> name, Class<V> c) {
		return unpackColumnValue(cursor, name.name, c);
	}

	@SuppressWarnings("unchecked")
	protected <V> V unpackColumnValue(Cursor cursor, String name, Class<V> c) {
		int idx = cursor.getColumnIndex(name);
		V value;
		if (cursor.isNull(idx))
			value = null;
		else if (c == Integer.class)
			value = (V) Integer.valueOf(cursor.getInt(idx));
		else if (c == Long.class)
			value = (V) Long.valueOf(cursor.getInt(idx));
		else if (c == Float.class)
			value = (V) Float.valueOf(cursor.getFloat(idx));
		else if (c == Double.class)
			value = (V) Double.valueOf(cursor.getDouble(idx));
		else if (c == String.class)
			value = (V) cursor.getString(idx);
		else if (c == byte[].class)
			value = (V) cursor.getBlob(idx);
		else if (c == Calendar.class)
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new SimpleDateFormat(DATE_FORMAT, LOCALE)
						.parse(cursor.getString(idx)));
				value = (V) cal;
			} catch (ParseException e) {
				return null;
			}
		else if (c == Locale.class) {
			value = (V) Utils.stringToLocale(cursor.getString(idx));
		} else if (c == Bundle.class) {
			BundleDao ad = new BundleDao(dbHelper);
			value = (V) ad.findById(cursor.getLong(idx)).getBundle();
		} else if (c == Address.class) {
			AddressDao ad = new AddressDao(dbHelper);
			value = (V) ad.findById(cursor.getLong(idx));
		} else if (c == DateFormat.class) {
			value = (V) new SimpleDateFormat(cursor.getString(idx), getLocale());
		} else if (c == URL.class) {
			try {
				value = (V) new URL(cursor.getString(idx));
			} catch (MalformedURLException e) {
				return null;
			}
		} else if (c.isInstance(Identificable.class)) {
			value = null;
			try {
				value = c.newInstance();
				((Identificable<V>) value).setId(cursor.getLong(idx));
			} catch (InstantiationException e) {
				log.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				log.error(e.getMessage(), e);
			}
		} else if (c == Enclosure.class) {
			value = null;
			try {
				value = c.newInstance();
				((Identificable<V>) value).setId(cursor.getLong(idx));
			} catch (InstantiationException e) {
				log.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				log.error(e.getMessage(), e);
			}
		} else {
			throw new ClassCastException();
		}
		return value;
	}

	protected SQLiteDatabase getDb() {
		return db;
	}

	protected void setDb(SQLiteDatabase db) {
		this.db = db;
	}

	public static class AndroidSQLiteColumn extends Column<SQLiteDataTypes> {

		public AndroidSQLiteColumn(String name, SQLiteDataTypes type,
				boolean pk, cz.mammahelp.handy.dao.GenericDao.ForeignKey fk) {
			super(name, type, pk, fk);
		}

		public AndroidSQLiteColumn(String name, SQLiteDataTypes type, boolean pk) {
			super(name, type, pk);
		}

		public AndroidSQLiteColumn(String name, SQLiteDataTypes type,
				cz.mammahelp.handy.dao.GenericDao.ForeignKey fk) {
			super(name, type, fk);
		}

		public AndroidSQLiteColumn(String name, SQLiteDataTypes type) {
			super(name, type);
		}

		@Override
		protected String getTypeName(SQLiteDataTypes type) {
			return type.name();
		}

	}
}
