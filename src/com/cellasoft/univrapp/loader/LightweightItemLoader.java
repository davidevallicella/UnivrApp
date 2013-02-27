package com.cellasoft.univrapp.loader;

import java.util.Date;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.model.Item.Items;

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
	public Item load(Cursor cursor) {
		Item item = new Item();
		item.id = cursor.getInt(0);// cursor.getColumnIndex(Items.ID));
		item.title = cursor.getString(1);// cursor.getColumnIndex(Items.TITLE));
		item.pubDate = new Date(cursor.getLong(2));// cursor.getColumnIndex(Items.PUB_DATE));
		item.channel = new Channel(cursor.getInt(3));
		return item;
	}

}