package com.cellasoft.univrapp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.util.Log;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.Constants;

public class Utils {

	private static Context context;
	static {
		context = Application.getInstance();
	}

	/**
	 * @return boolean return true if the application can access the internet
	 */
	public static boolean isNetworkAvailable(Context context) {
		Log.i(Constants.LOG_TAG, "Verifica stato della connessione...");
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity.getActiveNetworkInfo() != null
				&& connectivity.getActiveNetworkInfo().isAvailable()
				&& connectivity.getActiveNetworkInfo().isConnected()) {

			try {

				HttpURLConnection httpConn = (HttpURLConnection) new URL(
						"http://m.google.com").openConnection();
				httpConn.setRequestProperty("User-Agent", "Android Application");
				httpConn.setRequestProperty("Connection", "close");
				httpConn.setConnectTimeout(1000 * 5);
				httpConn.connect();
				if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					if (Constants.DEBUG_MODE)
						Log.i(Constants.LOG_TAG, "ONLINE!");
					return true;
				}
			} catch (Exception e) {
				appendToLogFile("isNetworkAvailable()", e.getMessage());
			}
		}

		if (Constants.DEBUG_MODE)
			Log.i(Constants.LOG_TAG, "OFFLINE!");
		return false;
	}

	// Riceve l'input stream della pagina e lo converte in stringa
	public static String inputStreamToString(InputStream is) {
		String line = "";
		BufferedReader in = null;

		StringBuilder sb = new StringBuilder(line);
		String NL = System.getProperty("line.separator");
		try {
			in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
			while ((line = in.readLine()) != null)
				sb.append(line).append(NL);
		} catch (IOException e) {
			appendToLogFile("inputStreamToString()", e.getMessage());
		} finally {
			StreamUtils.closeQuietly(in);
		}

		return sb.toString();
	}

	public static void appendToLogFile(String tag, String error) {
		File log = Utils.getBestCacheDir(context, "univrapp-log.txt");
		FileOutputStream fos = null;

		String s = "<ERROR date=\""
				+ DateFormat.getDateTimeInstance().format(new Date())
				+ "\" name=\"" + tag + "\">\n" + error + "\n</ERROR>\n";

		try {
			fos = new FileOutputStream(log, true);
			fos.write(s.getBytes());
		} catch (Exception ex) {
			if (Constants.DEBUG_MODE)
				Log.e(Constants.LOG_TAG, "Error log file: " + ex.getMessage());
		} finally {
			StreamUtils.closeQuietly(fos);
		}
	}

	//
	// Convert the Set to a List can be done by passing the Set instance
	// into
	// the constructor of a List implementation class such as ArrayList.
	//
	public static List<String> SetToList(Set<String> set) {
		return new ArrayList<String>(set);
	}

	public static Date timeInMillisecondsToDate(long time) {
		GregorianCalendar calendar = new GregorianCalendar(
				TimeZone.getTimeZone("GMT+1"));
		calendar.setTimeInMillis(time);
		return calendar.getTime();
	}

	public static void copyStream(final InputStream is, final OutputStream os)
			throws IOException {
		final int buffer_size = 8192;
		final byte[] bytes = new byte[buffer_size];
		int count = is.read(bytes, 0, buffer_size);
		while (count != -1) {
			os.write(bytes, 0, count);
			count = is.read(bytes, 0, buffer_size);
		}
	}

	public static File getBestCacheDir(final Context context,
			final String cache_dir_name) {
		final File ext_cache_dir = EnvironmentAccessor
				.getExternalCacheDir(context);
		if (ext_cache_dir != null && ext_cache_dir.isDirectory()) {
			final File cache_dir = new File(ext_cache_dir + cache_dir_name);
			if (!cache_dir.exists())
				cache_dir.mkdirs();
			return cache_dir;
		} else {
			final File int_cache_dir = new File(context.getCacheDir()
					+ cache_dir_name);
			if (!int_cache_dir.exists())
				int_cache_dir.mkdirs();

			return int_cache_dir;
		}
	}

	public static int getScreenSize() {
		return context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
	}

}