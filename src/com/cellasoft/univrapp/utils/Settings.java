package com.cellasoft.univrapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
	public static final String PREFS_NAME = "com.cellasoft_preferences";
	public static final String AUTO_UPDATE_KEY = "auto_update";
	public static final String UPDATE_INTERVAL_KEY = "update_interval";
	public static final String WIFI_ONLY_KEY = "wifi_only";
	public static final String ID_EDITORE = "a14fcb4caab6d83";
	private static Context context;

	static {
		context = ApplicationContext.getInstance();
	}

	public static int getUpdateInterval() {
		// SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
		// 0);
		// return Integer.parseInt(prefs.getString(
		// context.getString(R.string.update_interval_key), "5"));
		return 10;
	}

	public static boolean getAutoUpdate() {
		// SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
		// 0);
		// return prefs.getBoolean(context.getString(R.string.auto_update_key),
		// true);
		return true;
	}

	public static void setUniversity(String uni) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("UNIVERSITY", uni);
		editor.commit();
	}

	public static String getUniversity() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString("UNIVERSITY", "");
	}

	public static boolean getDownloadImages() {
		return true;
	}
}
