package com.cellasoft.univrapp.activity;

import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.GCMUtils;
import com.cellasoft.univrapp.utils.UIUtils;
import static com.cellasoft.univrapp.utils.LogUtils.LOGD;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!UIUtils.hasHoneycomb()) {
			onCreatePreferenceActivity();
		} else {
			onCreatePreferenceFragment();
		}

		findPreferenceBykey("app_version").setSummary(
				Config.getAppVersion(getApplicationContext()));
		findPreferenceBykey("univrapp_regid").setSummary(
				Settings.getRegistrationId());

	}

	/**
	 * Wraps legacy {@link #onCreate(Bundle)} code for Android < 3 (i.e. API lvl
	 * < 11).
	 */
	@SuppressWarnings("deprecation")
	private void onCreatePreferenceActivity() {
		addPreferencesFromResource(R.xml.settings);
	}

	/**
	 * Wraps {@link #onCreate(Bundle)} code for Android >= 3 (i.e. API lvl >=
	 * 11).
	 */
	@SuppressLint("NewApi")
	private void onCreatePreferenceFragment() {
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new MyPreferenceFragment())
				.commit();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
		super.onPostCreate(savedInstanceState);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		showListPreferenceValues(getPreferenceScreen());
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (Settings.AUTO_UPDATE_KEY.equals(key)) {
			if (Settings.getAutoUpdate()) {
				startSynchronizationService();
			} else {
				SynchronizationService.cancelScheduledUpdates();
			}
		}

		if (Settings.NOTIFICATIONS_UNIVRAPP.equals(key)) {
			if (Settings.isEnabledNotificationUnivrApp()) {
				registerGCMClient();
			} else {
				unregisterGCMClient();
			}
		}

		Preference pref = findPreferenceBykey(key);
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
	}

	private void registerGCMClient() {
		if (ConnectivityReceiver
				.hasGoodEnoughNetworkConnection()) {
			GCMUtils.doRegister(this);

			try {
				Thread.sleep(1000);
				findPreferenceBykey("univrapp_regid").setSummary(
						Settings.getRegistrationId());
			} catch (InterruptedException e) {
			}
		}
	}

	private void unregisterGCMClient() {
		if (ConnectivityReceiver
				.hasGoodEnoughNetworkConnection()) {
			GCMUtils.doUnregister(this);
			findPreferenceBykey("univrapp_regid").setSummary("Not Registered");
		}
	}

	@SuppressWarnings("deprecation")
	private Preference findPreferenceBykey(String key) {
		return findPreference(key);
	}

	private void startSynchronizationService() {
		if (ConnectivityReceiver
				.hasGoodEnoughNetworkConnection()) {
			Intent service = new Intent(this, SynchronizationService.class);
			startService(service);
		}
	}

	private void showListPreferenceValues(PreferenceGroup group) {
		int count = group.getPreferenceCount();
		for (int i = 0; i < count; i++) {
			Preference preference = group.getPreference(i);
			if (preference instanceof PreferenceCategory
					|| preference instanceof PreferenceScreen) {
				showListPreferenceValues((PreferenceGroup) preference);
			} else if (preference instanceof ListPreference) {
				ListPreference listPreference = (ListPreference) preference;
				listPreference.setSummary(listPreference.getEntry());
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class MyPreferenceFragment extends PreferenceFragment {
		private static final String TAG = makeLogTag(MyPreferenceFragment.class);

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			if (BuildConfig.DEBUG) {
				LOGD(TAG, "onCreate()");
			}
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
		}
	}

}
