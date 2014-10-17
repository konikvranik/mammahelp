package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.DAY_IN_MILLIS;
import static cz.mammahelp.handy.Constants.HOUR_IN_MILLIS;
import static cz.mammahelp.handy.Constants.MINUTE_IN_MILLIS;
import static cz.mammahelp.handy.Constants.WEEK_IN_MILLIS;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mammahelp.handy.R;
import cz.mammahelp.handy.Utils;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class IntervalPreference extends DialogPreference {

	private SeekBar myView;
	private String[] values;
	private int titleRes;
	private Long defVal = (long) -1;
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory
			.getLogger(IntervalPreference.class);

	public IntervalPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(attrs);
	}

	public IntervalPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(attrs);
	}

	private void setup(AttributeSet attrs) {
		values = getContext().getResources().getStringArray(
				attrs.getAttributeResourceValue("http://suteren.net/",
						"values", -1));
		titleRes = attrs.getAttributeResourceValue(
				"http://schemas.android.com/apk/res/android", "title", -1);
	}

	@Override
	protected void onBindDialogView(final View view) {
		super.onBindDialogView(view);

		myView = (SeekBar) view.findViewById(R.id.value);

		myView.setMax(values.length - 1);
		long sv = getPersistedLong(defVal);
		for (int i = 0; i < values.length; i++) {
			long v = Long.parseLong(values[i]);
			if (sv <= v) {
				myView.setProgress(i);
				break;
			}
		}

		updateDisplay(view, sv);

		myView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar paramSeekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar paramSeekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar paramSeekBar, int paramInt,
					boolean paramBoolean) {
				long value = Long.parseLong(values[paramInt]);

				updateDisplay(view, value);
			}

		});
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (Long.parseLong(a.getString(index)));
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		defVal = (Long) defaultValue;
		if (defVal == null)
			defVal = (long) -1;
		super.onSetInitialValue(restorePersistedValue, getPersistedLong(defVal));
	}

	private void updateDisplay(final View view, long value) {
		TextView displayValue = (TextView) view.findViewById(R.id.display);
		String valueString = prettyPrintValue(value);
		displayValue.setText(valueString);
	}

	private String prettyPrintValue(long value) {
		String valueString = "-";

		if (value < 0)
			valueString = getContext().getResources().getString(R.string.never);
		else if (value >= WEEK_IN_MILLIS && (value % WEEK_IN_MILLIS) == 0) {
			valueString = String.format(Locale.getDefault(), "%d %s", value
					/ WEEK_IN_MILLIS, Utils.getPlural(getContext()
					.getResources(), R.array.weeks, value / WEEK_IN_MILLIS));

		} else if (value >= DAY_IN_MILLIS && (value % DAY_IN_MILLIS) == 0) {
			valueString = String.format(Locale.getDefault(), "%d %s", value
					/ DAY_IN_MILLIS, Utils.getPlural(getContext()
					.getResources(), R.array.days, value / DAY_IN_MILLIS));
		} else if (value >= HOUR_IN_MILLIS && (value % HOUR_IN_MILLIS) == 0) {
			valueString = String.format(Locale.getDefault(), "%d %s", value
					/ HOUR_IN_MILLIS, Utils.getPlural(getContext()
					.getResources(), R.array.hours, value / HOUR_IN_MILLIS));
		} else if (value >= MINUTE_IN_MILLIS && (value % MINUTE_IN_MILLIS) == 0) {
			valueString = String
					.format(Locale.getDefault(), "%d %s", value
							/ MINUTE_IN_MILLIS, Utils.getPlural(getContext()
							.getResources(), R.array.minutes, value
							/ MINUTE_IN_MILLIS));
		}
		return valueString;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (!positiveResult)
			return;

		long result = Long.parseLong(values[myView.getProgress()]);

		Editor e = getEditor();
		e.putLong(getKey(), result);
		e.commit();
		notifyChanged();
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		TextView title = (TextView) view.findViewById(android.R.id.title);

		title.setText(getContext().getResources().getString(titleRes) + ": "
				+ prettyPrintValue(getPersistedLong(defVal)));

		// view.findViewById(android.R.id.summary);
		// view.findViewById(android.R.id.widget_frame);
		// view.findViewById(android.R.id.icon);

		// debugViewIds(view);
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

}
