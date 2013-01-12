package com.cellasoft.univrapp.utils;

import android.app.Activity;

public class DashboardEntry {
	private int title;
	private int icon;
	private Class<? extends Activity> activity;

	public DashboardEntry(int title, int icon,
			Class<? extends Activity> activity) {
		this.title = title;
		this.icon = icon;
		this.activity = activity;
	}

	public int getTitle() {
		return title;
	}

	public int getIcon() {
		return icon;
	}

	public Class<? extends Activity> getActivity() {
		return activity;
	}
}