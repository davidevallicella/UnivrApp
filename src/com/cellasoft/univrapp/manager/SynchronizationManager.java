package com.cellasoft.univrapp.manager;

import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.utils.ApplicationContext;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.Utils;

public class SynchronizationManager {
	private static SynchronizationManager instance;

	private Object synRoot = new Object();
	private boolean synchronizing = false;
	int totalNewItems = 0;

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

	public int startSynchronizing(int id) {
		synchronized (synRoot) {
			if (synchronizing) {
				if (Constants.DEBUG_MODE)
					Log.d(Constants.LOG_TAG, "Synchronizing... return now.");
				return 0;
			}
			synchronizing = true;
		}

		int totalNewItems = 0;

		if (Utils.isNetworkAvailable(ApplicationContext.getInstance())) {
			if (Constants.DEBUG_MODE)
				Log.d(Constants.LOG_TAG, "Start synchronization at " + new Date());
			try {
				totalNewItems = syncFeeds(id);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			if (Constants.DEBUG_MODE)
				Log.d(Constants.LOG_TAG, "Stop synchronization at " + new Date());
		}

		synchronized (synRoot) {
			synchronizing = false;
		}
		return totalNewItems;
	}

	public void stopSynchronizing() {
		synchronized (synRoot) {
			synchronizing = false;
		}
	}

	protected int syncFeeds(int id) {
		int totalNewItems = 0;

		ArrayList<Channel> channels = new ArrayList<Channel>();
		if (id > 0) {
			channels.add(ContentManager.loadChannel(id,
					ContentManager.LIGHTWEIGHT_CHANNEL_LOADER));
		} else {
			channels = ContentManager
					.loadAllChannels(ContentManager.LIGHTWEIGHT_CHANNEL_LOADER);
		}
		for (Channel channel : channels) {
			if (!channel.starred && id == 0) {
				continue;
			}

			VALIDATION: {
				synchronized (synRoot) {
					if (!synchronizing)
						break VALIDATION;
				}

				try {
					int newItems = channel.update();
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

		// clean up memory
		channels = null;

		return totalNewItems;
	}
}
