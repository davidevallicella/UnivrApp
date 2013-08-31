package com.cellasoft.univrapp.manager;

import static com.cellasoft.univrapp.utils.LogUtils.LOGD;
import static com.cellasoft.univrapp.utils.LogUtils.LOGE;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.util.Date;
import java.util.List;

import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.utils.Lists;
import com.cellasoft.univrapp.widget.SynchronizationListener;

public class SynchronizationManager {
	private static final String TAG = makeLogTag(SynchronizationManager.class);

	private static SynchronizationManager instance;
	private Object synRoot = new Object();
	private boolean synchronizing = false;
	private List<SynchronizationListener> synchronizationListeners;

	public SynchronizationManager() {
		synchronizationListeners = Lists.newArrayList();
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
				if (BuildConfig.DEBUG) {
					LOGD(TAG, "Synchronizing... return now.");
				}
				return 0;
			}
			synchronizing = true;
		}

		int totalNewItems = 0;

		if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {

			if (BuildConfig.DEBUG) {
				LOGD(TAG, "Start synchronization at " + new Date());
			}

			totalNewItems = syncFeeds();

			if (BuildConfig.DEBUG) {
				LOGD(TAG, "Stop synchronization at " + new Date());
			}
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
		}
	}

	public synchronized void unregisterSynchronizationListener(
			SynchronizationListener listener) {
		if (synchronizationListeners.contains(listener)) {
			synchronizationListeners.remove(listener);
		}
	}

	public void onSynchronizationStart(int id) {
		for (SynchronizationListener listener : synchronizationListeners) {
			listener.onStart(id);
		}
	}

	protected void onSynchronizationProgress(int id, long updateTime) {
		for (SynchronizationListener listener : synchronizationListeners) {
			listener.onProgress(id, updateTime);
		}
	}

	protected void onSynchronizationFinish(int totalNewItems) {
		for (SynchronizationListener listener : synchronizationListeners) {
			listener.onFinish(totalNewItems);
		}
	}

	protected int syncFeeds() {
		int totalNewItems = 0;
		int maxItemsForChannel = Settings.getMaxItemsForChannel();
		List<Channel> channels = Lists.newArrayList();

		channels = Channel
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
						List<Item> newItems = channel
								.update(maxItemsForChannel);
						if (!channel.mute) {
							totalNewItems += newItems.size();
						}

						// clean up memory
						channel.clearItems();
						ContentManager.cleanUp(channel,
								Settings.getKeepMaxItems());
					} catch (Throwable e) {
						LOGE(TAG, e.getMessage());
					}

					onSynchronizationProgress(channel.id, channel.updateTime);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LOGE(TAG, e.getMessage());
					}
				}
			}
		}

		// clean up memory
		channels.clear();
		channels = null;

		return totalNewItems;
	}
}
