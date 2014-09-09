package cz.mammahelp.handy.provider;

import java.io.IOException;
import java.io.InputStream;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;

public class LocalImageContentProvider extends ContentProvider {

	public class PipeRunnable implements Runnable {

		public PipeRunnable(InputStream inputStreamFromDbFile,
				AutoCloseOutputStream autoCloseOutputStream) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	}

	private static final String AUTHORITY = "cz.mammahelp.handy.local.provider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/image");

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) {
		String filename = uri.getLastPathSegment();

		// http://developer.android.com/reference/android/os/ParcelFileDescriptor.html#createPipe%28%29
		// [0] is the reader, [1] is the writer
		ParcelFileDescriptor[] pipe = null;

		try {
			pipe = ParcelFileDescriptor.createPipe();

			// copy DB file through pipe to WebView
			new Thread(new PipeRunnable(getInputStreamFromDbFile(filename),
					new AutoCloseOutputStream(pipe[1]))).start();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return (pipe[0]);
	}

	private InputStream getInputStreamFromDbFile(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri paramUri, String[] paramArrayOfString1,
			String paramString1, String[] paramArrayOfString2,
			String paramString2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType(Uri paramUri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri paramUri, ContentValues paramContentValues) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri paramUri, String paramString,
			String[] paramArrayOfString) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri paramUri, ContentValues paramContentValues,
			String paramString, String[] paramArrayOfString) {
		// TODO Auto-generated method stub
		return 0;
	}
}