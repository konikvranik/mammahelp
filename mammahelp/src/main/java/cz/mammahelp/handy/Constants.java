package cz.mammahelp.handy;


public class Constants {

	public static final String DEFAULT_PREFERENCES = "default";

	public static final String KEY = "key";

	public static final String LAST_UPDATED_KEY = "last_updated";

	public static final String DELETE_DELAY_KEY = "delete_delay";

	public static final String CATEGORY_BACKGROUND_KEY = "category_background";
	public static final String UPDATE_INTERVAL_KEY = "update_interval";
	public static final String UPDATE_TIME_KEY = "update_time";
	public static final String AUTOMATIC_UPDATES_KEY = "automatic_updates";

	public static final long SECOND_IN_MILLIS = 1000;
	public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
	public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
	public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
	public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;

	public static final long DEFAULT_UPDATE_INTERVAL = 4 * HOUR_IN_MILLIS;

	public static final long DEFAULT_DELETE_DELAY = WEEK_IN_MILLIS;

	public static final String EXCEPTION = "exception";

	public static String WIFI_ONLY_KEY = "only_wifi";
	public static boolean DEFAULT_WIFI_ONLY = true;

	public static final String NEWS_KEY = "news";

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	public static final String ARTICLE_KEY = "article";

	public static final String NEWS_FRAGMENT_TAG = "news";

	public static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

	public static final String CATEGORY_KEY = "category";

	public static final String CATEGORY_INFORMATIONS = "informations";

	public static final String CATEGORY_HELP = "help";

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	public static final String CENTER_KEY = "center";

	public static final String REGISTER_FLAG = "register";

	public static final int NEWS_NOTIFICATION_ID = 1;

	public static final String NEWS_LAST_UPDATED = "news_last_updated";

}
