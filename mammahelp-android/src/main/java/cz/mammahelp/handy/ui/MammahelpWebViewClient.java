package cz.mammahelp.handy.ui;

import java.net.URL;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cz.mammahelp.GeneralConstants;
import cz.mammahelp.Utils;
import cz.mammahelp.handy.AndroidConstants;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.feeder.LocationFeeder;
import cz.mammahelp.handy.ui.fragment.CenterDetailViewFragment;
import cz.mammahelp.model.LocationPoint;

public class MammahelpWebViewClient extends WebViewClient {

	private Context context;
	private static Logger log = LoggerFactory
			.getLogger(MammahelpWebViewClient.class);

	public MammahelpWebViewClient(Context context) {
		this.context = context;

	}

	private void openCenterDetail(long id) {
		if (context instanceof Activity) {
			CenterDetailViewFragment af = new CenterDetailViewFragment();
			Bundle args = new Bundle();
			args.putLong(AndroidConstants.CENTER_KEY, id);
			af.setArguments(args);
			((Activity) context).getFragmentManager().beginTransaction()
					.add(R.id.container, af).addToBackStack(null).commit();
		}
	}

	private void loadCentersIfEmpty(final long id) {
		if (context instanceof AbstractMammaHelpActivity) {
			LocationPointDao ld = new LocationPointDao(
					((AbstractMammaHelpActivity) context).getDbHelper());
			SortedSet<LocationPoint> l;
			try {
				l = ld.findAll();

				if (l == null || l.isEmpty()) {
					log.debug("Loading default locations...");
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							LocationFeeder lf = new LocationFeeder(context);
							try {
								lf.setUrl(new URL(
										"file:///android_res/raw/locations.xml"));
								lf.feedData();
								openCenterDetail(id);
							} catch (Exception e) {
								log.error(
										"Unable to load locations: "
												+ e.getMessage(), e);
							}
						}
					});
					t.start();
				} else {
					openCenterDetail(id);
				}
			} catch (Exception e) {
				log.error("Unable get Centers: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {

		if (url == null)
			super.shouldOverrideUrlLoading(view, url);

		if (url.startsWith(Utils
				.makeContentUri(GeneralConstants.ENCLOSURE_CONTENT))) {
			log.debug("force view enclosure: " + url);
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			ContentResolver ressolver = view.getContext().getContentResolver();
			String type = ressolver.getType(Uri.parse(url));
			intent.setDataAndType(Uri.parse(url), type);
			intent.putExtra(Intent.EXTRA_STREAM, url);
			view.getContext().startActivity(intent);
			return true;
		} else if (url.startsWith("content://"
				+ GeneralConstants.CONTENT_URI_PREFIX + "center/")) {
			loadCentersIfEmpty(Long.parseLong(Uri.parse(url)
					.getLastPathSegment()));

			return true;
		} else if (url.startsWith("content://"
				+ GeneralConstants.CONTENT_URI_PREFIX)) {
			// view.loadUrl(url);
			log.debug("let decide nativelly: " + url);
			return super.shouldOverrideUrlLoading(view, url);
		} else {
			log.debug("force view: " + url);
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			context.startActivity(i);
			return true;
		}
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		WebResourceResponse response = super.shouldInterceptRequest(view, url);
		log.debug("Intercepted response: " + response);
		return response;
	}

}
