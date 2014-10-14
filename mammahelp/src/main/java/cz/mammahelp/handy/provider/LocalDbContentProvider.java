package cz.mammahelp.handy.provider;

import static cz.mammahelp.handy.Constants.log;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.dao.NewsDao;
import cz.mammahelp.handy.model.Articles;
import cz.mammahelp.handy.model.Enclosure;
import cz.mammahelp.handy.model.News;

public class LocalDbContentProvider extends ContentProvider {

	private static final String ID_PARAM = "id";
	private static final String ENCLOSURE_PATH = "enclosure";
	private static final String ARTICLE_PATH = "article";
	private static final String NEWS_PATH = "news";

	private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static class TransferThread extends Thread {
		InputStream in;
		OutputStream out;

		TransferThread(InputStream in, OutputStream out) {
			this.in = in;
			this.out = out;
		}

		@Override
		public void run() {
			byte[] buf = new byte[8192];
			int len;

			try {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				in.close();
				out.flush();
				out.close();
			} catch (IOException e) {
				log.error("Exception transferring file", e);
			}
		}
	}

	private static final String AUTHORITY = "cz.mammahelp.handy.local.provider";
	public static final String CONTENT_BASE_URI = "content://" + AUTHORITY;

	public static final String CONTENT_ARTICLE_URI = CONTENT_BASE_URI + "/"
			+ ARTICLE_PATH;

	public static final String CONTENT_ENCLOSURE_URI = CONTENT_BASE_URI + "/"
			+ ENCLOSURE_PATH;

	public static final String CONTENT_NEWS_URI = CONTENT_BASE_URI + "/"
			+ NEWS_PATH;

	static {
		uriMatcher.addURI(AUTHORITY, ARTICLE_PATH + "/*", 1);
		uriMatcher.addURI(AUTHORITY, NEWS_PATH + "/*", 2);
		uriMatcher.addURI(AUTHORITY, ENCLOSURE_PATH + "/*", 3);
	}

	private MammaHelpDbHelper dbHelper;

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {

		log.debug("openFile: " + uri + " ... mode: " + mode);

		ParcelFileDescriptor[] pipe = null;

		try {
			pipe = ParcelFileDescriptor.createPipe();

			new TransferThread(getInputStreamFromDbFile(uri),
					new AutoCloseOutputStream(pipe[1])).start();
		} catch (IOException e) {
			log.error("Exception opening pipe", e);
			throw new FileNotFoundException("Could not open pipe for: "
					+ uri.toString());
		}

		return (pipe[0]);
	}

	private InputStream getInputStreamFromDbFile(Uri uri) {

		Long id = null;

		String idString = uri.getQueryParameter(ID_PARAM);
		if (idString == null)
			idString = uri.getLastPathSegment();

		try {
			id = Long.parseLong(idString);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		switch (uriMatcher.match(uri)) {
		case 1:
			return getInputStreamOfArticle(id);

		case 2:
			return getInputStreamOfNews(id);

		case 3:
			return getInputStreamOfEnclosure(id);

		default:
			return null;
		}

	}

	private InputStream getInputStreamOfEnclosure(Long id) {

		log.debug("Querying enclosure id " + id);

		EnclosureDao edao = new EnclosureDao(getDbHelper());
		Enclosure enclosure = edao.findById(new Enclosure(id));

		log.debug("Enclosure found: " + enclosure);

		return new ByteArrayInputStream(enclosure.getData());
	}

	private InputStream getInputStreamOfArticle(Long id) {

		ArticlesDao adao = new ArticlesDao(getDbHelper());

		Articles article = adao.findById(new Articles(id));

		StringBuilder articleHtml = new StringBuilder("<html>");
		// articleHtml
		// .append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><meta charset=\"UTF-8\">");
		articleHtml.append("<head>");
		articleHtml
				.append("<link rel='stylesheet' href='file:///android_asset/article.css' type='text/css' />");
		articleHtml.append("<title>");
		articleHtml.append(article.getTitle());
		articleHtml.append("</title>");
		articleHtml.append("</head><body>");
		articleHtml.append("<div class=\"article\">");
		articleHtml.append("<h1 class=\"title\">");
		articleHtml.append(article.getTitle());
		articleHtml.append("</h1>");
		articleHtml.append("<p class=\"body\">");
		articleHtml.append(article.getBody());
		articleHtml.append("</p>");
		articleHtml.append("</div>");
		articleHtml.append("</body></html>");

		log.debug("Article: \n" + articleHtml);

		return new ByteArrayInputStream(articleHtml.toString().getBytes(
				Charset.forName("UTF-8")));

	}

	private InputStream getInputStreamOfNews(Long id) {

		NewsDao adao = new NewsDao(getDbHelper());

		News article = adao.findById(new News(id));

		StringBuilder articleHtml = new StringBuilder("<html>");
		// articleHtml
		// .append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><meta charset=\"UTF-8\">");
		articleHtml.append("<head>");
		articleHtml
				.append("<link rel='stylesheet' href='file:///android_asset/article.css' type='text/css' />");
		articleHtml.append("<title>");
		articleHtml.append(article.getTitle());
		articleHtml.append("</title>");
		articleHtml.append("</head><body>");
		articleHtml.append("<div class=\"article\">");
		articleHtml.append("<h1 class=\"title\">");
		articleHtml.append(article.getTitle());
		articleHtml.append("</h1>");
		articleHtml.append("<p class=\"body\">");
		articleHtml.append(article.getBody());
		articleHtml.append("</p>");
		articleHtml.append("</div>");
		articleHtml.append("</body></html>");

		log.debug("Article: \n" + articleHtml);

		return new ByteArrayInputStream(articleHtml.toString().getBytes(
				Charset.forName("UTF-8")));

	}

	@Override
	public boolean onCreate() {

		log.debug("onCreate");

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri paramUri, String[] paramArrayOfString1,
			String paramString1, String[] paramArrayOfString2,
			String paramString2) {

		log.debug("query");

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType(Uri uri) {

		log.debug("getType");

		switch (uriMatcher.match(uri)) {
		case 1:
			return ARTICLE_PATH;

		case 2:
			return NEWS_PATH;

		case 3:
			return ENCLOSURE_PATH;

		default:
			return null;
		}

	}

	@Override
	public Uri insert(Uri paramUri, ContentValues paramContentValues) {

		log.debug("insert");

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri paramUri, String paramString,
			String[] paramArrayOfString) {

		log.debug("delete");

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri paramUri, ContentValues paramContentValues,
			String paramString, String[] paramArrayOfString) {

		log.debug("update");

		// TODO Auto-generated method stub
		return 0;
	}

	public MammaHelpDbHelper getDbHelper() {
		if (dbHelper == null)
			dbHelper = MammaHelpDbHelper.getInstance(getContext());
		return dbHelper;
	}

}