package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.RSSItem;

public interface ItemLoader {
	String[] getProjection();

	RSSItem load(Cursor cursor);
}