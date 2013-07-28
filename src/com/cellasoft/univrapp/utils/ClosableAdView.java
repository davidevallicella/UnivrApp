package com.cellasoft.univrapp.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.Settings;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

public class ClosableAdView extends AdView implements AdListener {

	private static final String TAG = ClosableAdView.class.getSimpleName();

	public ClosableAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void inizialize() {
		setAdListener(this);
	}

	@Override
	public void onDismissScreen(Ad arg0) {
	}

	@Override
	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
	}

	@Override
	public void onLeaveApplication(Ad arg0) {
	}

	@Override
	public void onPresentScreen(Ad arg0) {
		Calendar cal = Calendar.getInstance(Locale.ITALY);
		cal.setTime(new Date());
		Settings.setAdClickTime(cal.getTime().getTime());
		hideAd();
	}

	@Override
	public void onReceiveAd(Ad arg0) {
	}

	public void hideAd() {
		if (Config.DEBUG_MODE)
			Log.d(TAG, "Hide Ad");

		setVisibility(View.GONE);

		try {
			stopLoading();
			destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void viewAd() {
		if (!Settings.hasPassed24Hour()) {
			return;
		}

		if (Config.DEBUG_MODE)
			Log.d(TAG, "View Ad");

		setVisibility(View.VISIBLE);
		
		loadAd();
	}

	public void loadAd() {
		if (!Settings.hasPassed24Hour()) {
			if (Config.DEBUG_MODE)
				Log.d(TAG, "No load Ad");
			hideAd();
			return;
		}

		if (Config.DEBUG_MODE)
			Log.d(TAG, "Load Ad");

		try {
			loadAd(new AdRequest());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
