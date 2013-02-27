package com.cellasoft.univrapp.loader;

import java.util.Date;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.model.Item.Items;

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
	public Item load(Cursor cursor) {
		// using magic numbers !!!
		Item item = new Item();
		item.id = cursor.getInt(0);
		item.title = cursor.getString(1);
		item.description = cursor.getString(2);
		item.pubDate = new Date(cursor.getLong(3));
		item.link = cursor.getString(4);
		item.channel = new Channel(cursor.getInt(5));
		return item;
	}
}