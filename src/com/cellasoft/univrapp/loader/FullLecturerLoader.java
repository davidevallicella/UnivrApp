package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.model.Lecturer.Lecturers;

public class FullLecturerLoader implements LecturerLoader {
	private final String[] projection = new String[] { 
			Lecturers.KEY,
			Lecturers.DEST,
			Lecturers.THUMBNAIL,
			Lecturers.NAME, 
			Lecturers.EMAIL };

	@Override
	public String[] getProjection() {
		return projection;
	}

	@Override
	public Lecturer load(Cursor cursor) {
		// using magic numbers !!!
		Lecturer lecturer = new Lecturer();
		lecturer.key = cursor.getInt(0);
		lecturer.dest = cursor.getInt(1);
		lecturer.thumbnail = cursor.getString(2);
		lecturer.name = cursor.getString(3);
		lecturer.mail = cursor.getString(4);
		return lecturer;
	}
}
