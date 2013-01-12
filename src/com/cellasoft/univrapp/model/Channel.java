package com.cellasoft.univrapp.model;

import java.io.Serializable;
import java.util.Observable;

import android.net.Uri;
import android.provider.BaseColumns;

import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.provider.Provider;
import com.cellasoft.univrapp.rss.SaxFeedParser;
import com.cellasoft.univrapp.utils.ActiveList;

public class Channel extends Observable implements Serializable {

	private static final long serialVersionUID = 6999952067033640004L;
	private transient ActiveList<RSSItem> items = new ActiveList<RSSItem>();
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

	public ActiveList<RSSItem> getItems() {
		synchronized (items) {
			if (items == null) {
				items = new ActiveList<RSSItem>();
			}
			return items;
		}
	}

	public void clearItems() {
		getItems().clear();
	}

	public boolean existItem(RSSItem item) {
		return this.getItems().indexOf(item) >= 0;
	}

	public void addItem(RSSItem item) {
		synchronized (synRoot) {
			ActiveList<RSSItem> items = this.getItems();
			if (items.indexOf(item) < 0)
				items.add(item);

		}
	}

	public void insertItem(int location, RSSItem item) {
		synchronized (synRoot) {
			ActiveList<RSSItem> items = this.getItems();
			if (items.indexOf(item) < 0)
				items.add(location, item);
		}
	}

	public boolean isUpdating() {
		synchronized (synRoot) {
			return updating;
		}
	}

	public int update() {
		synchronized (synRoot) {
			if (updating) 
				return 0;
			
			updating = true;
			this.setChanged();
			this.notifyObservers(updating);
		}

		parse();
		int newItems = saveItems();

		synchronized (synRoot) {
			updating = false;
			this.setChanged();
			this.notifyObservers(updating);
		}

		return newItems;
	}

	protected void parse() {
		new SaxFeedParser(url).parse(this);
	}

	private int saveItems() {
		int newItems = 0;
		if (items != null) {
			if (ContentManager.existChannel(this)) {
				for (RSSItem item : items) {
					if (ContentManager.saveItem(item)) {
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

	public int indexOf(RSSItem item) {
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

	public void loadLightweightItems() {
		ContentManager.loadAllItemsOfChannel(this,
				ContentManager.LIGHTWEIGHT_ITEM_LOADER);
	}
	
	public void loadFullItems() {
		ContentManager.loadAllItemsOfChannel(this,
				ContentManager.FULL_ITEM_LOADER);
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
