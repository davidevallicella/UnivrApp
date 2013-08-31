package com.cellasoft.univrapp;

import static com.cellasoft.univrapp.utils.LogUtils.LOGD;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.util.Stack;

import org.acra.annotation.ReportsCrashes;

import com.cellasoft.univrapp.utils.ImageFetcher;
import com.github.droidfu.DroidFuApplication;

@ReportsCrashes(formKey = "dFFyRWpQWXV4blpmazN3MFo4VllKTUE6MQ")
public class Application extends DroidFuApplication {

	private static final String TAG = makeLogTag("UnivrApp");
	private static Application instance;
	public static Stack<Class<?>> parents = new Stack<Class<?>>();

	public Application() {
		instance = this;
	}

	public static Application getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onCreate()");
		}
		super.onCreate();

		// ACRA.init(this);
	}

	@Override
	public void onLowMemory() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onLowMemory()");
		}
		super.onLowMemory();
		ImageFetcher.getInstance(getApplicationContext()).clearCache();
		System.gc();
	}
}