package com.cellasoft.univrapp.activity;

import java.util.Locale;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.cellasoft.univrapp.Settings;
import com.github.droidfu.activities.BetterDefaultActivity;

public class LocalizedActivity extends BetterDefaultActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = new Locale(Settings.getLocale());
		res.updateConfiguration(conf, dm);

		super.onCreate(savedInstanceState);
	}
}