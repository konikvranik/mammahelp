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
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.dao.BaseDao;
import cz.mammahelp.handy.model.Identificable;

public abstract class AbstractMammahelpContentProvider<T extends Identificable<T>>
		extends ContentProvider {

	public static Logger log = LoggerFactory
			.getLogger(AbstractMammahelpContentProvider.class);

	protected static final String ID_PARAM = "id";

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
				while ((len = in.read(buf)) > 0) {
					log.debug("Written " + len + " bytes");
					out.write(buf, 0, len);
				}

				out.flush();
				in.close();
			} catch (IOException e) {
				log.error("Exception transferring file: " + e.getMessage(), e);
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

			new TransferThread(getInputStreamFromUri(uri),
					new AutoCloseOutputStream(pipe[1])).start();
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
	public Cursor query(Uri paramUri, String[] paramArrayOfString1,
			String paramString1, String[] paramArrayOfString2,
			String paramString2) {

		log.debug("query");

		// TODO Auto-generated method stub
		return null;
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