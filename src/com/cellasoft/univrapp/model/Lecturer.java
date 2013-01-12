package com.cellasoft.univrapp.model;

import android.net.Uri;
import android.provider.BaseColumns;

import com.cellasoft.univrapp.provider.Provider;

public class Lecturer {

	public int id;
	public String name;
	public int key;
	public int dest;
	public String thumbnail;

	public Lecturer() {
	}

	public Lecturer(int key, int dest, String name, String thumbnail) {
		this.key = key;
		this.dest = dest;
		this.name = name;
		this.thumbnail = thumbnail;
	}

	@Override
	public String toString() {
		return name;
	}

	public static final class Lecturers implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ Provider.AUTHORITY + "/lecturers");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cellasoft.univrapp.provider.lecturers";

		public static final String ID = "ID";
		public static final String KEY = "KEY";
		public static final String DEST = "DEST";
		public static final String NAME = "NAME";
		public static final String THUMBNAIL = "THUMBNAIL";
	}
}
