package cz.mammahelp.handy.provider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;
import cz.mammahelp.handy.Constants;
import cz.mammahelp.handy.dao.BaseDao;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.model.Enclosure;

public class EnclosureContentProvider extends
		AbstractMammahelpContentProvider<Enclosure> {

	public static final String AUTHORITY = Constants.CONTENT_URI_PREFIX
			+ "enclosure";
	public static final String CONTENT_BASE_URI = "content://" + AUTHORITY;
	public static final String CONTENT_URI = CONTENT_BASE_URI + "/data/";

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
	protected BaseDao<Enclosure> getDao() {
		if (edao == null)
			edao = new EnclosureDao(getDbHelper());
		return edao;
	}

	public static String makeUri(Long id) {
		return CONTENT_URI + id;
	}

}