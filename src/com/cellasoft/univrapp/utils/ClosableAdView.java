package com.cellasoft.univrapp.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.Settings;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.cellasoft.univrapp.utils.LogUtils.*;

public class ClosableAdView extends AdView implements AdListener {

    private static final String TAG = makeLogTag(ClosableAdView.class);

    public ClosableAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init() {
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
        if (BuildConfig.DEBUG)
            LOGD(TAG, "Hide Ad");

        setVisibility(View.GONE);

        try {
            stopLoading();
            //destroy();
        } catch (Exception e) {
            LOGE(TAG, "Destroy AdMod banner - " + e.getMessage());
        }
    }

    public void viewAd() {
        if (!Settings.hasPassed24Hours()) {
            if (BuildConfig.DEBUG) {
                LOGD(TAG, "No load Ad");
            }
            hideAd();
            return;
        }

        if (BuildConfig.DEBUG) {
            LOGD(TAG, "View Ad");
        }

        setVisibility(View.VISIBLE);
        setFocusable(true);
        requestFocus();

        loadAd();
    }

    private void loadAd() {
        try {
            loadAd(new AdRequest());
        } catch (Exception e) {
            LOGE(TAG, "Load AdMod banner - " + e.getMessage());
        }
    }
}
