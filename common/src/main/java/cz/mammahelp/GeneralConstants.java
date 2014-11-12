package cz.mammahelp;



public class GeneralConstants {

	public static final String CATEGORY_HELP = "help";
	public static final String CATEGORY_INFORMATIONS = "informations";
	public static final String CONTENT_URI_PREFIX = "cz.mammahelp.handy.";
	public static final long SECOND_IN_MILLIS = 1000;
	public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
	public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
	public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
	public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
	public static final String SOURCE_ROOT_URL = "http://www.mammahelp.cz/";
	
	public static final String AUTHORITY = CONTENT_URI_PREFIX
			+ "%1$s";
	public static final String CONTENT_BASE_URI = "content://" + AUTHORITY;
	public static final String CONTENT_URI = CONTENT_BASE_URI + "/data/%2$d";

	public static final String ENCLOSURE_CONTENT = "enclosure";
	public static final String NEWS_CONTENT = "news";
	public static final String ARTICLE_CONTENT = "article";
	
}
