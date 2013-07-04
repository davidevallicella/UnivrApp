package com.cellasoft.univrapp.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import android.net.Uri;
import android.provider.BaseColumns;

import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.provider.Provider;

public class Item implements ActionSupport, Comparable<Item>, Serializable {

	private static final long serialVersionUID = 8512014197691115155L;
	public static final int UNREAD = 0;
	public static final int READ = 1;
	public static final int TEMPORARILY_MARKED_AS_READ = 2;
	public static final int KEPT_UNREAD = 3;

	// All <item> node name
	public int id;
	public String title;
	public String link;
	public String description;
	public String category;
	public Date pubDate;
	public String guid;
	public int read;
	public long updateTime;
	public Channel channel;

	public Item() {
		this.id = 0;
		this.read = UNREAD;
		this.pubDate = new Timestamp(System.currentTimeMillis());
	}

	public Item(int id) {
		this();
		this.id = id;
	}

	// constructor with parameters
	public Item(String title, String link, String description, Date pubDate,
			String guid) {
		this.title = title;
		this.link = link;
		this.description = description;
		if (pubDate != null)
			this.pubDate = pubDate;
		this.guid = guid;
	}

	public boolean isRead() {
		return this.read == READ;
	}

	/**
	 * All SET methods
	 * */
	public void setTitle(String title) {
		this.title = title;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setDescription(String description) {
		this.description = description.trim();
	}

	public void setDate(Date pubDate) {
		if (pubDate != null) {
			this.pubDate = pubDate;
		}
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * All GET methods
	 * */
	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public String getDescription() {
		return description;
	}

	public Date getPubDate() {
		return pubDate;
	}

	public String getGuid() {
		return guid;
	}

	@Override
	public int compareTo(Item another) {
		if (another == null)
			return 1;
		return another.pubDate.compareTo(pubDate);
	}

	@Override
	public int hashCode() {
		return 31 + ((link == null) ? 0 : link.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;

		if (id != 0 && other.id != 0) {
			return id == other.id;
		} else if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		return true;
	}

	public void markItemAsRead() {
		ContentManager.markItemAsRead(this);
	}

	@Override
	public boolean save() {
		return ContentManager.saveItem(this);
	}

	@Override
	public void delete() {
		ContentManager.deleteItem(this);
	}

	@Override
	public boolean exist() {
		return ContentManager.existItem(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: ").append(title).append("\n");
		sb.append("Link: ").append(link).append('\n');
		sb.append("Description: ").append(description).append("\n");
		sb.append("Date: ").append(pubDate).append('\n');
		sb.append("Guid: ").append(guid).append('\n');
		return sb.toString();
	}

	public static final class Items implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ Provider.AUTHORITY + "/items");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cellasoft.univrapp.provider.items";

		public static final String ID = "ID";
		public static final String TITLE = "TITLE";
		public static final String DESCRIPTION = "DESCRIPTION";
		public static final String PUB_DATE = "PUB_DATE";
		public static final String LINK = "LINK";
		public static final String READ = "READ";
		public static final String CHANNEL_ID = "CHANNEL_ID";
		public static final String UPDATE_TIME = "UPDATE_TIME";
		public static final String UNREAD_COUNT = "UNREAD";
		public static final String COUNT = "COUNT(DISTINCT ID)";

		public static final Uri hasTagAndLimit(int limit) {
			return Uri.parse("content://" + Provider.AUTHORITY + "/items/tag/"
					+ limit);
		}

		public static final Uri count() {
			return Uri
					.parse("content://" + Provider.AUTHORITY + "/items/count");
		}

		public static final Uri countUnreadEachChannel() {
			return Uri.parse("content://" + Provider.AUTHORITY
					+ "/items/unread");
		}

		public static final Uri countUnread() {
			return Uri.parse("content://" + Provider.AUTHORITY
					+ "/items/unread/all");
		}

		public static final Uri limit(int limit) {
			return Uri.parse("content://" + Provider.AUTHORITY + "/items/"
					+ limit);
		}

		public static final Uri limitAndStartAt(int limit, int offset) {
			return Uri.parse("content://" + Provider.AUTHORITY + "/items/"
					+ limit + "/" + offset);
		}

	}
}
