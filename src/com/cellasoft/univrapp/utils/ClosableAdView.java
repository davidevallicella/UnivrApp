package com.cellasoft.univrapp.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

public class ClosableAdView extends AdView implements AdListener {

	private static final String TAG = ClosableAdView.class.getSimpleName();

	// private static final int AD_CLOSABLE_BUTTON = 1;
	// private Context context;
	// private ImageButton closeAdmodButton;
	// private RelativeLayout adModLayout;

	// Handler handler = new Handler() {
	// @Override
	// public void handleMessage(Message msg) {
	// super.handleMessage(msg);
	// if (msg.what == AD_CLOSABLE_BUTTON) {
	// if (adModLayout != null) {
	// try {
	// ((ViewGroup) closeAdmodButton.getParent())
	// .removeView(closeAdmodButton);
	// adModLayout.addView(closeAdmodButton);
	// } catch (Exception e) {
	// }
	// }
	// }
	// }
	// };

	public ClosableAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// this.context = context;
	}

	public void inizialize(Context context) {
		// this.context = context;
		// adModLayout = (RelativeLayout) ((Activity)
		// context).findViewById(R.id.AdModLayout);
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
		// if (closeAdmodButton == null) {
		// if (Constants.DEBUG_MODE)
		// Log.d(TAG, "Init closable Ad button");
		// addCloseButtonTask();
		// } else {
		// viewAd();
		// }
	}

	public void hideAd() {
		try {

			if (Constants.DEBUG_MODE)
				Log.d(TAG, "Hide Ad");

			setVisibility(View.GONE);
			stopLoading();
			destroy();

			// if (closeAdmodButton != null) {
			// closeAdmodButton.setVisibility(View.GONE);
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void viewAd() {
		if (!Settings.hasPassed24Hour()) {
			hideAd();
			return;
		}

		if (Constants.DEBUG_MODE)
			Log.d(TAG, "View Ad");

		// try {
		// setVisibility(View.VISIBLE);
		// if (closeAdmodButton != null)
		// closeAdmodButton.setVisibility(View.VISIBLE);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public synchronized void loadAd() {
		if (!Settings.hasPassed24Hour()) {
			if (Constants.DEBUG_MODE)
				Log.d(TAG, "No load Ad");
			hideAd();
			return;
		}

		if (Constants.DEBUG_MODE)
			Log.d(TAG, "Load Ad");

		try {
			loadAd(new AdRequest());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// private void addCloseButtonTask() {
	// new AsyncTask<Void, Void, Void>() {
	//
	// @Override
	// protected void onPostExecute(Void result) {
	// handler.sendEmptyMessage(AD_CLOSABLE_BUTTON);
	// }
	//
	// @Override
	// protected Void doInBackground(Void... params) {
	// while (getHeight() == 0 && !isCancelled()) {
	// try {
	// Thread.sleep(5000);
	// } catch (InterruptedException e) {
	// cancel(true);
	// }
	// }
	//
	// RelativeLayout.LayoutParams closeLayoutParams = new
	// RelativeLayout.LayoutParams(
	// 30, 30);
	// closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
	// RelativeLayout.TRUE);
	// closeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT,
	// RelativeLayout.TRUE);
	//
	// closeLayoutParams.bottomMargin = (int) getHeight() - 15;
	// closeLayoutParams.leftMargin = 15;
	//
	// closeAdmodButton = new ImageButton(context);
	// closeAdmodButton.setLayoutParams(closeLayoutParams);
	// closeAdmodButton.setImageResource(R.drawable.close_button);
	// closeAdmodButton
	// .setBackgroundResource(android.R.color.transparent);
	// closeAdmodButton.setOnClickListener(new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// closeAdmodButton.setVisibility(View.GONE);
	// setVisibility(View.GONE);
	// }
	// });
	//
	// return null;
	// }
	// }.execute();
	// }

}
