package com.cellasoft.univrapp.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.cellasoft.univrapp.BuildConfig;

/**
 * Internal helper class for date conversions.
 * 
 */
public final class DateUtils {
	private static final String TAG = DateUtils.class.getSimpleName();

	/**
	 * @see <a href="http://www.ietf.org/rfc/rfc0822.txt">RFC 822</a>
	 */
	public static final String RFC822 = "EEE, dd MMM yyyy HH:mm:ss Z";
	public static final String NEWS_DATE_FORMAT = "d MMM',' yyyy 'at' HH:mm a";

	private static final ThreadLocal<DateFormat> formatRFC822 = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat(RFC822, Locale.ENGLISH);
		}
	};

	private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat(NEWS_DATE_FORMAT, Locale.getDefault());
		}
	};

	/* Hide constructor */
	private DateUtils() {
	}

	/**
	 * Parses string as an RFC 822 date/time.
	 * 
	 * @throws Exception
	 * 
	 * @throws RSSFault
	 *             if the string is not a valid RFC 822 date/time
	 */
	public static Date parseRfc822(String date) {

		try {
			return formatRFC822.get().parse(date.trim());
		} catch (ParseException e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "No parser date " + date);
			return new Date();
		}

	}

	public static String formatTimeMillis(Context context, long timeMillis) {

		StringBuilder sb = new StringBuilder();
		if (timeMillis > 0) {
			try {
				sb.append(android.text.format.DateUtils
						.getRelativeDateTimeString(context, timeMillis,
								android.text.format.DateUtils.MINUTE_IN_MILLIS,
								android.text.format.DateUtils.WEEK_IN_MILLIS,
								android.text.format.DateUtils.FORMAT_ABBREV_ALL));
			} catch (Throwable e) {
				sb.append(dateFormat.get().format(new Date(timeMillis)));
			}
		}
		return sb.toString();

	}

	public static String formatDate(Date date) {

		StringBuilder sb = new StringBuilder();
		if (date != null) {
			try {
				sb.append(android.text.format.DateUtils.formatSameDayTime(
						date.getTime(),
						new Timestamp(System.currentTimeMillis()).getTime(),
						DateFormat.MEDIUM, DateFormat.SHORT));
			} catch (Throwable e) {
				sb.append(dateFormat.get().format(date));
			}
		}
		return sb.toString();

	}
}