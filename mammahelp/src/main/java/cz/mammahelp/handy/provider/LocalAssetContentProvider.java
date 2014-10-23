package cz.mammahelp.handy.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class LocalAssetContentProvider extends AbstractDummyContentProvider {

	@Override
	protected Long getDataLength(Uri uri) {
		return AssetFileDescriptor.UNKNOWN_LENGTH;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return "application/octet-stream";
	}

	@Override
	public AssetFileDescriptor openAssetFile(final Uri uri, final String mode)
			throws FileNotFoundException {
		final String assetPath = getPathFromUri(uri);

		try {

			AssetManager assets = getContext().getAssets();

			log.debug("Accessing asset " + assetPath);

			try {
				return assets.openFd(assetPath);
			} catch (FileNotFoundException e) {
				log.warn("Direct read failed: " + e.getMessage());
				File cacheFile = serveFileThroughCache(
						assets.open(assetPath, AssetManager.ACCESS_BUFFER),
						"assets/" + assetPath);
				return new AssetFileDescriptor(ParcelFileDescriptor.open(
						cacheFile, ParcelFileDescriptor.MODE_READ_ONLY), 0, -1);
			}
		} catch (FileNotFoundException ex) {
			throw ex;
		} catch (IOException ex) {
			throw new FileNotFoundException(ex.getMessage());
		}
	}

	protected String getPathFromUri(Uri uri) {
		String path = uri.getPath();
		if (path.startsWith("/"))
			path = path.substring(1);
		return path;
	}

}
