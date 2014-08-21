/**
 * 
 */
package cz.mammahelp.handy.ui;

import static cz.mammahelp.handy.Constants.EXCEPTION;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import cz.mammahelp.handy.MammaHelpException;
import cz.mammahelp.handy.R;
import cz.mammahelp.handy.Utils;

/**
 * @author jd39426
 * 
 */
public class ErrorViewActivity extends AbstractMammaHelpActivity {

	private String text;
	private MammaHelpException exception;

	private static Logger log = LoggerFactory
			.getLogger(ErrorViewActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		log.debug("ErrorViewActivity onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.errorview);

		Bundle params = getIntent().getExtras();
		MammaHelpException e = (MammaHelpException) params
				.getSerializable(EXCEPTION);

		setException(e);
		WebView errorView = (WebView) getWindow().findViewById(R.id.error);
		setText(getKnownCause(getException()));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			Utils.transparencyHack(getApplicationContext(), errorView);
		else
			errorView.setBackgroundColor(getResources().getColor(
					android.R.color.black));

		errorView.loadDataWithBaseURL("", getHtmlText(), "text/html", "UTF-8",
				"");

	}

	private String getHtmlText() {

		StringBuffer sb = new StringBuffer("<html><head>");
		sb.append("<link rel='stylesheet' href='restaurant.css' type='text/css' />");
		sb.append("</head><body>");
		sb.append(getText());
		sb.append("</body></html>");
		return sb.toString();
	}

	private void renderStacktrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		StringBuffer sb = new StringBuffer("<h1>");
		sb.append(t.getMessage());
		sb.append("</h1><pre>");
		sb.append(StringEscapeUtils.escapeHtml4(sw.toString()));
		sb.append("</pre>");
		setText(sb.toString());
	}

	private Throwable getKnownCause(Throwable e) {
		Throwable exception = e;
		do {
			if (exception instanceof MammaHelpException)
				return exception;
		} while ((exception = exception.getCause()) != null);
		return e;
	}

	public void setException(MammaHelpException e) {
		this.exception = e;
	}

	public MammaHelpException getException() {
		return exception;
	}

	public String getText() {
		return text;
	}

	public void setText(Throwable cause) {
		 if (cause instanceof MammaHelpException) {

			setText(((MammaHelpException) cause)
					.toString(getApplicationContext()));

			renderStacktrace(getException());

		} else {
			renderStacktrace(getException());

		}

	}

	public void setText(String sb) {
		this.text = sb;
	}

	public void sendTo(View v) {
		startActivity(new Intent(android.content.Intent.ACTION_SENDTO,
				Uri.parse("exception://"
						+ getException().getClass().getCanonicalName()
						+ "/?message="
						+ URLEncoder.encode(getException().getMessage())))
				.putExtra(Intent.EXTRA_TEXT, getText()).setType("text/plain"));

	}

	public void sendError(View v) throws MammaHelpException {
		throw getException();
	}
}
