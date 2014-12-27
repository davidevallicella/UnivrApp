/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cellasoft.univrapp.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.activity.*;
import com.cellasoft.univrapp.widget.LecturerView;
import com.github.droidfu.concurrent.BetterAsyncTask;

/**
 * An assortment of UI helpers.
 */
public class UIUtils {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long sAppLoadTime = System.currentTimeMillis();
    private static final Class<?>[] sPhoneActivities = new Class[]{
            DepartmentsActivity.class, ChannelListActivity.class,
            ContactActivity.class, ItemListActivity.class,
            DisPlayWebPageActivity.class, ContactListActivity.class,
            AboutScreen.class, SettingsActivity.class,};

    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = getCurrentTime(ctx);
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    // public static void getImageFetcher(final Context context) {
    // // Fetch screen height and width, to use as our max size when loading
    // // images as this
    // // activity runs full screen
    // final DisplayMetrics displayMetrics = new DisplayMetrics();
    // ((Activity) context).getWindowManager().getDefaultDisplay()
    // .getMetrics(displayMetrics);
    // final int height = displayMetrics.heightPixels;
    // final int width = displayMetrics.widthPixels;
    //
    // // For this sample we'll use half of the longest width to resize our
    // // images. As the
    // // image scaling ensures the image is larger than this, we should be
    // // left with a
    // // resolution that is appropriate for both portrait and landscape. For
    // // best image quality
    // // we shouldn't divide by 2, but this will use more memory and require a
    // // larger memory
    // // cache.
    // final int longest = (height > width ? height : width) / 2;
    //
    // // The ImageFetcher takes care of loading remote images into our
    // // ImageView
    // ImageFetcher fetcher = ImageFetcher.getInstance();
    // fetcher.init(context);
    // fetcher.setImageFadeIn(true);
    // fetcher.setImageSize(longest, longest);
    // fetcher.setLoadingImage(R.drawable.user);
    // ImageCache.ImageCacheParams cacheParams = new
    // ImageCache.ImageCacheParams(
    // context, IMAGE_CACHE_DIR);
    // // Set memory cache to 25% of app memory
    // cacheParams.setMemCacheSizePercent(0.25f);
    // fetcher.addImageCache(cacheParams);
    // return fetcher;
    // }

    /**
     * Populate the given {@link TextView} with the requested text, formatting
     * through {@link Html#fromHtml(String)} when applicable. Also sets
     * {@link TextView#setMovementMethod} so inline links are handled.
     */
    public static void setTextMaybeHtml(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setText("");
            return;
        }
        if (text.contains("<") && text.contains(">")) {
            view.setText(Html.fromHtml(text));
            view.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            view.setText(text);
        }
    }

    public static void preferPackageForIntent(Context context, Intent intent,
                                              String packageName) {
        PackageManager pm = context.getPackageManager();
        for (ResolveInfo resolveInfo : pm.queryIntentActivities(intent, 0)) {
            if (resolveInfo.activityInfo.packageName.equals(packageName)) {
                intent.setPackage(packageName);
                break;
            }
        }
    }

    // Shows whether a notification was fired for a particular session time
    // block. In the
    // event that notification has not been fired yet, return false and set the
    // bit.
    public static boolean isNotificationFiredForBlock(Context context,
                                                      String blockId) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        final String key = String.format("notification_fired_%s", blockId);
        boolean fired = sp.getBoolean(key, false);
        sp.edit().putBoolean(key, true).commit();
        return fired;
    }

    public static long getCurrentTime(final Context context) {
        if (BuildConfig.DEBUG) {
            return context.getSharedPreferences("mock_data",
                    Context.MODE_PRIVATE).getLong("mock_current_time",
                    System.currentTimeMillis())
                    + System.currentTimeMillis() - sAppLoadTime;
        } else {
            return System.currentTimeMillis();
        }
    }

    public static void safeOpenLink(Context context, Intent linkIntent) {
        try {
            context.startActivity(linkIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Couldn't open link", Toast.LENGTH_SHORT).show();
        }
    }

    public static void enableDisableActivities(final Context context) {

        boolean isHoneycombTablet = isHoneycombTablet(context);
        PackageManager pm = context.getPackageManager();

        // Enable/disable phone activities
        for (Class<?> a : sPhoneActivities) {
            pm.setComponentEnabledSetting(
                    new ComponentName(context, a),
                    isHoneycombTablet ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    public static int getScreenSize(Context context) {
        return context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;
    }

    @TargetApi(11)
    public static void enableStrictMode() {
        if (BuildConfig.DEBUG)
            return;
        if (UIUtils.hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectAll().penaltyLog().penaltyDialog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                    .detectAll().penaltyLog();

            if (UIUtils.hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
                vmPolicyBuilder
                        .setClassInstanceLimit(ChannelListActivity.class, 1)
                        .setClassInstanceLimit(ContactListActivity.class, 1)
                        .setClassInstanceLimit(ContactActivity.class, 1)
                        .setClassInstanceLimit(ItemListActivity.class, 1)
                        .setClassInstanceLimit(DepartmentsActivity.class, 1)
                        .setClassInstanceLimit(AboutScreen.class, 1)
                        .setClassInstanceLimit(DisPlayWebPageActivity.class, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static <P, T extends BetterAsyncTask<P, ?, ?>> void execute(T task,
                                                                       P... params) {
        if (hasHoneycomb()) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            task.execute(params);
        }
    }

    public static int getTouchAddition(Context context) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * LecturerView.TOUCH_ADDITION + 0.5f);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setHardwareAccelerated(Activity activity, View view,
                                              boolean activated) {
        if (hasHoneycomb()) {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setActivatedCompat(View view, boolean activated) {
        if (hasHoneycomb()) {
            view.setActivated(activated);
        }
    }

    public static void keepScreenOn(Activity activity, boolean activated) {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed
        // behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return hasHoneycomb() && isTablet(context);
    }

    public static void unbindWebView(WebView webView){
        ((ViewGroup)webView.getParent()).removeView(webView);
        webView.setFocusable(true);
        webView.removeAllViews();
        webView.clearHistory();
        webView.destroy();
    }
}
