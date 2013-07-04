package com.cellasoft.univrapp.model;

import java.util.ArrayList;

import android.net.Uri;
import android.provider.BaseColumns;

import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.provider.Provider;

public class Lecturer implements Comparable<Lecturer>, ActionSupport {

	public int id;
	public int key;
	public int dest;
	public String name;

	public String department;
	public String sector;
	public String office;

	public String telephone;
	public String email;
	public String thumbnail;

	public boolean isSelected;

	public Lecturer() {
		this.id = 0;
	}

	public Lecturer(String name) {
		this();
		this.name = name;
	}

	public Lecturer(int key, int dest, String name, String department,
			String sector, String office, String telephone, String email,
			String thumbnail) {
		super();
		this.key = key;
		this.dest = dest;
		this.name = name;
		this.department = department;
		this.sector = sector;
		this.office = office;
		this.telephone = telephone;
		this.email = email;
		this.thumbnail = thumbnail;
	}

	@Override
	public boolean save() {
		if (thumbnail != null && thumbnail.length() != 0) {
			Image image = new Image(thumbnail, Image.IMAGE_STATUS_QUEUED);
			if (!image.exist())
				image.save();
		}
		return ContentManager.saveLecturer(this);
	}

	@Override
	public void delete() {
		ContentManager.deleteLecturer(this);
	}

	@Override
	public boolean exist() {
		return ContentManager.existLecturer(this);
	}

	public static Lecturer findById(int id) {
		return ContentManager.loadLecturer(id,
				ContentManager.FULL_LECTURER_LOADER);
	}

	public static ArrayList<Lecturer> loadFullLecturers() {
		return ContentManager
				.loadAllLecturers(ContentManager.FULL_LECTURER_LOADER);
	}

	public static ArrayList<Lecturer> loadLightweightLecturer() {
		return ContentManager
				.loadAllLecturers(ContentManager.LIGHTWEIGHT_LECTURER_LOADER);
	}

	@Override
	public String toString() {
		return String
				.format("Lecturer [id=%s, key=%s, dest=%s, name=%s, department=%s, sector=%s, office=%s, telephone=%s e-mail=%s, thumbnail=%s]",
						id, key, dest, name, department, sector, office,
						telephone, email, thumbnail);
	}

	public static final class Lecturers implements BaseColumns {		
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ Provider.AUTHORITY + "/lecturers");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cellasoft.univrapp.provider.lecturers";

		public static final String ID = "ID";
		public static final String KEY = "KEY";
		public static final String DEST = "DEST";
		public static final String NAME = "NAME";
		public static final String DEPARTMENT = "DEPARTMENT";
		public static final String SECTOR = "SECTOR";
		public static final String OFFICE = "OFFICE";
		public static final String TELEPHONE = "TELEPHONE";
		public static final String EMAIL = "EMAIL";
		public static final String THUMBNAIL = "THUMBNAIL";
	}

	@Override
	public int compareTo(Lecturer another) {
		if (this.name.charAt(0) == another.name.charAt(0))
			return 0;
		else if (this.name.charAt(0) > another.name.charAt(0))
			return 1;
		return -1;
	}
}
