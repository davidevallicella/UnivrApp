package com.cellasoft.univrapp.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.Constants;

/**
 * Internal helper class for date conversions.
 * 
 */
public final class DateUtils {
	private static final String TAG = DateUtils.class.getSimpleName();
	/**
	 * @see <a href="http://www.ietf.org/rfc/rfc0822.txt">RFC 822</a>
	 */
	private static final SimpleDateFormat RFC822 = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"d MMM',' yyyy 'at' HH:mm a");

	private static Context context;
	static {
		context = Application.getInstance();
	}

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
			return RFC822.parse(date.trim());
		} catch (ParseException e) {
			if (Constants.DEBUG_MODE)
				Log.e(TAG, "No parser date " + date);
			return new Date();
		}
	}

	public static String formatTimeMillis(long timeMillis) {
		StringBuilder sb = new StringBuilder();
		if (timeMillis > 0) {
			try {
				sb.append(android.text.format.DateUtils
						.getRelativeDateTimeString(context, timeMillis,
								android.text.format.DateUtils.MINUTE_IN_MILLIS,
								android.text.format.DateUtils.WEEK_IN_MILLIS,
								android.text.format.DateUtils.FORMAT_ABBREV_ALL));
			} catch (Throwable e) {
				sb.append(dateFormat.format(new Date(timeMillis)));
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
				sb.append(dateFormat.format(date));
			}
		}
		return sb.toString();
	}
}