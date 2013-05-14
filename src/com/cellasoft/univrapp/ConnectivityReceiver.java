package com.cellasoft.univrapp;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cellasoft.univrapp.service.SynchronizationService;

public class ConnectivityReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent updatingService = new Intent(context,
				SynchronizationService.class);

		@SuppressWarnings("deprecation")
		NetworkInfo info = intent
				.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		if (hasGoodEnoughNetworkConnection(info, context)) {
			// should start background service to update
			if (Constants.DEBUG_MODE)
				Log.d(Constants.LOG_TAG,
						"Have WIFI or 3G connection, start background services...");
			context.startService(updatingService);
		} else {
			if (Constants.DEBUG_MODE)
				Log.d(Constants.LOG_TAG,
						"No WIFI or 3G connection, stop background services...");
			context.stopService(updatingService);
		}
	}

	public static boolean hasGoodEnoughNetworkConnection() {
		Context context = Application.getInstance();
		NetworkInfo info = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		return hasGoodEnoughNetworkConnection(info, context);
	}

	public static boolean hasGoodEnoughNetworkConnection(NetworkInfo info,
			Context context) {

		if (info == null)
			return false;

		// Only update if WiFi or 3G is connected and not roaming
		int netType = info.getType();
		int netSubtype = info.getSubtype();
		if (netType == ConnectivityManager.TYPE_WIFI) {
			if (info.isConnected()) {
				return isOnline();
			}
		}

		if (Settings.getWifiOnly())
			return false;

		if (isConnectionFast(netType, netSubtype)) {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (!telephonyManager.isNetworkRoaming()) {
				if (info.isConnected()) {
					return isOnline();
				}
			}
		}

		return false;
	}

	public static boolean isOnline() {
		HttpURLConnection urlc = null;
		int statusCode = HttpStatus.SC_NO_CONTENT;
		try {
			URL url = new URL("http://www.google.com");
			urlc = (HttpURLConnection) url.openConnection();
			urlc.setConnectTimeout(3000);
			urlc.connect();
			statusCode = urlc.getResponseCode();
			urlc.getOutputStream().close();
		} catch (Exception e) {
		} finally {
			urlc.disconnect();
		}

		return statusCode == HttpStatus.SC_OK;
	}

	/**
	 * Check if the connection is fast
	 * 
	 * @param type
	 * @param subType
	 * @return
	 */
	public static boolean isConnectionFast(int type, int subType) {
		if (type == ConnectivityManager.TYPE_WIFI) {
			return true;
		} else if (type == ConnectivityManager.TYPE_MOBILE) {
			switch (subType) {
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return true; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return false; // ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return true; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return true; // ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return true; // ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return true; // ~ 100 kbps
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return true; // ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return true; // ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return true; // ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return true; // ~ 400-7000 kbps
				/*
				 * Above API level 7, make sure to set android:targetSdkVersion
				 * to appropriate level to use these
				 */
			case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
				return true; // ~ 1-2 Mbps
			case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
				return true; // ~ 5 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
				return true; // ~ 10-20 Mbps
			case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
				return false; // ~25 kbps
			case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
				return true; // ~ 10+ Mbps
				// Unknown
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			default:
				return false;
			}
		} else {
			return false;
		}
	}
}
