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
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.activity.AboutScreen;
import com.cellasoft.univrapp.activity.ChannelListActivity;
import com.cellasoft.univrapp.activity.ChooseMainFeedActivity;
import com.cellasoft.univrapp.activity.ContactActivity;
import com.cellasoft.univrapp.activity.DisPlayWebPageActivity;
import com.cellasoft.univrapp.activity.ItemListActivity;
import com.cellasoft.univrapp.activity.SettingsActivity;
import com.cellasoft.univrapp.activity.SubscribeActivity;

/**
 * An assortment of UI helpers.
 */
public class UIUtils {

	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

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

	public static ImageFetcher getImageFetcher(final FragmentActivity activity) {
		// The ImageFetcher takes care of loading remote images into our
		// ImageView
		ImageFetcher fetcher = new ImageFetcher(activity);
		fetcher.addImageCache(activity);
		return fetcher;
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

	private static final long sAppLoadTime = System.currentTimeMillis();

	public static long getCurrentTime(final Context context) {
		if (Config.DEBUG_MODE) {
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
			Toast.makeText(context, "Couldn't open link", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// TODO: use <meta-data> element instead
	private static final Class[] sPhoneActivities = new Class[] {
			ChooseMainFeedActivity.class, ChannelListActivity.class,
			SubscribeActivity.class, ContactActivity.class,
			ItemListActivity.class, DisPlayWebPageActivity.class,
			AboutScreen.class, SettingsActivity.class, };

	public static void enableDisableActivities(final Context context) {
		boolean isHoneycombTablet = isHoneycombTablet(context);
		PackageManager pm = context.getPackageManager();

		// Enable/disable phone activities
		for (Class a : sPhoneActivities) {
			pm.setComponentEnabledSetting(
					new ComponentName(context, a),
					isHoneycombTablet ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
							: PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
		}
	}

	public static int getScreenSize() {
		return Application.getInstance().getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
	}

	@TargetApi(11)
	public static void enableStrictMode() {
		if (UIUtils.hasGingerbread()) {
			StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
					.detectAll().penaltyLog().penaltyDialog();
			StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
					.detectAll().penaltyLog();

			if (UIUtils.hasHoneycomb()) {
				threadPolicyBuilder.penaltyFlashScreen();
				vmPolicyBuilder
						.setClassInstanceLimit(ChannelListActivity.class, 1)
						.setClassInstanceLimit(SubscribeActivity.class, 1)
						.setClassInstanceLimit(DisPlayWebPageActivity.class, 1);
			}
			StrictMode.setThreadPolicy(threadPolicyBuilder.build());
			StrictMode.setVmPolicy(vmPolicyBuilder.build());
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void setActivatedCompat(View view, boolean activated) {
		if (hasHoneycomb()) {
			view.setActivated(activated);
		}
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
}
