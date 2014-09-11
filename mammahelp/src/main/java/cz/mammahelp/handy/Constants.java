package cz.mammahelp.handy;

public class Constants {

	public static final String DEFAULT_PREFERENCES = "default";

	public static final String LAST_UPDATED_KEY = "last_updated";

	public static final String DELETE_DELAY_KEY = "delete_delay";

	public static final String CATEGORY_BACKGROUND_KEY = "category_background";
	public static final String UPDATE_INTERVAL_KEY = "update_interval";
	public static final String UPDATE_TIME_KEY = "update_time";
	public static final String PARTICULAR_TIME_KEY = "particular_time";
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
	
	

}
