package com.cellasoft.univrapp.activity;

import test.FileCache;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;

public class MainActivity extends LocalizedActivity /*
													 * implements
													 * OnItemClickListener
													 */{

	private static final int SUCCESS = 1;

	// private DashboardEntry[] dashboardEntries = { new DashboardEntry(
	// R.string.launcher_notes_feed, R.drawable.ic_menu_feed,
	// ChannelListActivity.class) };
	// private GridView dashboardGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.grid_menu);
		//
		// dashboardGridView = (GridView) findViewById(R.id.dashboardGridView);
		// dashboardGridView.setOnItemClickListener(this);
		// dashboardGridView.setAdapter(new DashboardAdapter(MainActivity.this,
		// R.layout.dashboard_entry, dashboardEntries));
		init();
	}

	private void init() {
		// startServices();
		if (Settings.getFirstTime()) {
			onFirstTime();
		}
		startActivity(new Intent(this, ChannelListActivity.class));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AsyncTask<Void, Void, Void> clearCacheTask = new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				return null;
			}
		};
		clearCacheTask.execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SUCCESS) {
			if (resultCode == RESULT_OK) {
				Settings.saveFirstTime();
				cancelNotification();
			}
			if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
		finish();
	}

	private void onFirstTime() {
		FileCache.clearCacheFolder();
		Intent intent = new Intent(this, ChooseMainFeedActivity.class);
		startActivityForResult(intent, SUCCESS);
	}

	private void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(Constants.NOTIFICATION_ID);
	}

	// @Override
	// public void onItemClick(AdapterView<?> parent, View view, int position,
	// long id) {
	// startActivity(new Intent(this,
	// dashboardEntries[position].getActivity()));
	// }
}
