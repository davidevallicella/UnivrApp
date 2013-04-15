package com.cellasoft.univrapp.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.widget.SynchronizationListener;

public class SynchronizationManager {
	private static final String TAG = SynchronizationManager.class.getName();
	
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

			if (Constants.DEBUG_MODE) Log.d(TAG, "Start synchronization at " + new Date());
			
			totalNewItems = syncFeeds();			
	      
			if (Constants.DEBUG_MODE) Log.d(TAG, "Stop synchronization at " + new Date());
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
						List<Item> newItems = channel.update(maxItemsForChannel);
						totalNewItems += newItems.size();
						channel.getItems().clear();
						ContentManager.cleanUp(channel, Settings.getKeepMaxItems()); 
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
