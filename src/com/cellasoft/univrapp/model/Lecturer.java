package com.cellasoft.univrapp.model;

import java.util.List;

import android.net.Uri;
import android.provider.BaseColumns;

import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.provider.Provider;
import com.cellasoft.univrapp.widget.ContactItemInterface;

public class Lecturer implements Comparable<Lecturer>, ActionSupport,
		ContactItemInterface {

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
	public boolean isSubscribed;

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

	public Lecturer(ContactItemInterface item) {
		if (item instanceof Lecturer) {
			Lecturer lecturer = (Lecturer) item;
			this.id = lecturer.id;
			this.key = lecturer.key;
			this.dest = lecturer.dest;
			this.name = lecturer.name;
			this.department = lecturer.department;
			this.sector = lecturer.sector;
			this.office = lecturer.office;
			this.telephone = lecturer.telephone;
			this.email = lecturer.email;
			this.thumbnail = lecturer.thumbnail;
			this.isSelected = lecturer.isSelected;
			this.isSubscribed = lecturer.isSubscribed;
		}
	}

	@Override
	public String getItemForIndex() {
		return name;
	}

	@Override
	public boolean save() {
		boolean success = ContentManager.saveLecturer(this);
		if (success && thumbnail != null) {
			new Image(thumbnail, Image.IMAGE_STATUS_QUEUED).save();
		}
		return success;
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

	public static List<ContactItemInterface> loadFullLecturers() {
		return ContentManager
				.loadAllLecturers(ContentManager.FULL_LECTURER_LOADER);
	}

	public static List<ContactItemInterface> loadLightweightLecturer() {
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

	@Override
	public int compareTo(Lecturer another) {
		if (this.name.charAt(0) == another.name.charAt(0))
			return 0;
		else if (this.name.charAt(0) > another.name.charAt(0))
			return 1;
		return -1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Lecturer other = (Lecturer) obj;

		if (id != 0 && other.id != 0) {
			return id == other.id;
		} else if (key != other.key)
			return false;
		if (dest != other.dest)
			return false;
		return true;
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

}
