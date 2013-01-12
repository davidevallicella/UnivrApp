package com.cellasoft.univrapp.model;

import android.net.Uri;
import android.provider.BaseColumns;

import com.cellasoft.univrapp.provider.Provider;

public class University {

	public int id;
	public String name;
	public String url;
	
	
	
	public static final class Universitys implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ Provider.AUTHORITY + "/universitys");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cellasoft.univrapp.provider.universitys";
	
		public static final String ID = "ID";
		public static final String NAME = "NAME";
		public static final String DEST = "DEST";
		public static final String URL = "URL";
	}
}
