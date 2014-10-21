package cz.mammahelp.handy.provider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.provider.OpenableColumns;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.dao.BaseDao;
import cz.mammahelp.handy.model.Identificable;

public abstract class AbstractMammahelpContentProvider<T extends Identificable<T>>
		extends ContentProvider {

	public static Logger log = LoggerFactory
			.getLogger(AbstractMammahelpContentProvider.class);

	protected static final String ID_PARAM = "id";

	private final static String[] OPENABLE_PROJECTION = {
			OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };

	protected class TransferThread extends Thread {
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
				while ((len = in.read(buf)) >= 0) {
					if (len > 0) {
						log.debug("Written " + len + " bytes");
						out.write(buf, 0, len);
					}
				}
			} catch (IOException e) {
				log.error("Exception transferring file: " + e.getMessage(), e);
			} finally {
				try {
					in.close();
					out.flush();
					out.close();
				} catch (IOException e) {
					log.error(
							"Exception closing input stream: " + e.getMessage(),
							e);
				}
			}
		}
	}

	private MammaHelpDbHelper dbHelper;

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {

		log.debug("openFile: " + uri + " ... mode: " + mode);

		ParcelFileDescriptor[] pipe = null;

		try {
			pipe = ParcelFileDescriptor.createPipe();

			OutputStream out = new AutoCloseOutputStream(pipe[1]);
			new TransferThread(getInputStreamFromUri(uri), out).start();
		} catch (IOException e) {
			log.error("Exception opening pipe", e);
			throw new FileNotFoundException("Could not open pipe for: "
					+ uri.toString());
		}

		return (pipe[0]);
	}

	protected abstract InputStream getInputStreamFromUri(Uri uri);

	protected Long getIdFromUri(Uri uri) {
		Long id = null;

		String idString = uri.getQueryParameter(ID_PARAM);
		if (idString == null)
			idString = uri.getLastPathSegment();

		try {
			id = Long.parseLong(idString);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return id;
	}

	protected T getObjectFromUri(Uri uri) {
		Long id = getIdFromUri(uri);
		log.debug("Querying object id " + id);
		return getDao().findById(id);
	}

	protected abstract BaseDao<T> getDao();

	@Override
	public boolean onCreate() {

		log.debug("onCreate");

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		log.debug("query");

		if (projection == null) {
			projection = OPENABLE_PROJECTION;
		}

		final MatrixCursor cursor = new MatrixCursor(projection, 1);

		MatrixCursor.RowBuilder b = cursor.newRow();

		for (String col : projection) {
			if (OpenableColumns.DISPLAY_NAME.equals(col)) {
				b.add(getFileName(uri));
			} else if (OpenableColumns.SIZE.equals(col)) {
				b.add(getDataLength(uri));
			} else { // unknown, so just add null
				b.add(null);
			}
		}

		return (cursor);
	}

	protected abstract Long getDataLength(Uri uri);

	protected String getFileName(Uri uri) {
		return (uri.getLastPathSegment());
	}

	@Override
	public Uri insert(Uri paramUri, ContentValues paramContentValues) {

		log.debug("insert");

		throw new RuntimeException("Operation not supported");
	}

	@Override
	public int delete(Uri paramUri, String paramString,
			String[] paramArrayOfString) {

		log.debug("delete");

		throw new RuntimeException("Operation not supported");
	}

	@Override
	public int update(Uri paramUri, ContentValues paramContentValues,
			String paramString, String[] paramArrayOfString) {

		log.debug("update");

		throw new RuntimeException("Operation not supported");
	}

	public MammaHelpDbHelper getDbHelper() {
		if (dbHelper == null)
			dbHelper = MammaHelpDbHelper.getInstance(getContext());
		return dbHelper;
	}

}