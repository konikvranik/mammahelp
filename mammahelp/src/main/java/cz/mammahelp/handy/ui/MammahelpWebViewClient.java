package cz.mammahelp.handy.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cz.mammahelp.handy.Constants;
import cz.mammahelp.handy.provider.EnclosureContentProvider;

public class MammahelpWebViewClient extends WebViewClient {

	private Context context;

	public MammahelpWebViewClient(Context context) {
		this.context = context;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {

		if (url == null)
			super.shouldOverrideUrlLoading(view, url);

		if (url.startsWith(EnclosureContentProvider.CONTENT_URI)) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			ContentResolver ressolver = view.getContext().getContentResolver();
			String type = ressolver.getType(Uri.parse(url));
			intent.setDataAndType(Uri.parse(url), type);
			intent.putExtra(Intent.EXTRA_STREAM, url);
			view.getContext().startActivity(intent);
			return true;
		} else if (url.startsWith(Constants.CONTENT_URI_PREFIX)) {
			// view.loadUrl(url);
			return super.shouldOverrideUrlLoading(view, url);
		} else {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			context.startActivity(i);
			return true;
		}
	}

}
