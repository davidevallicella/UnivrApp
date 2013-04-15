package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.model.Lecturer.Lecturers;

public class FullLecturerLoader implements LecturerLoader {
	private final String[] projection = new String[] { 
			Lecturers.ID,
			Lecturers.KEY,
			Lecturers.DEST,
			Lecturers.THUMBNAIL,
			Lecturers.NAME, 
			Lecturers.EMAIL,
			Lecturers.TELEPHONE,
			Lecturers.OFFICE,
			Lecturers.DEPARTMENT,
			Lecturers.SECTOR};

	@Override
	public String[] getProjection() {
		return projection;
	}

	@Override
	public Lecturer load(Cursor cursor) {
		// using magic numbers !!!
		Lecturer lecturer = new Lecturer();
		lecturer.id = cursor.getInt(0);
		lecturer.key = cursor.getInt(1);
		lecturer.dest = cursor.getInt(2);
		lecturer.thumbnail = cursor.getString(3);
		lecturer.name = cursor.getString(4);
		lecturer.email = cursor.getString(5);
		lecturer.telephone = cursor.getString(6);
		lecturer.office = cursor.getString(7);
		lecturer.department = cursor.getString(8);
		lecturer.sector = cursor.getString(9);
		return lecturer;
	}
}
