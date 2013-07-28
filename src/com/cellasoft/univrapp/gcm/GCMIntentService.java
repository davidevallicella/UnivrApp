/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cellasoft.univrapp.gcm;

import static com.cellasoft.univrapp.Config.GCM_SENDER_ID;
import static com.cellasoft.univrapp.utils.LogUtils.LOGD;
import static com.cellasoft.univrapp.utils.LogUtils.LOGE;
import static com.cellasoft.univrapp.utils.LogUtils.LOGI;
import static com.cellasoft.univrapp.utils.LogUtils.LOGW;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.activity.ChannelListActivity;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = makeLogTag("GCM");

	public GCMIntentService() {
		super(GCM_SENDER_ID);
	}

	/**
	 * Method called on device registered
	 **/
	@Override
	protected void onRegistered(Context context, String regId) {
		LOGI(TAG, "Device registered: regId = " + regId);
		ServerUtilities.register(context, Settings.getUniversity().name, regId);
	}

	/**
	 * Method called on device un registred
	 * */
	@Override
	protected void onUnregistered(Context context, String regId) {
		LOGI(TAG, "Device unregistered");

		if (GCMRegistrar.isRegisteredOnServer(context)) {
			GCMRegistrar.setRegisteredOnServer(context, false);
			// ServerUtilities.unregister(context, regId);
		} else {
			// This callback results from the call to unregister made on
			// ServerUtilities when the registration to the server failed.
			LOGD(TAG, "Ignoring unregister callback");
		}
	}

	/**
	 * Method called on Receiving a new message
	 * */
	@Override
	protected void onMessage(Context context, Intent intent) {
		LOGI(TAG, "Received message");
		String message = intent.getExtras().getString("announcement");
		if (message != null) {
			// notifies user
			displayNotification(context, message);
			return;
		}

		// int jitterMillis = (int) (sRandom.nextFloat() *
		// TRIGGER_SYNC_MAX_JITTER_MILLIS);
		// final String debugMessage = "Received message to trigger sync; "
		// + "jitter = " + jitterMillis + "ms";
		// LOGI(TAG, debugMessage);
		//
		// if (BuildConfig.DEBUG) {
		// displayNotification(context, debugMessage);
		// }
		//
		// ((AlarmManager) context.getSystemService(ALARM_SERVICE)).set(
		// AlarmManager.RTC, System.currentTimeMillis() + jitterMillis,
		// PendingIntent.getBroadcast(context, 0, new Intent(context,
		// TriggerSyncReceiver.class),
		// PendingIntent.FLAG_CANCEL_CURRENT));
	}

	/**
	 * Method called on receiving a deleted message
	 * */
	@Override
	protected void onDeletedMessages(Context context, int total) {
		LOGI(TAG, "Received deleted messages notification");
		String message = getString(R.string.gcm_deleted, total);
		// notifies user
		displayNotification(context, message);
	}

	/**
	 * Method called on Error
	 * */
	@Override
	public void onError(Context context, String errorId) {
		LOGE(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		LOGW(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private void displayNotification(Context context, String message) {
		LOGI(TAG, "displayNotification: " + message);
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		long[] vibraPattern = { 0, 500, 250, 500 };
		((NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(Config.GCM_NOTIFICATION_ID,
						new NotificationCompat.Builder(context)
								.setWhen(when)
								.setSmallIcon(icon)
								.setTicker(message)
								.setContentTitle(
										context.getString(R.string.app_name))
								.setContentText(message)
								.setContentIntent(
										PendingIntent
												.getActivity(
														context,
														0,
														new Intent(
																context,
																ChannelListActivity.class)
																.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
																		| Intent.FLAG_ACTIVITY_SINGLE_TOP),
														0)).setAutoCancel(true)
								.setVibrate(vibraPattern)
								.setDefaults(Notification.DEFAULT_SOUND)
								.getNotification());
	}

}