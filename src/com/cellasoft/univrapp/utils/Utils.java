package com.cellasoft.univrapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.StrictMode;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.activity.ChannelListActivity;
import com.cellasoft.univrapp.activity.ChooseMainFeedActivity;
import com.cellasoft.univrapp.activity.ContactActivity;
import com.cellasoft.univrapp.activity.DisPlayWebPageActivity;
import com.cellasoft.univrapp.activity.ItemListActivity;
import com.cellasoft.univrapp.activity.SubscribeActivity;

public class Utils {

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

	public static int getScreenSize() {
		return Application.getInstance().getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
	}

	@TargetApi(11)
	public static void enableStrictMode() {
		if (Utils.hasGingerbread()) {
			StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
					.detectAll().penaltyLog()
			        .penaltyDialog();
			StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
					.detectAll().penaltyLog();

			if (Utils.hasHoneycomb()) {
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

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}
}