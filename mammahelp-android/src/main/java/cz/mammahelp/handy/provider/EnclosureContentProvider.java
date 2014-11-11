package cz.mammahelp.handy.provider;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.model.Enclosure;

public class EnclosureContentProvider extends
		AbstractMammahelpContentProvider<Enclosure> {

	private static final boolean CACHE = true;

	public static Logger log = LoggerFactory
			.getLogger(EnclosureContentProvider.class);
	private EnclosureDao edao;

	@Override
	public String getType(Uri uri) {
		log.debug("getType");
		return getObjectFromUri(uri).getType();
	}

	@Override
	protected InputStream getInputStreamFromUri(Uri uri) {
		Enclosure enclosure = getObjectFromUri(uri);
		return new ByteArrayInputStream(enclosure.getData());
	}

	@Override
	protected EnclosureDao getDao() {
		if (edao == null)
			edao = new EnclosureDao(getDbHelper());
		return edao;
	}

	@Override
	protected Long getDataLength(Uri uri) {
		return getObjectFromUri(uri).getLength();
	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode)
			throws FileNotFoundException {

		AssetFileDescriptor assetFile = super.openAssetFile(uri, mode);
		if (CACHE)
			try {
				assetFile = serveFileThroughCache(
						new FileInputStream(assetFile.getFileDescriptor()),
						"enclosures/" + uri.getLastPathSegment());
			} catch (IOException e) {
				log.error("FAiled to retrieve enclosure: " + e.getMessage(), e);
			}
		return assetFile;
	}

}