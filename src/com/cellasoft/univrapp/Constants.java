package com.cellasoft.univrapp;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class Constants {
	public static final int MAX_ITEMS = 15;
	public static final int MAX_ITEMS_PER_FETCH = 20;
	public static final int NOTIFICATION_ID = 2;
	public static final boolean DEBUG_MODE = false;
	public static final String LOG_TAG = "UnivrApp";
	
	private static Context context;

	static {
		context = Application.getInstance();
	}
	
	public static String getAppVersion() {
		try {
            String pkg = context.getPackageName();
            return "Version: " + context.getPackageManager().getPackageInfo(pkg, 0).versionName;
        } catch (NameNotFoundException e) {
            return "Version: ?";
        }
	}
}
