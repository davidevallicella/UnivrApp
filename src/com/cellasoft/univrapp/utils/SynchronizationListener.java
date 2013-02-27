package com.cellasoft.univrapp.utils;

public interface SynchronizationListener {
	void onStart();

	void onProgress(String progressText);

	void onFinish(int totalNewItems);
}