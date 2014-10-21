package cz.mammahelp.handy.provider;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.model.Enclosure;

public class EnclosureContentProvider extends LocalDbContentProvider<Enclosure> {

	public static final String AUTHORITY = "cz.mammahelp.handy.enclosure";
	public static final String CONTENT_BASE_URI = "content://" + AUTHORITY;
	public static final String CONTENT_ENCLOSURE_URI = CONTENT_BASE_URI
			+ "/data/";

	public static Logger log = LoggerFactory
			.getLogger(EnclosureContentProvider.class);

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {

		log.debug("openFile: " + uri + " ... mode: " + mode);

		ParcelFileDescriptor[] pipe = null;

		try {
			pipe = ParcelFileDescriptor.createPipe();

			new TransferThread(getInputStreamOfEnclosure(uri),
					new AutoCloseOutputStream(pipe[1])).start();
		} catch (IOException e) {
			log.error("Exception opening pipe", e);
			throw new FileNotFoundException("Could not open pipe for: "
					+ uri.toString());
		}

		return (pipe[0]);
	}

	private InputStream getInputStreamOfEnclosure(Uri uri) {

		Enclosure enclosure = getEnclosureFromUri(uri);

		return new ByteArrayInputStream(enclosure.getData());
	}

	private Enclosure getEnclosureFromUri(Uri uri) {
		Long id = getIdFromUri(uri);

		log.debug("Querying enclosure id " + id);

		EnclosureDao edao = new EnclosureDao(getDbHelper());
		Enclosure enclosure = edao.findById(new Enclosure(id));
		return enclosure;
	}

	@Override
	public String getType(Uri uri) {
		log.debug("getType");
		return getEnclosureFromUri(uri).getType();
	}

}