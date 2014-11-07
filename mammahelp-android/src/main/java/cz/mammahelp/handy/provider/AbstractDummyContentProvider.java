package cz.mammahelp.handy.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

public abstract class AbstractDummyContentProvider extends ContentProvider {

	public static Logger log = LoggerFactory
			.getLogger(AbstractDummyContentProvider.class);

	protected final static String[] OPENABLE_PROJECTION = {
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
			streamCopy(in, out);
		}

	}

	protected void streamCopy(InputStream in, OutputStream out) {
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
				log.error("Exception closing input stream: " + e.getMessage(),
						e);
			}
		}
	}

	@Override
	public boolean onCreate() {
		log.debug("onCreate");
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

	protected AssetFileDescriptor serveFileThroughCache(InputStream inputStream, String path)
			throws IOException {

		final File cacheFile = new File(getContext().getCacheDir(), path);
		cacheFile.getParentFile().mkdirs();
		try {
			final FileOutputStream fileOutputStream = new FileOutputStream(
					cacheFile, false);
			try {
				streamCopy(inputStream, fileOutputStream);
			} finally {
				fileOutputStream.close();
			}
		} finally {
			inputStream.close();
		}
		return
		new AssetFileDescriptor(ParcelFileDescriptor.open(
				cacheFile, ParcelFileDescriptor.MODE_READ_ONLY), 0, -1);
	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode)
			throws FileNotFoundException {
		log.debug("openAssetFile: " + uri + " ... mode: " + mode);

		return super.openAssetFile(uri, mode);
	}

}