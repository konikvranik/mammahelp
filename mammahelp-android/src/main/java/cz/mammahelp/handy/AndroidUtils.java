package cz.mammahelp.handy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class AndroidUtils {

	private static Logger log = LoggerFactory.getLogger(AndroidUtils.class);

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

	public static void setupBrowser(WebView wv) {

		WebSettings s = wv.getSettings();
		s.setJavaScriptEnabled(false);
		s.setDefaultTextEncodingName("utf-8");
		s.setBlockNetworkImage(false);
		s.setBlockNetworkLoads(true);
		s.setAllowContentAccess(true);
		s.setAllowFileAccess(true);
		s.setBuiltInZoomControls(true);
		s.setDisplayZoomControls(false);
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
					context, AndroidConstants.REQUEST_CODE_RECOVER_PLAY_SERVICES)
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
		log.debug("All providers: "
				+ Arrays.toString(lm.getAllProviders().toArray()));
		log.debug("Available providers: "
				+ Arrays.toString(lm.getProviders(false).toArray()));
		log.debug("Enables providers: "
				+ Arrays.toString(lm.getProviders(true).toArray()));
		String prov = lm.getBestProvider(criteria, true);
		log.debug("Location provider is: " + prov);
		Location loc = null;
		if (prov != null)
			loc = lm.getLastKnownLocation(prov);
		if (loc == null)
			loc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		if (loc == null)
			loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (loc == null)
			loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		log.debug("Last known location: " + loc);
		return loc;

	}
}