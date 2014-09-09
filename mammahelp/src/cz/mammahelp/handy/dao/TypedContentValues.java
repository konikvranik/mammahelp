package cz.mammahelp.handy.dao;

import android.content.ContentValues;
import cz.mammahelp.handy.dao.BaseDao.Column;
import cz.mammahelp.handy.model.Identificable;

public class TypedContentValues {

	ContentValues values = new ContentValues();
	private boolean updateNull;

	public TypedContentValues(boolean updateNull) {
		setUpdateNull(updateNull);
	}

	public void setUpdateNull(boolean updateNull) {
		this.updateNull = updateNull;
	}

	public ContentValues getValues() {
		return values;
	}

	public void put(Column column, Object obj) {
		if (obj != null || updateNull) {
			String value;
			if (obj == null) {
				value = null;
			} else if (obj.getClass().isEnum()) {
				value = String.valueOf(((Enum<?>) obj).ordinal());
			} else if (obj instanceof Identificable<?>) {
				value = String.valueOf(((Identificable<?>) obj).getId());
			} else {
				value = String.valueOf(obj);
			}
			values.put(column.getName(), value);
		}
	}
}
