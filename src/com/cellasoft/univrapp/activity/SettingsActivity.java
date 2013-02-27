package com.cellasoft.univrapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.cellasoft.univrapp.service.DownloadingService;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.ConnectivityReceiver;
import com.cellasoft.univrapp.utils.Settings;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = SettingsActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		showListPreferenceValues(getPreferenceScreen());
	}

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

		if (Settings.DOWNLOAD_IMAGES_KEY.equals(key)) {
			if (Settings.getDownloadImages()) {
				startDownloadingService();
			} else {
				DownloadingService.cancelScheduledDownloads();
			}
		}

		Preference pref = findPreference(key);
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
	}

	private void startSynchronizationService() {
		if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
			Intent service = new Intent(this, SynchronizationService.class);
			startService(service);
		}
	}

	private void startDownloadingService() {
		if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
			Intent downloadService = new Intent(this, DownloadingService.class);
			startService(downloadService);
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

}
