package com.cellasoft.univrapp.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cellasoft.univrapp.activity.ItemListActivity;
import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.cellasoft.univrapp.utils.ApplicationContext;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.utils.Settings;

public class SynchronizationService extends Service {
	public static final String ACTION = "com.cellasoft.activity.SynchronizationService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "Start Synchronization Service..");
		super.onStart(intent, startId);
		ImageLoader.initialize(this);
		final int id = intent.getIntExtra("channel", 0);
		new Thread(new Runnable() {
			public void run() {
				startSynchronization(id);
			}
		}).start();
	}

	@Override
	public void onDestroy() {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "Destroy Synchronization Service!");
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "LowMemory Synchronization Service!");
		super.onLowMemory();
		SynchronizationManager.getInstance().stopSynchronizing();
	}

	private void startSynchronization(int id) {
		int totalNewItems = SynchronizationManager.getInstance()
				.startSynchronizing(id);

		if (totalNewItems > 0) {
			notifyNewItems(totalNewItems);
		}

		sendMessage(Constants.UPDATE_UI);
		if (Settings.getAutoUpdate()) {
			scheduleNextUpdate();
		}
		stopSelf();
	}

	public void notifyNewItems(int newItem) {
		NotificationManager notificationManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// This is who should be launched if the user selects our notification.
		Intent contentIntent = new Intent(getApplicationContext(),
				ItemListActivity.class);
		contentIntent.setAction(Intent.ACTION_MAIN);
		contentIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		PendingIntent appIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, contentIntent, 0);

		Notification notification = new Notification(R.drawable.univr,
				"Università di Verona", System.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(),
				"Università di Verona", "Ci sono nuovi avvisi.", appIntent);
		notification.number = newItem;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(Constants.NOTIFICATION_ID, notification);
	}

	private void sendMessage(int action) {
		sendMessage(action, null);
	}

	private void sendMessage(int action, String msg) {
		Intent intent = new Intent(ACTION);
		intent.putExtra("action", action);
		if (msg != null)
			intent.putExtra("message", msg);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void scheduleNextUpdate() {
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, SynchronizationService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				0);
		int updateInterval = Settings.getUpdateInterval() * 1000 * 60;
		long firstWake = System.currentTimeMillis() + updateInterval;
		am.set(AlarmManager.RTC, firstWake, pendingIntent);
	}

	public static void cancelScheduledUpdates() {
		Context context = ApplicationContext.getInstance();
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, SynchronizationService.class);
		PendingIntent pendingIntent = PendingIntent.getService(
				ApplicationContext.getInstance(), 0, intent, 0);
		am.cancel(pendingIntent);
	}
}
