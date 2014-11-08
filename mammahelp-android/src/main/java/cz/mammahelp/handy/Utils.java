package cz.mammahelp.handy;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import cz.mammahelp.model.Address;

public class Utils {

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

	public static void setupBrowser(WebView wv) {

		WebSettings s = wv.getSettings();
		s.setJavaScriptEnabled(false);
		s.setDefaultTextEncodingName("utf-8");
		s.setBlockNetworkImage(false);
		s.setBlockNetworkLoads(true);
		s.setAllowContentAccess(true);
		s.setAllowFileAccess(true);
		s.setBuiltInZoomControls(true);
		s.setDisplayZoomControls(true);
		s.setJavaScriptEnabled(false);
		s.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		s.setLoadWithOverviewMode(true);
		s.setSupportZoom(true);

		s.setAppCacheEnabled(false);

		// s.setAllowFileAccessFromFileURLs(true);
		// s.setAllowUniversalAccessFromFileURLs(false);

	}

	public static Map<String, String> bundleToMap(Bundle bundle) {
		if (bundle == null)
			return null;
		Map<String, String> map = new HashMap<String, String>();
		for (String key : bundle.keySet()) {
			map.put(key, bundle.get(key).toString());
		}
		return map;
	}

	public static Bundle mapToBundle(Map<String, String> map) {
		if (map == null)
			return null;
		Bundle bundle = new Bundle();
		for (Entry<String, String> entry : map.entrySet()) {
			bundle.putString(entry.getKey(), entry.getValue());
		}
		return bundle;
	}

	public static Address gAddresToMhAddress(android.location.Address gaddr) {
		if (gaddr == null)
			return null;
		Address mhaddr = new Address();
		mhaddr.setAdminArea(gaddr.getAdminArea());
		mhaddr.setCountryCode(gaddr.getCountryCode());
		mhaddr.setCountryName(gaddr.getCountryName());
		mhaddr.setExtras(bundleToMap(gaddr.getExtras()));
		mhaddr.setFeatureName(gaddr.getFeatureName());
		mhaddr.setLatitude(gaddr.getLatitude());
		mhaddr.setLongitude(gaddr.getLongitude());
		mhaddr.setLocality(gaddr.getLocality());
		mhaddr.setPhone(gaddr.getPhone());
		mhaddr.setPostalCode(gaddr.getPostalCode());
		mhaddr.setPremises(gaddr.getPremises());
		mhaddr.setSubAdminArea(gaddr.getSubAdminArea());
		mhaddr.setSubLocality(gaddr.getSubLocality());
		mhaddr.setSubThoroughfare(gaddr.getSubThoroughfare());
		mhaddr.setThoroughfare(gaddr.getThoroughfare());
		mhaddr.setUrl(gaddr.getUrl());
		for (int i = 0; i <= gaddr.getMaxAddressLineIndex(); i++) {
			mhaddr.setAddressLine(i, gaddr.getAddressLine(i));
		}
		return mhaddr;
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	public static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
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

	public static void checkGooglePlayServices(Activity context) {
		int checkGooglePlayServices = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
			// google play services is missing!!!!
			/*
			 * Returns status code indicating whether there was an error. Can be
			 * one of following in ConnectionResult: SUCCESS, SERVICE_MISSING,
			 * SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED,
			 * SERVICE_INVALID.
			 */
			GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
					context, Constants.REQUEST_CODE_RECOVER_PLAY_SERVICES)
					.show();
		}
	}

	public static Location getPosition(Context context) {
		LocationManager lm = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		String prov = lm.getBestProvider(criteria, true);
		if (prov != null)
			return lm.getLastKnownLocation(prov);
		else
			return null;
	}
}