package cz.mammahelp.handy;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cz.mammahelp.handy.dao.AddressDao;
import cz.mammahelp.handy.dao.ArticlesDao;
import cz.mammahelp.handy.dao.BaseDao.Column;
import cz.mammahelp.handy.dao.BaseDao.Table;
import cz.mammahelp.handy.dao.BundleDao;
import cz.mammahelp.handy.dao.EnclosureDao;
import cz.mammahelp.handy.dao.LocationPointDao;
import cz.mammahelp.handy.dao.NewsDao;
import cz.mammahelp.model.Articles;
import cz.mammahelp.model.ArticlesXmlWrapper;

public class MammaHelpDbHelper extends SQLiteOpenHelper {

	public static final int DATABASE_VERSION = 2;
	public static final String DATABASE_NAME = "MammaHelp.db";

	public static Logger log = LoggerFactory.getLogger(MammaHelpDbHelper.class);

	private static final String SQL_CREATE_ARTICLES = ArticlesDao.getTable()
			.createClausule();

	private static final String SQL_CREATE_NEWS = NewsDao.getTable()
			.createClausule();

	private static final String SQL_CREATE_ADDRESS = AddressDao.getTable()
			.createClausule();

	private static final String SQL_CREATE_BUNDLE = BundleDao.getTable()
			.createClausule();

	private static final String SQL_CREATE_ENCLOSURE = EnclosureDao.getTable()
			.createClausule();

	private static final String SQL_CREATE_LOCATION_POINT = LocationPointDao
			.getTable().createClausule();

	private final static DataSetObservable mDataSetObservable = new DataSetObservable();

	private static MammaHelpDbHelper singletonInstance;

	public static MammaHelpDbHelper getInstance(Context context) {
		if (singletonInstance == null)
			singletonInstance = new MammaHelpDbHelper(context);

		// singletonInstance.createFakeArticle("článek", "informations");
		// singletonInstance.createFakeArticle("článek", "help");

		return singletonInstance;
	}

	private Context context;

	// private void createFakeArticle(String string, String string2) {
	// createFakeArticle(getWritableDatabase(), string, string2);
	// }

	private MammaHelpDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		log.debug("Constructed DbHelper");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.suteren.android.jidelak.INotifyingDbHelper#onCreate(android.database
	 * .sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		log.debug(SQL_CREATE_ARTICLES);
		db.execSQL(SQL_CREATE_ARTICLES);

		log.debug(SQL_CREATE_NEWS);
		db.execSQL(SQL_CREATE_NEWS);

		log.debug(SQL_CREATE_ADDRESS);
		db.execSQL(SQL_CREATE_ADDRESS);

		log.debug(SQL_CREATE_ENCLOSURE);
		db.execSQL(SQL_CREATE_ENCLOSURE);

		log.debug(SQL_CREATE_BUNDLE);
		db.execSQL(SQL_CREATE_BUNDLE);

		log.debug(SQL_CREATE_LOCATION_POINT);
		db.execSQL(SQL_CREATE_LOCATION_POINT);

		loadData(db);

		log.debug("Finished onCreate");

	}

	private void loadData(SQLiteDatabase db) {
		Serializer serializer = new Persister();

		loadArticles(db, serializer);
		// loadLocations(db, serializer);
	}

	private void loadArticles(SQLiteDatabase db, Serializer serializer) {
		try {
			ArticlesXmlWrapper aw = serializer.read(ArticlesXmlWrapper.class,
					context.getResources().openRawResource(R.raw.articles));
			ArticlesDao aDao = new ArticlesDao(db);

			log.debug("Read " + aw.articles.size() + " articles.");

			for (Articles a : aw.articles) {
				log.debug("Saving article " + a);
				a.setCategory(a.getCategory().trim());
				a.setTitle(a.getTitle().trim());
				aDao.insert(a);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.suteren.android.jidelak.INotifyingDbHelper#onUpgrade(android.database
	 * .sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		log.debug("Old version: " + oldVersion + ", new version: " + newVersion);

		switch (oldVersion) {
		case 1:

			// New stuf here

			if (newVersion <= 2)
				break;

		default:
			break;
		}

	}

	//
	// private void createFakeArticle(SQLiteDatabase db, String code,
	// String category) {
	// ArticlesDao aDao = new ArticlesDao(db);
	//
	// Articles a = new Articles();
	// a.setBody("Pokusný " + code + " v sekci " + category + "- tělo");
	// a.setSyncTime(Calendar.getInstance());
	// a.setTitle("Pokusný " + code + " v sekci " + category + " - titulek");
	// a.setUrl("http://www.vysetrise.cz/");
	// a.setCategory(category);
	//
	// aDao.insert(a);
	//
	// a.setTitle(a.getTitle() + " " + a.getId());
	// a.setBody(a.getBody() + " " + a.getId());
	//
	// aDao.update(a);
	//
	// log.debug("found " + aDao.findAll().size() + " articles");
	//
	// }

	protected void addColumn(SQLiteDatabase db, Table table, Column column) {
		db.execSQL("alter table " + table.getName() + " add column "
				+ column.createClausule());
	}

	protected void createIndex(SQLiteDatabase db, Table table,
			Column... columns) {
		createIndex(db, table, join(columns, "_"), columns);
	}

	protected void createIndex(SQLiteDatabase db, Table table, String name,
			Column... columns) {
		db.execSQL("create index " + table.getName() + "_" + name + " on "
				+ table.getName() + "(" + join(columns, ",") + ")");
	}

	private String join(Column[] columns, String delimiter) {
		if (columns.length < 1)
			return "";
		StringBuffer sb = new StringBuffer(columns[0].getName());
		for (int i = 0; i < columns.length; i++) {
			sb.append(delimiter);
			sb.append(columns[i].getName());
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.suteren.android.jidelak.INotifyingDbHelper#onDowngrade(android.database
	 * .sqlite.SQLiteDatabase, int, int)
	 */
	@SuppressLint("Override")
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.suteren.android.jidelak.INotifyingDbHelper#notifyDataSetChanged()
	 */
	public void notifyDataSetChanged() {
		log.debug("notifyDataSetChanged");
		mDataSetObservable.notifyChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.suteren.android.jidelak.INotifyingDbHelper#registerObserver(android
	 * .database.DataSetObserver)
	 */
	public void registerObserver(DataSetObserver observer) {
		log.debug("Registering observer: " + observer);
		mDataSetObservable.registerObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.suteren.android.jidelak.INotifyingDbHelper#unregisterObserver(android
	 * .database.DataSetObserver)
	 */
	public void unregisterObserver(DataSetObserver observer) {
		log.debug("UnRegistering observer: " + observer);
		mDataSetObservable.unregisterObserver(observer);
	}

	public void notifyDataSetInvalidated() {
		log.debug("notifyDataSetInvalidated");
		mDataSetObservable.notifyInvalidated();
	}

	public void cleanup(SQLiteDatabase db) {
		db.execSQL("delete from availability where restaurant is null and id not in (select a2.id from availability a2, meal m where m.availability = a2.id)");
	}

	public void cleanup() {
		cleanup(getWritableDatabase());
	}

}
