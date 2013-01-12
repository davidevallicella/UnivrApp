package com.cellasoft.univrapp.loader;

import android.database.Cursor;

import com.cellasoft.univrapp.model.Lecturer;

public interface LecturerLoader {
	String[] getProjection();

	Lecturer load(Cursor cursor);
}