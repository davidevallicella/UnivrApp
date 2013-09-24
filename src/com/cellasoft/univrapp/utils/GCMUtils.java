package com.cellasoft.univrapp.utils;

import static com.cellasoft.univrapp.Config.GCM_SENDER_ID;
import static com.cellasoft.univrapp.utils.LogUtils.LOGE;
import static com.cellasoft.univrapp.utils.LogUtils.LOGI;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;
import android.app.Activity;
import android.text.TextUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.gcm.ServerUtilities;
import com.google.android.gcm.GCMRegistrar;

public class GCMUtils {
	private static final String TAG = makeLogTag(GCMUtils.class);
	private static AsyncTask<Void, Void, Void> gcmRegisterTask, gcmUnregisterTask;

	public static void doRegister(final Activity activity) {
		GCMRegistrar.checkDevice(activity);

		if (BuildConfig.DEBUG) {
			GCMRegistrar.checkManifest(activity);
		}

		final String regId = GCMRegistrar.getRegistrationId(activity);

		if (TextUtils.isEmpty(regId)) {
			// Automatically registers application on startup.
			GCMRegistrar.register(activity, GCM_SENDER_ID);

		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(activity)) {
				// Skips registration
				LOGI(TAG, "Already registered on the GCM server");

			} else {
				// Try to register again, but not on the UI thread.
				// It's also necessary to cancel the task in onDestroy().
				gcmRegisterTask = new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						boolean registered = ServerUtilities.register(
								activity.getApplicationContext(),
								String.valueOf(Settings.getUniversity().dest),
								regId);
						if (!registered) {
							GCMRegistrar.unregister(activity
									.getApplicationContext());
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						if (activity instanceof SherlockPreferenceActivity) {
							SherlockPreferenceActivity prefAct = (SherlockPreferenceActivity) activity;
							prefAct.findPreference("univrapp_regid")
									.setSummary(Settings.getRegistrationId());
						}
						gcmRegisterTask = null;
					}
				};
				gcmRegisterTask.execute(null, null, null);
			}
		}
	}

	public static void doUnregister(final Activity activity) {
		gcmUnregisterTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				ServerUtilities.unregister(activity,
						Settings.getRegistrationId());
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (activity instanceof SherlockPreferenceActivity) {
					SherlockPreferenceActivity prefAct = (SherlockPreferenceActivity) activity;
					prefAct.findPreference("univrapp_regid").setSummary(
							"Not Registered");
				}
				gcmUnregisterTask = null;
			}
		};

		gcmUnregisterTask.execute(null, null, null);
	}

	public static void onDistroyGCMClient(Activity activity) {
		if (gcmRegisterTask != null) {
			gcmRegisterTask.cancel(true);
		}
		if (gcmUnregisterTask != null) {
			gcmUnregisterTask.cancel(true);
		}
		try {
			GCMRegistrar.onDestroy(activity.getApplicationContext());
		} catch (Exception e) {
			LOGE("UnRegister Receiver Error", "> " + e.getMessage());
		}
	}

	public static boolean isRegistered(Activity activity) {
		return GCMRegistrar.isRegistered(activity);
	}
}
