package com.cellasoft.univrapp.activity;

import java.util.Date;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.service.DownloadingService;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.FileCache;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.widget.DashboardEntry;

public class MainActivity extends LocalizedActivity /*implements OnItemClickListener */{

	private static final int SUCCESS = 1;
//	private DashboardEntry[] dashboardEntries = { new DashboardEntry(
//			R.string.launcher_notes_feed, R.drawable.ic_menu_feed,
//			ChannelListActivity.class) };
//	private GridView dashboardGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageLoader.initialize(this);
//		setContentView(R.layout.grid_menu);
//
//		dashboardGridView = (GridView) findViewById(R.id.dashboardGridView);
//		dashboardGridView.setOnItemClickListener(this);
//		dashboardGridView.setAdapter(new DashboardAdapter(MainActivity.this,
//				R.layout.dashboard_entry, dashboardEntries));
		init();
	}

	private void init() {
		startServices();
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
				FileCache.clearCacheIfNecessary();
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

	private void startServices() {
		if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
			if (Constants.DEBUG_MODE)
				Log.d(Constants.LOG_TAG, "Begin startServices " + new Date());
			Intent service = new Intent(this, SynchronizationService.class);
			startService(service);

			Intent downloadService = new Intent(this, DownloadingService.class);
			startService(downloadService);
			if (Constants.DEBUG_MODE)
				Log.d(Constants.LOG_TAG, "End startServices " + new Date());
		}
	}

//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position,
//			long id) {
//		startActivity(new Intent(this, dashboardEntries[position].getActivity()));
//	}
}
