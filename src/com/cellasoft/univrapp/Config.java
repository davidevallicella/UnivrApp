package com.cellasoft.univrapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;

public final class Config {
	public static final String APP_NAME = "UnivrApp";
	public static final int MAX_ITEMS = 15;
	public static final int MAX_ITEMS_PER_FETCH = 20;
	public static final int NOTIFICATION_ID = 1;

	// App. Version
	public static String getAppVersion(Context context) {
		try {
			String pkg = context.getPackageName();
			return "Version: "
					+ context.getPackageManager().getPackageInfo(pkg, 0).versionName;
		} catch (NameNotFoundException e) {
			return "Version: ?";
		}
	}

	public static final String SERVER_URL = "http://univrapp.altervista.org";
	public static final String SUPPORT_EMAIL = "vallicella.davide@gmail.com";

	// ADMod
	public static final String ADB_EDITOR_ID = "a14fcb4caab6d83";

	// PAYPAL config
	public static final String PAYPAL_APP_ID = "APP-02V829382W416122M";
	public static final String PAYPAL_EMAIL = SUPPORT_EMAIL;

	// GCM config
	public static final String GCM_SERVER_URL = SERVER_URL;
	public static final String GCM_SENDER_ID = "843017041228";
	public static final int GCM_NOTIFICATION_ID = 2;

	static final String DISPLAY_MESSAGE_ACTION = "com.cellasoft.univrapp.DISPLAY_MESSAGE";

	static final String EXTRA_MESSAGE = "message";

	/**
	 * Notifies UI to display a message.
	 * <p>
	 * This method is defined in the common helper because it's used both by the
	 * UI and the background service.
	 * 
	 * @param context
	 *            application's context.
	 * @param message
	 *            message to be displayed.
	 */
	public static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
}