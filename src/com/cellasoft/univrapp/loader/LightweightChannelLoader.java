package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Channel.Channels;

public class LightweightChannelLoader implements ChannelLoader {
	private final String[] projection = new String[] { Channels.ID,
			Channels.TITLE, Channels.URL, Channels.STARRED, Channels.MUTE };

	@Override
	public String[] getProjection() {
		return projection;
	}

	@Override
	public Channel load(Cursor cursor) {
		Channel channel = new Channel();
		channel.id = cursor.getInt(0);// cursor.getColumnIndex(Channels.ID));
		channel.title = cursor.getString(1);// cursor.getColumnIndex(Channels.TITLE));
		channel.url = cursor.getString(2);// cursor.getColumnIndex(Channels.URL));
		channel.starred = (cursor.getInt(3) != 0);
		channel.mute = (cursor.getInt(4) != 0);
		return channel;
	}

}
