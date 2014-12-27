package com.cellasoft.univrapp.activity;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.utils.ClosableAdView;
import com.cellasoft.univrapp.utils.FontUtils;

import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

public abstract class BaseListActivity extends SherlockListActivity {
    private static final String TAG = makeLogTag(BaseListActivity.class);

    protected ClosableAdView adView;
    protected boolean refresh = false;
    protected volatile boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            // UIUtils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);
        running = true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        FontUtils.setRobotoFont(this, getWindow().getDecorView());
        // Add the footer before adding the adapter, else the footer will not
        // load!
       // initBanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showAdmodBanner();
    }

    @Override
    protected void onDestroy() {
        running = false;
        if (adView != null) {
            adView.hideAd();
        }
        super.onDestroy();
    }

    private void initBanner() {
        // Look up the AdView as a resource.
        adView = (ClosableAdView) this.findViewById(R.id.adView);
        if (adView != null) {
            adView.init();
        }
    }

    private void showAdmodBanner() {
        if (adView != null) {
            adView.viewAd();
        }
    }

    protected abstract void loadData();

    protected abstract void initListView();

    protected abstract void initActionBar();
}