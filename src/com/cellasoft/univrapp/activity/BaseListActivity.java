package com.cellasoft.univrapp.activity;

import static com.cellasoft.univrapp.utils.LogUtils.LOGD;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.utils.ClosableAdView;
import com.cellasoft.univrapp.utils.FontUtils;

public abstract class BaseListActivity extends SherlockListActivity {
	private static final String TAG = makeLogTag(BaseListActivity.class);

	protected ClosableAdView adView;
	protected boolean refresh = false;
	protected volatile boolean running;

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
		FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
		// Add the footer before adding the adapter, else the footer will not
		// load!
		initBanner();
		super.onPostCreate(savedInstanceState);
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
		adView.init();
	}

	private void showAdmodBanner() {
		if (adView != null) {
			adView.viewAd();
		}
	}

	protected void unbindDrawables(View view) {
		if (view == null) {
			return;
		}

		if (BuildConfig.DEBUG) {
			LOGD(TAG, "unbindDrawables()" + view.getId());
		}

		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}

		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			if (view instanceof AdapterView) {

			} else {
				((ViewGroup) view).removeAllViews();
			}
		}
	}

	protected abstract void loadData();

	protected abstract void initListView();

	protected abstract void initActionBar();
}
