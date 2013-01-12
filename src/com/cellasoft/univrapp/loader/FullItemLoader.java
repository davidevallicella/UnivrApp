package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.RSSItem;
import com.cellasoft.univrapp.model.RSSItem.Items;

public class FullItemLoader implements ItemLoader {
	private final String[] projection = new String[] { 
			Items.ID, 
			Items.TITLE, 
			Items.DESCRIPTION,
			Items.PUB_DATE, 
			Items.LINK, 
			Items.CHANNEL_ID, 
			};	
	
	@Override
	public String[] getProjection() {
		return projection;
	}

	@Override
	public RSSItem load(Cursor cursor) {
		// using magic numbers !!!
		RSSItem item = new RSSItem();
		item._id = cursor.getInt(0);
		item._title = cursor.getString(1);
		item._description = cursor.getString(2);
		item._pubDate = cursor.getString(3);
		item._link = cursor.getString(4);
		item._channel = new Channel(cursor.getInt(5));
		return item;
	}
}