package com.cellasoft.univrapp;

import static com.cellasoft.univrapp.Config.GCM_SENDER_ID;
import static com.cellasoft.univrapp.utils.LogUtils.LOGE;
import static com.cellasoft.univrapp.utils.LogUtils.LOGI;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.util.Stack;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.text.TextUtils;

import com.cellasoft.univrapp.gcm.ServerUtilities;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.github.droidfu.DroidFuApplication;
import com.google.android.gcm.GCMRegistrar;

@ReportsCrashes(formKey = "dFFyRWpQWXV4blpmazN3MFo4VllKTUE6MQ")
public class Application extends DroidFuApplication {
	private static final String TAG = makeLogTag(Application.class);

	private static Application instance;
	public static Stack<Class<?>> parents = new Stack<Class<?>>();
	private AsyncTask<Void, Void, Void> gcmRegisterTask;
	private boolean isReceiverRegistered = false;

	public Application() {
		instance = this;
	}

	public static Application getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}

}