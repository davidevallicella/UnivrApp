package com.cellasoft.univrapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;

public final class Config {
    public static final String APP_NAME = "UnivrApp";
    public static final int MAX_ITEMS = 15;
    public static final int MAX_ITEMS_PER_FETCH = 20;
    public static final int NOTIFICATION_ID = 1;

    public static final String SUPPORT_EMAIL = "vallicella.davide@gmail.com";
    public static final String PAYPAL_EMAIL = SUPPORT_EMAIL;
    // ADMod
    public static final String ADB_EDITOR_ID = "a14fcb4caab6d83";
    // PAYPAL config
    public static final String PAYPAL_APP_ID = "APP-02V829382W416122M";
    public static final String GCM_SENDER_ID = "843017041228";
    public static final int GCM_NOTIFICATION_ID = 2;
    static final String DISPLAY_MESSAGE_ACTION = "com.cellasoft.univrapp.DISPLAY_MESSAGE";
    static final String EXTRA_MESSAGE = "message";
    // ACRA config
    public static final String ACRA_USER = "blestereentopporepiestua";
    public static final String ACRA_PASS = "xdVgky3F73DQ0RsupIH02KvQ";
    public static final String ACRA_KEY = "dFFyRWpQWXV4blpmazN3MFo4VllKTUE6MQ";

    public static final class Links {
        public static final String SERVER = "http://univrapp.altervista.org";
        public static final String GCM = SERVER;
        public static final String ACRA = "https://dvallicella.cloudant.com/acra-univrapp/_design/acra-storage/_update/report";
        public static final String DONATE = "http://goo.gl/eoDGbu";
        public static final String GITHUB = "https://github.com/davidevallicella/UnivrApp";
    }

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

    /**
     * Notifies UI to display a message.
     * <p/>
     * This method is defined in the common helper because it's used both by the
     * UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}