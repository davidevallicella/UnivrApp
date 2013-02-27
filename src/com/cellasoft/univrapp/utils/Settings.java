package com.cellasoft.univrapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.cellasoft.univrapp.activity.R;

public class Settings {
	public static final String PREFS_NAME = "com.cellasoft.univrapp.activity_preferences";
	public static final String AUTO_UPDATE_KEY = "auto_update";
	public static final String UPDATE_INTERVAL_KEY = "update_interval";
	public static final String DOWNLOAD_IMAGES_KEY = "download_images";
	public static final String WIFI_ONLY_KEY = "wifi_only";
	public static final String KEEP_MAX_ITEMS_KEY = "keep_max_items";
	public static final String ID_EDITORE = "a14fcb4caab6d83";
	private static Context context;

	static {
		context = Application.getInstance();
	}

	public static boolean getFirstTime() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean(context.getString(R.string.first_time_key),
				true);
	}

	public static void saveFirstTime() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(context.getString(R.string.first_time_key), false);

		editor.putString(context.getString(R.string.update_interval_key), "15");
		editor.putString(context.getString(R.string.max_items_for_channel_key),
				"100");
		editor.putBoolean(context.getString(R.string.auto_update_key), true);
		// editor.putBoolean(context.getString(R.string.show_updated_channels_key),
		// false);
		editor.putString(context.getString(R.string.language_key), "it");

		editor.putString("font", "sans");
		editor.putString("font_size", "1.0em");
		editor.putBoolean("notification_sound", false);
		editor.putBoolean("notification_vibrate", false);
		editor.putBoolean("notification_light", true);

		editor.putString(KEEP_MAX_ITEMS_KEY, "2000");
		editor.putBoolean(DOWNLOAD_IMAGES_KEY, false);
		editor.putBoolean(WIFI_ONLY_KEY, false);

		editor.commit();
	}

	public static int getUpdateInterval() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return Integer.parseInt(prefs.getString(
				context.getString(R.string.update_interval_key), "5"));
	}

	public static boolean getAutoUpdate() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean(context.getString(R.string.auto_update_key),
				true);
	}

	public static void setUniversity(String uni) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("UNIVERSITY", uni);
		editor.commit();
	}

	public static String getLocale() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString(context.getString(R.string.language_key), "en");
	}

	public static String getUniversity() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString("UNIVERSITY",
				Constants.UNIVERSITY.SCIENZE_MM_FF_NN);
	}

	public static int getKeepMaxItems() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return Integer.parseInt(prefs.getString(KEEP_MAX_ITEMS_KEY, "2000"));
	}

	public static boolean getDownloadImages() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean(DOWNLOAD_IMAGES_KEY, false);
	}

	public static int getKeepMaxImages() {
		return 2000;
	}

	public static boolean getWifiOnly() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean(WIFI_ONLY_KEY, false);
	}

	public static int getMaxItemsForChannel() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return Integer.parseInt(prefs.getString(
				context.getString(R.string.max_items_for_channel_key), "20"));
	}

	public static String getFont() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString("font", "sans");
	}

	public static String getFontSize() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString("font_size", "1.0em");
	}
}
