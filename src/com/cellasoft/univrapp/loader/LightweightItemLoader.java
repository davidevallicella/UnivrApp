package com.cellasoft.univrapp.loader;

import java.sql.Timestamp;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.model.Item.Items;

public class LightweightItemLoader implements ItemLoader {
	private final String[] projection = new String[] { 
			Items.ID,
			Items.TITLE,
			Items.PUB_DATE, 
			Items.UPDATE_TIME, 
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
		item.pubDate = new Timestamp(cursor.getLong(3));// cursor.getColumnIndex(Items.PUB_DATE));
		item.updateTime = cursor.getLong(3);
		item.channel = new Channel(cursor.getInt(4));
		return item;
	}

}