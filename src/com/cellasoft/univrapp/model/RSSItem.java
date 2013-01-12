package com.cellasoft.univrapp.model;

import java.io.Serializable;

import android.net.Uri;
import android.provider.BaseColumns;

import com.cellasoft.univrapp.provider.Provider;
import com.cellasoft.univrapp.utils.DateUtils;

public class RSSItem implements Comparable<RSSItem>, Serializable {

	private static final long serialVersionUID = 8512014197691115155L;

	// All <item> node name
	public long _id;
	public String _title;
	public String _link;
	public String _description;
	public String _category;
	public String _pubDate;
	public String _guid;
	public Channel _channel;

	public RSSItem() {
	}

	// constructor with parameters
	public RSSItem(String title, String link, String description,
			String pubDate, String guid) {
		this._title = title;
		this._link = link;
		this._description = description;
		this._pubDate = DateUtils.formatDate(DateUtils.parseDate(pubDate));
		this._guid = guid;
	}

	/**
	 * All SET methods
	 * */
	public void setTitle(String title) {
		this._title = title;
	}

	public void setLink(String link) {
		this._link = link;
	}

	public void setDescription(String description) {
		this._description = description.trim();
	}

	public void setDate(String pubDate) {
		if (pubDate != null && pubDate.length() > 0) {
			_pubDate = DateUtils.formatDate(DateUtils.parseDate(pubDate));
		}
	}

	public void setGuid(String guid) {
		this._guid = guid;
	}

	/**
	 * All GET methods
	 * */
	public String getTitle() {
		return _title;
	}

	public String getLink() {
		return _link;
	}

	public String getDescription() {
		return _description;
	}

	public String getDate() {
		return _pubDate;
	}

	public String getGuid() {
		return _guid;
	}

	@Override
	public int compareTo(RSSItem another) {
		if (another == null)
			return 1;
		return another._pubDate.compareTo(_pubDate);
	}

	@Override
	public int hashCode() {
		return 31 + ((_link == null) ? 0 : _link.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RSSItem other = (RSSItem) obj;

		if (_id != 0 && other._id != 0) {
			return _id == other._id;
		} else if (_link == null) {
			if (other._link != null)
				return false;
		} else if (!_link.equals(other._link))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: ").append(_title).append("\n");
		sb.append("Link: ").append(_link).append('\n');
		sb.append("Description: ").append(_description).append("\n");
		sb.append("Date: ").append(_pubDate).append('\n');
		sb.append("Guid: ").append(_guid).append('\n');
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
		public static final String CHANNEL_ID = "CHANNEL_ID";

		public static final Uri limitAndStartAt(int limit, int offset) {
			return Uri.parse("content://" + Provider.AUTHORITY + "/items/"
					+ limit + "/" + offset);
		}
	}
}
