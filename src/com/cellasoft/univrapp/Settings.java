package com.cellasoft.univrapp;


import android.content.Context;
import android.content.SharedPreferences;

import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.model.University;
import com.cellasoft.univrapp.model.University.Universites;

public class Settings {
	public static final String PREFS_NAME = "com.cellasoft.univrapp.activity_preferences";
	public static final String AUTO_UPDATE_KEY = "auto_update";
	public static final String UPDATE_INTERVAL_KEY = "update_interval";
	public static final String DOWNLOAD_IMAGES_KEY = "download_images";
	public static final String WIFI_ONLY_KEY = "wifi_only";
	public static final String KEEP_MAX_ITEMS_KEY = "keep_max_items";
	public static final String MAX_ITEMS_FOR_CHANNEL_KEY = "max_items_for_channel";
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
	
	public static void setFirstTime(boolean value) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(context.getString(R.string.first_time_key), value);
		editor.commit();
	}

	public static void saveFirstTime() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(context.getString(R.string.first_time_key), false);
		editor.putString(context.getString(R.string.update_interval_key), "15");
		editor.putBoolean(context.getString(R.string.auto_update_key), true);
		// editor.putBoolean(context.getString(R.string.show_updated_channels_key),
		// false);
		editor.putString(context.getString(R.string.language_key), "it");

		editor.putString("font", "sans");
		editor.putString("font_size", "1.0em");
		editor.putBoolean("notification_sound", false);
		editor.putBoolean("notification_vibrate", false);
		editor.putBoolean("notification_light", true);

		editor.putString(MAX_ITEMS_FOR_CHANNEL_KEY, "100");
		editor.putString(KEEP_MAX_ITEMS_KEY, "20");
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

	public static void setUniversity(String university) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("UNIVERSITY", Universites.DEST.get(university));
		editor.commit();
	}

	public static boolean getShowUpdatedChannels() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean(
				context.getString(R.string.show_updated_channels_key), false);
	}

	public static String getLocale() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString(context.getString(R.string.language_key), "en");
	}

	public static University getUniversity() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		int dest = prefs
				.getInt("UNIVERSITY", Universites.DEST_SCIENZE_MM_FF_NN);
		return University.getUniversityByDest(dest);
	}

	public static int getKeepMaxItems() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return Integer.parseInt(prefs.getString(KEEP_MAX_ITEMS_KEY, "20"));
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
		return Integer.parseInt(prefs.getString(MAX_ITEMS_FOR_CHANNEL_KEY,
				"100"));
	}

	public static String getFont() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString("font", "sans");
	}

	public static String getFontSize() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString("font_size", "1.0em");
	}

	public static boolean getNightReadingMode() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean("night_mode", false);
	}

	public static void saveNightReadingMode(boolean nightMode) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("night_mode", nightMode);
		editor.commit();
	}

	public static boolean getNotificationSound() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean("notification_sound", false);
	}

	public static boolean getNotificationVibrate() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean("notification_vibrate", false);
	}

	public static boolean getNotificationLight() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean("notification_light", false);
	}
}
