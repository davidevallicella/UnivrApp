package com.cellasoft.univrapp.model;

import java.io.Serializable;
import java.util.List;
import java.util.Observable;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.cellasoft.univrapp.loader.ChannelLoader;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.provider.Provider;
import com.cellasoft.univrapp.rss.RSSHandler.OnNewEntryCallback;
import com.cellasoft.univrapp.rss.SaxFeedParser;
import com.cellasoft.univrapp.utils.ActiveList;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.UnivrReader;
import com.cellasoft.univrapp.utils.UnivrReaderFactory;

public class Channel extends Observable implements ActionSupport, Serializable {

	private static final long serialVersionUID = 6999952067033640004L;
	private transient ActiveList<Item> items = new ActiveList<Item>();
	private Object synRoot = new Object();
	private boolean updating = false;

	public int id = 0;
	public String url;
	public String title;
	public String description;
	public String imageUrl;
	public boolean isSelected;
	public boolean starred;

	public Channel() {
	}

	public Channel(int id) {
		this.id = id;
	}

	public Channel(String url) {
		this("", url);
	}

	public Channel(String title, String url) {
		this.title = title;
		this.url = url;
	}

	public Channel(String title, String url, String imageUrl) {
		this(title, url);
		this.imageUrl = imageUrl;
	}

	public ActiveList<Item> getItems() {
		synchronized (items) {
			if (items == null) {
				items = new ActiveList<Item>();
			}
			return items;
		}
	}

	public void clearItems() {
		getItems().clear();
	}

	public boolean existItem(Item item) {
		return this.getItems().indexOf(item) >= 0;
	}

	public void addItem(Item item) {
		item.channel = this;

		synchronized (synRoot) {
			ActiveList<Item> items = this.getItems();
			if (items.indexOf(item) < 0) {
				// find insert location
				int position = 0;
				for (Item currentItem : this.items) {
					if (currentItem.pubDate.before(item.pubDate)) {
						items.add(position, item);
						return;
					}
					position++;
				}
				items.add(item);
			}
		}
	}

	public void addItem(int position, Item item) {
		synchronized (synRoot) {
			ActiveList<Item> items = this.getItems();
			if (items.indexOf(item) < 0)
				items.add(position, item);
		}
	}

	public boolean isUpdating() {
		synchronized (synRoot) {
			return updating;
		}
	}

	public int update(int maxItems) {
		synchronized (synRoot) {
			if (updating)
				return 0;
			updating = true;
			this.setChanged();
			this.notifyObservers(updating);
		}

		int newItems = updateItems(maxItems);
		if (newItems > 0)
			newItems = saveItems();

		synchronized (synRoot) {
			updating = false;
			this.setChanged();
			this.notifyObservers(updating);
		}

		return newItems;
	}

	protected int updateItems(int maxItems) {
		int numberOfFetchedItems = 0;
		try {
			UnivrReader reader = UnivrReaderFactory.getGoogleReader();
			while (true) {
				SaxFeedParser feed = reader.fetchEntriesOfFeed(this, maxItems,
						new OnNewEntryCallback() {

							@Override
							public void onNewEntry(Item item) {
								if (item.exist()) {
									throw new RuntimeException(
											"Found exist item. Stop parsing "
													+ Channel.this.title);
								} else {
									item.channel = Channel.this;
								}
							}
						});

				int position = 0;
				List<Item> items = feed.getEntries();
				for (Item item : items) {
					this.addItem(position, item);
					position++;
				}

				numberOfFetchedItems += feed.getEntries().size();
				if (numberOfFetchedItems >= maxItems
						|| feed.getEntries().size() < Constants.MAX_ITEMS_PER_FETCH) {
					break;
				}

				try {
					Thread.yield();
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			Log.e("ERROR", e.getMessage());
		} catch (Exception e) {
			Log.e("ERROR", e.getMessage());
		}
		
		return numberOfFetchedItems;
	}

	private int saveItems() {
		int newItems = 0;
		if (items != null) {
			if (exist()) {
				for (Item item : items) {
					if (item.save()) {
						newItems++;
					}
				}
			}
		}
		return newItems;
	}

	public boolean isEmpty() {
		return this.getItems().size() == 0;
	}

	public int size() {
		return getItems().size();
	}

	public int indexOf(Item item) {
		return getItems().indexOf(item);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: ").append(title).append("\n");
		sb.append("Description: ").append(description).append('\n');
		sb.append("Url: ").append(url).append('\n');
		return sb.toString();
	}

	@Override
	public boolean save() {
		return ContentManager.saveChannel(this);
	}

	@Override
	public void delete() {
		this.clearItems();
		ContentManager.deleteChannel(this);
	}

	@Override
	public boolean exist() {
		return ContentManager.existChannel(this);
	}

	public void clean() {
		this.clearItems();
		ContentManager.cleanChannel(this, 1);
	}

	public void loadLightweightItems() {
		ContentManager.loadAllItemsOfChannel(this,
				ContentManager.LIGHTWEIGHT_ITEM_LOADER);
	}

	public void loadFullItems() {
		ContentManager.loadAllItemsOfChannel(this,
				ContentManager.FULL_ITEM_LOADER);
	}

	public static Channel findById(int id, ChannelLoader loader) {
		return ContentManager.loadChannel(id, loader);
	}

	public static final class Channels implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ Provider.AUTHORITY + "/channels");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cellasoft.univrapp.provider.channels";

		public static final String ID = "ID";
		public static final String TITLE = "TITLE";
		public static final String URL = "URL";
		public static final String DESCRIPTION = "DESCRIPTION";
		public static final String STARRED = "STARRED";
		public static final String IMAGE_URL = "IMAGE_URL";

		private Channels() {
		}
	}
}
