package com.cellasoft.univrapp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.util.Log;

public class Utils {

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
					return new Boolean(true);
				}
			} catch (Exception e) {
				appendToLogFile("isNetworkAvailable()", e.getMessage());
			}
		}

		if (Constants.DEBUG_MODE)
			Log.i(Constants.LOG_TAG, "OFFLINE!");
		return new Boolean(false);
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
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					appendToLogFile("inputStreamToString()", e.getMessage());
				}
			}
		}

		return sb.toString();
	}

	public static String removeSpecialString(String page_HTML, String regex) {
		// ELIMINO STRINGHE SPECIALI
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(page_HTML);
		while (matcher.find())
			page_HTML = matcher.replaceAll("");

		return page_HTML;
	}

	public static void appendToLogFile(String tag, String error) {
		File log = new File(Environment.getExternalStorageDirectory(),
				"UNIVR_RSS_LOG.txt");
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
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					if (Constants.DEBUG_MODE)
						Log.e(Constants.LOG_TAG, "Error log file not closed!");
				}
			}
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
}