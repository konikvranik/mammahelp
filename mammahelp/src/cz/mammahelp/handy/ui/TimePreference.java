package cz.mammahelp.handy.ui;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {
	private Calendar calendar;
	private TimePicker picker = null;

	public TimePreference(Context ctxt) {
		this(ctxt, null);
	}

	public TimePreference(Context ctxt, AttributeSet attrs) {
		this(ctxt, attrs, 0);
	}

	public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
		super(ctxt, attrs, defStyle);

		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		calendar = new GregorianCalendar();
	}

	@Override
	protected View onCreateDialogView() {
		picker = new TimePicker(getContext());
		return (picker);
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
			calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

			setSummary(getSummary());
			if (callChangeListener(calendar.getTimeInMillis())) {
				persistLong(calendar.getTimeInMillis());
				notifyChanged();
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (a.getString(index));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		if (restoreValue) {
			if (defaultValue == null) {
				calendar.setTimeInMillis(getPersistedLong(System
						.currentTimeMillis()));
			} else {
				calendar.setTimeInMillis(parseTime(getPersistedString((String) defaultValue)));
			}
		} else {
			if (defaultValue == null) {
				calendar.setTimeInMillis(System.currentTimeMillis());
			} else {
				calendar.setTimeInMillis(parseTime((String) defaultValue));
			}
		}
		setSummary(getSummary());
	}

	private long parseTime(String persistedString) {

		Date d = new Date();
		try {
			d = DateFormat.getTimeFormat(getContext()).parse(persistedString);
		} catch (ParseException e) {
		}

		return d.getTime();
	}

	@Override
	public CharSequence getSummary() {
		if (calendar == null) {
			return null;
		}
		return DateFormat.getTimeFormat(getContext()).format(
				new Date(calendar.getTimeInMillis()));
	}
}