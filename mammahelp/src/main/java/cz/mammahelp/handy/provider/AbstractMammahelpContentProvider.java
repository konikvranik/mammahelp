package cz.mammahelp.handy.provider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import cz.mammahelp.handy.MammaHelpDbHelper;
import cz.mammahelp.handy.dao.BaseDao;
import cz.mammahelp.handy.model.Identificable;

public abstract class AbstractMammahelpContentProvider<T extends Identificable<T>>
		extends AbstractDummyContentProvider {

	public static Logger log = LoggerFactory
			.getLogger(AbstractMammahelpContentProvider.class);

	protected static final String ID_PARAM = "id";

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

	public MammaHelpDbHelper getDbHelper() {
		if (dbHelper == null)
			dbHelper = MammaHelpDbHelper.getInstance(getContext());
		return dbHelper;
	}

}