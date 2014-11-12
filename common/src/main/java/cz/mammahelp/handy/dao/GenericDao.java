package cz.mammahelp.handy.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;

import cz.mammahelp.model.Identificable;

public abstract class GenericDao<T extends Identificable<T>> {

	public static Column<?> ID;

	protected static Map<String, Table> tables = new HashMap<String, Table>();

	public static class Table {

		private String name;
		private List<Column<?>> columns = new ArrayList<Column<?>>();
		private List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();
		private String appendix;

		public Table(String name) {
			this.name = name;
		}

		public void addColumn(Column<?> col) {
			columns.add(col);
			if (col.getFk() != null) {
				if (col.getFk().isUnset())
					col.getFk().setColumn(col);
				foreignKeys.add(col.getFk());
			}
		}

		public String createClausule() {
			StringBuffer sb = new StringBuffer("create table ");
			sb.append(name);
			sb.append("(");
			Iterator<Column<?>> ci = columns.iterator();

			while (ci.hasNext()) {
				Column<?> c = ci.next();
				sb.append(c.createClausule());
				if (ci.hasNext())
					sb.append(",");
			}

			for (ForeignKey fk : foreignKeys) {
				sb.append(",");
				sb.append(fk.createClausule());

			}

			sb.append(getAppendix());
			sb.append(")");
			return sb.toString();
		}

		public String getAppendix() {
			return appendix == null ? "" : appendix;
		}

		public void setAppendix(String appendix) {
			this.appendix = appendix;
		}

		public String getName() {
			return name;
		}

		public String[] getColumnNames() {
			List<String> colNames = new ArrayList<String>();
			for (Column<?> col : columns) {
				colNames.add(col.getName());
			}
			return colNames.toArray(new String[0]);
		}

		public Column<?>[] getColumns() {
			return columns.toArray(new Column[] {});
		}
	}

	public abstract static class Column<T> {

		String name;
		private T type;
		private boolean pk;
		private ForeignKey fk;

		public Column(String name, T type, boolean pk) {
			this(name, type, pk, null);
		}

		public Column(String name, T type, ForeignKey fk) {
			this(name, type, false, fk);
		}

		public Column(String name, T type) {
			this(name, type, false, null);
		}

		public Column(String name, T type, boolean pk, ForeignKey fk) {
			this.name = name;
			this.type = type;
			this.pk = pk;
			this.fk = fk;
		}

		@Override
		public String toString() {
			return getName();
		}

		public String createClausule() {
			StringBuffer sb = new StringBuffer();
			sb.append(name);
			sb.append(" ");
			sb.append(getTypeName(type));
			if (pk)
				sb.append(" primary key");
			return sb.toString();
		}

		protected abstract String getTypeName(T type);

		public ForeignKey getFk() {
			return fk;
		}

		public String getName() {
			return name;
		}

		public T getType() {
			return type;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Column) {
				return toString().equals(o.toString());
			}
			return false;
		}

	}

	public static class ForeignKey {

		private Column<?> target;
		private Table table;
		private Column<?> source;

		public ForeignKey(String table, Column<?> column) {
			this.table = getTable(table);
			this.target = column;
		}

		public boolean isUnset() {
			return source == null;
		}

		public ForeignKey(Table table, Column<?> column) {
			this.table = table;
			this.target = column;
		}

		public ForeignKey(Class<?> class1) {
			try {
				this.table = (Table) class1.getMethod("getTable").invoke(null);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.target = ID;
		}

		public void setColumn(Column<?> column) {
			this.source = column;
		}

		public Object createClausule() {
			StringBuffer sb = new StringBuffer();
			sb.append("foreign key(");
			sb.append(source.getName());
			sb.append(") references ");
			sb.append(table.getName());
			sb.append("(");
			sb.append(target.getName());
			sb.append(")");
			return sb.toString();
		}
	}

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

	public abstract void update(Collection<T> objs, boolean updateNull);

	public void update(T obj) {
		update(obj, true);
	}

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

	public static Table getTable(String name) {
		return tables.get(name);
	}

	public static String columnNamesToClause(String alias, Column<?>[] columns) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < columns.length; i++) {
			if (i > 0)
				sb.append(", ");
			if (alias != null) {
				sb.append(alias);
				sb.append(".");
			}
			sb.append(columns[i]);
		}
		return sb.toString();

	}

	protected static Table registerTable(String name) {
		Table table = new Table(name);
		tables.put(name, table);
		return table;
	}

}
