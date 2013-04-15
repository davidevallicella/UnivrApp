package com.cellasoft.univrapp.loader;

import java.sql.Timestamp;

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
			Items.UPDATE_TIME, 
			Items.READ,
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
		item.pubDate = new Timestamp(cursor.getLong(3));
		item.updateTime = cursor.getLong(4);
		item.read = cursor.getInt(5);
		item.link = cursor.getString(6);
		item.channel = new Channel(cursor.getInt(7));
		return item;
	}
}