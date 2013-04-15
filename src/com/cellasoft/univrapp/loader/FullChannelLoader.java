package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Channel.Channels;

public class FullChannelLoader implements ChannelLoader {
	private final String[] projection = new String[] { 
			Channels.ID,
			Channels.LECTURER_ID,
			Channels.TITLE,
			Channels.URL,
			Channels.DESCRIPTION,
			Channels.UPDATE_TIME,
			Channels.STARRED,
			Channels.IMAGE_URL
			};

	@Override
	public String[] getProjection() {
		return projection;
	}

	@Override
	public Channel load(Cursor cursor) {
		Channel channel = new Channel();
		channel.id = cursor.getInt(0);// cursor.getColumnIndex(Channels.ID));
		channel.lecturerId = cursor.getInt(1);
		channel.title = cursor.getString(2);// cursor.getColumnIndex(Channels.TITLE));
		channel.url = cursor.getString(3);// cursor.getColumnIndex(Channels.URL));
		channel.description = cursor.getString(4);// cursor.getColumnIndex(Channels.DESCRIPTION));
		channel.updateTime = cursor.getLong(5);
		channel.starred = (cursor.getInt(6) != 0);
		channel.imageUrl = cursor.getString(7);
		return channel;
	}

}
