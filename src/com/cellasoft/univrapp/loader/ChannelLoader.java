package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Channel;

public interface ChannelLoader {
	String[] getProjection();

	Channel load(Cursor cursor);
}
