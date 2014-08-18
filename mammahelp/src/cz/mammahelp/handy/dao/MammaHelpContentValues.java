package cz.mammahelp.handy.dao;

import cz.mammahelp.handy.dao.BaseDao.Column;
import android.content.ContentValues;

public class MammaHelpContentValues {

	ContentValues values;
	private boolean updateNull;

	public MammaHelpContentValues(boolean updateNull) {
		setUpdateNull(updateNull);
	}

	public void setUpdateNull(boolean updateNull) {
		this.updateNull = updateNull;
	}

	public ContentValues getValues() {
		return values;
	}

	@SuppressWarnings("rawtypes")
	public void put(Column column, Object obj) {
		if (obj != null || updateNull) {
			String value;
			if (obj.getClass().isEnum()) {
				value = String.valueOf(((Enum) obj).ordinal());
			} else {
				value = String.valueOf(obj);
			}
			values.put(column.getName(), value);
		}
	}
}
