package cz.mammahelp.handy.dao;

import java.util.Collection;
import java.util.Locale;
import java.util.SortedSet;

import cz.mammahelp.model.Identificable;

public abstract class GenericDao<T extends Identificable<T>> {

	public static final Locale LOCALE = Locale.ENGLISH;
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public Locale locale;

	public Locale getLocale() {
		if (locale == null)
			return Locale.getDefault();
		return locale;
	}

	protected abstract String getTableName();

	public abstract void insert(T obj);

	public abstract void update(T obj, boolean updateNull);

	public abstract void update(T obj);

	public abstract void update(Collection<T> objs, boolean updateNull);

	public void update(Collection<T> objs) {
		update(objs, true);
	}

	public void deleteAll(Collection<T> obj) {
		for (T t : obj) {
			delete(t);
		}
	}

	public void deleteAllById(Collection<Long> obj) {
		for (Long t : obj) {
			delete(t);
		}
	}

	public void delete(T obj) {
		delete(obj.getId());
	}

	public abstract void delete(Long id);

	public SortedSet<T> findAll() {
		return query(null, null, null, null, null);
	}

	public T findById(T obj) {
		return findById(obj.getId());
	}

	protected abstract SortedSet<T> query(String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy);

	public abstract T findById(long obj);

	protected abstract SortedSet<T> rawQuery(String selection,
			String[] selectionArgs);

	protected abstract String[] getColumnNames();

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public abstract void insert(Collection<T> objs);

	
}
