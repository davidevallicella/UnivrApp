package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.model.Lecturer.Lecturers;

public class LightweightLecturerLoader implements LecturerLoader {
	private final String[] projection = new String[] { Lecturers.KEY,
			Lecturers.DEST, Lecturers.NAME };

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
		lecturer.name = cursor.getString(2);
		return lecturer;
	}
}