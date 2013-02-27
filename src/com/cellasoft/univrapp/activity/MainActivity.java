package com.cellasoft.univrapp.activity;

import java.util.Date;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.cellasoft.univrapp.adapter.DashboardAdapter;
import com.cellasoft.univrapp.service.DownloadingService;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.ConnectivityReceiver;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.DashboardEntry;
import com.cellasoft.univrapp.utils.ImageCache;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.utils.Settings;
import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity implements OnItemClickListener {

	private DashboardEntry[] dashboardEntries = { new DashboardEntry(
			R.string.launcher_notes_feed, R.drawable.ic_menu_feed,
			ChannelListActivity.class) };
	private GridView dashboardGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageLoader.initialize(this);
		setContentView(R.layout.grid_menu);

		dashboardGridView = (GridView) findViewById(R.id.dashboardGridView);
		dashboardGridView.setOnItemClickListener(this);
		dashboardGridView.setAdapter(new DashboardAdapter(MainActivity.this,
				R.layout.dashboard_entry, dashboardEntries));
		init();
	}

	private void init() {
		startServices();
		if (Settings.getFirstTime()) {
			Settings.saveFirstTime();

			onFirstTime();
		}

		cancelNotification();
		//cloud();
	}

	private void cloud() {
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);

		final String regId = GCMRegistrar.getRegistrationId(this);
		Log.i("CLOUD", "registration id =====  " + regId);

		if (regId.equals("")) {
			GCMRegistrar.register(this, Constants.SENDER_ID);
		} else {
			Log.v("CLOUD", "Already registered");
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AsyncTask<Void, Void, Void> clearCacheTask = new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				ImageCache.clearCacheIfNecessary();
				return null;
			}
		};
		clearCacheTask.execute();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		GCMRegistrar.unregister(this);
	}

	private void onFirstTime() {
		ImageCache.clearCacheFolder();
		Intent intent = new Intent(this, ChooseMainFeedActivity.class);
		startActivity(intent);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		startActivity(new Intent(this, dashboardEntries[position].getActivity()));
	}
}
