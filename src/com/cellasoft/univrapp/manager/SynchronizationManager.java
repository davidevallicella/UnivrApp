package com.cellasoft.univrapp.manager;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.utils.Application;
import com.cellasoft.univrapp.utils.ConnectivityReceiver;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.Settings;
import com.cellasoft.univrapp.utils.SynchronizationListener;
import com.cellasoft.univrapp.utils.Utils;

public class SynchronizationManager {
	private static SynchronizationManager instance;
	private Object synRoot = new Object();
	private boolean synchronizing = false;
	private String progress;
	private List<SynchronizationListener> synchronizationListeners;

	public SynchronizationManager() {
		synchronizationListeners = new ArrayList<SynchronizationListener>();
	}

	static {
		instance = new SynchronizationManager();
	}

	public static SynchronizationManager getInstance() {
		return instance;
	}

	public boolean isSynchronizing() {
		synchronized (synRoot) {
			return synchronizing;
		}
	}

	public int startSynchronizing() {
		synchronized (synRoot) {
			if (synchronizing) {
				if (Constants.DEBUG_MODE)
					Log.d(Constants.LOG_TAG, "Synchronizing... return now.");
				return 0;
			}
			synchronizing = true;
		}

		onSynchronizationStart();  
		int totalNewItems = 0;

		if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {

			final long startMillis = System.currentTimeMillis();
			try {
				totalNewItems = syncFeeds();
			} catch (Throwable t) {
				t.printStackTrace();
			}

			if (Constants.DEBUG_MODE)
				Log.i(Constants.LOG_TAG,
						"Sync channels to "
								+ (System.currentTimeMillis() - startMillis)
								/ 1000 + "s");
		}

		synchronized (synRoot) {
			synchronizing = false;
		}
		
		onSynchronizationFinish(totalNewItems);

		return totalNewItems;
	}

	public void stopSynchronizing() {
		synchronized (synRoot) {
			synchronizing = false;
		}
	}

	public synchronized void registerSynchronizationListener(
			SynchronizationListener listener) {
		if (!synchronizationListeners.contains(listener)) {
			synchronizationListeners.add(listener);
			if (progress != null) {
				listener.onProgress(progress);
			}
		}
	}

	public synchronized void unregisterSynchronizationListener(
			SynchronizationListener listener) {
		if (synchronizationListeners.contains(listener)) {
			synchronizationListeners.remove(listener);
		}
	}

	protected void onSynchronizationStart() {
		for (SynchronizationListener listener : synchronizationListeners) {
			listener.onStart();
		}
	}

	protected void onSynchronizationProgress(String progressText) {
		this.progress = progressText;
		for (SynchronizationListener listener : synchronizationListeners) {
			listener.onProgress(progressText);
		}
	}

	protected void onSynchronizationFinish(int totalNewItems) {
		progress = null;
		for (SynchronizationListener listener : synchronizationListeners) {
			listener.onFinish(totalNewItems);
		}
	}

	protected int syncFeeds() {
		int totalNewItems = 0;
		int maxItemsForChannel = Settings.getMaxItemsForChannel();
		ArrayList<Channel> channels = new ArrayList<Channel>();

		channels = ContentManager
				.loadAllChannels(ContentManager.LIGHTWEIGHT_CHANNEL_LOADER);

		for (Channel channel : channels) {

			// sync selected channels
			if (channel.starred) {
				VALIDATION: {
					synchronized (synRoot) {
						if (!synchronizing)
							break VALIDATION;
					}

					try {
						int newItems = channel.update(maxItemsForChannel);
						totalNewItems += newItems;
						channel.getItems().clear();
					} catch (Throwable ex) {
						ex.printStackTrace();
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// clean up memory
		channels = null;

		return totalNewItems;
	}
}
