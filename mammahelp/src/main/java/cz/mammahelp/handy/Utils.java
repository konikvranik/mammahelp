package cz.mammahelp.handy;

import java.util.Locale;
import java.util.StringTokenizer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.webkit.WebView;

public class Utils {

	public Utils() {
	}

	public static Locale stringToLocale(String s) {
		if (s == null)
			return null;
		StringTokenizer tempStringTokenizer = new StringTokenizer(s, "_");
		String l = null;
		if (tempStringTokenizer.hasMoreTokens())
			l = (String) tempStringTokenizer.nextElement();
		String c = null;
		if (tempStringTokenizer.hasMoreTokens())
			c = (String) tempStringTokenizer.nextElement();
		return new Locale(l, c);
	}

	public static String getPlural(Resources res, int key, long count) {
		int[] plurals = res.getIntArray(R.array.plurals);
		int position = 0;
		for (position = 0; position < plurals.length
				&& plurals[position] <= Math.abs(count); position++)
			;
		if (position > plurals.length)
			position = plurals.length - 1;

		return res.getStringArray(key)[position];
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void transparencyHack(Context ctx, WebView usage) {
		usage.setBackgroundColor(ctx.getResources().getColor(
				android.R.color.transparent));
		usage.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
	}

	public static <T extends Comparable<T>> int compare(T o1, T o2) {
		if (o1 == null) {
			if (o2 == null)
				return 0;
			else
				return -1;
		} else {
			if (o2 == null)
				return 1;
			else
				return o1.compareTo((T) o2);
		}
	}
}