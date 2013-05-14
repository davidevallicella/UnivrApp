package com.cellasoft.univrapp.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.activity.ChannelListActivity;
import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.github.droidfu.services.BetterService;

public class SynchronizationService extends BetterService {
	private final static String DEFAULT_THREAD_NAME = "Asynchronous service RSS feed loader";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "Start Synchronization Service..");
		super.onStart(intent, startId);
		new Thread(new Runnable() {
			public void run() {
				startSynchronization();
			}
		}, DEFAULT_THREAD_NAME).start();
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

	private void startSynchronization() {
		if (!Settings.getAutoUpdate()) {
			return;
		}

		int totalNewItems = SynchronizationManager.getInstance()
				.startSynchronizing();

		if (totalNewItems > 0) {
			notifyNewItems(totalNewItems);
		}

		if (Settings.getAutoUpdate()) {
			scheduleNextUpdate();
		}
		stopSelf();
	}

	private void notifyNewItems(int totalNewItems) {
		Context context = Application.getInstance();
		Intent notifyIntent = new Intent(context, ChannelListActivity.class);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

		String ticker = getString(R.string.new_items_notification).replace(
				"{total}", String.valueOf(totalNewItems));
		Notification notification = new Notification(R.drawable.univr, ticker,
				System.currentTimeMillis());
		notification.setLatestEventInfo(Application.getInstance(),
				getString(R.string.app_name), ticker, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		if (Settings.getNotificationSound()) {
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		if (Settings.getNotificationVibrate()) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		if (Settings.getNotificationLight()) {
			notification.ledARGB = 0xff00ff00;
			notification.ledOnMS = 300;
			notification.ledOffMS = 1000;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(Constants.NOTIFICATION_ID, notification);

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
		Context context = Application.getInstance();
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, SynchronizationService.class);
		PendingIntent pendingIntent = PendingIntent.getService(
				Application.getInstance(), 0, intent, 0);
		am.cancel(pendingIntent);
	}
}
