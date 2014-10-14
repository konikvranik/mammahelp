package cz.mammahelp.handy.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MammahelpWebViewClient extends WebViewClient {

	private Context context;

	public MammahelpWebViewClient(Context context) {
		this.context = context;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {

		if (url == null)
			super.shouldOverrideUrlLoading(view, url);

		if (url.startsWith("http")) {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			context.startActivity(i);
			return true;
		}
		// view.loadUrl(url);

		return super.shouldOverrideUrlLoading(view, url);

	}

}
