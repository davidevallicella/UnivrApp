package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.RSSItem;
import com.cellasoft.univrapp.model.RSSItem.Items;

public class LightweightItemLoader implements ItemLoader {
	private final String[] projection = new String[] { 
			Items.ID,
			Items.TITLE,
			Items.PUB_DATE, 
			Items.CHANNEL_ID,
			};

	@Override
	public String[] getProjection() {
		return projection;
	}

	@Override
	public RSSItem load(Cursor cursor) {
		RSSItem item = new RSSItem();
		item._id = cursor.getInt(0);// cursor.getColumnIndex(Items.ID));
		item._title = cursor.getString(1);// cursor.getColumnIndex(Items.TITLE));
		item._pubDate = cursor.getString(3);// cursor.getColumnIndex(Items.PUB_DATE));
		item._channel = new Channel(cursor.getInt(5));
		return item;
	}

}