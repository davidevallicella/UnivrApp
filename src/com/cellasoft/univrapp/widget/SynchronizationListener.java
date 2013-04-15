package com.cellasoft.univrapp.widget;

public interface SynchronizationListener {
	void onStart();

	void onProgress(String progressText);

	void onFinish(int totalNewItems);
}