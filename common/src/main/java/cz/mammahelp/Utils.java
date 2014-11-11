package cz.mammahelp;

import java.util.Locale;
import java.util.StringTokenizer;

import static cz.mammahelp.GeneralConstants.*;

public class Utils {

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

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	public static double distance(double lat1, double lon1, double lat2,
			double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'k') {
			dist = dist * 1.609344;
		} else if (unit == 'm') {
			dist = dist * 1609.344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	public static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
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

	public static String makeContentUri(String type, Long id) {
		return String.format(CONTENT_URI, type, id);
	}

	public static String makeContentUri(String type) {
		return String.format(CONTENT_BASE_URI, type);
	}

}
