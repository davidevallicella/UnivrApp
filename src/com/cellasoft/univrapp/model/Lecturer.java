package com.cellasoft.univrapp.model;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Element;

import android.net.Uri;
import android.provider.BaseColumns;

import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.provider.Provider;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.HtmlParser;
import com.cellasoft.univrapp.utils.Settings;

public class Lecturer implements ActionSupport {

	public int id;
	public String name;
	public int key;
	public int dest;
	public String thumbnail;
	public String mail;
	
	private String url_page = Constants.UNIVERSITY.DOMAIN.get(Settings.getUniversity()) + "/fol/main?ent=persona&id=";

	public Lecturer() {
	}

	public Lecturer(int key, int dest, String name, String thumbnail) {
		this.key = key;
		this.dest = dest;
		this.name = name;
		this.thumbnail = thumbnail;
	}

	public Lecturer(Element element) throws ClientProtocolException,
			IOException {
		this.key = Integer.parseInt(element.attr("value"));
		this.dest = Constants.UNIVERSITY.DEST.get(Settings.getUniversity());
		this.name = element.text();
		String html = HtmlParser.get(url_page + key);
		this.thumbnail = HtmlParser.getThumbnailUrl(html);
		if (!thumbnail.isEmpty()) {
			Image image = new Image(thumbnail, Image.IMAGE_STATUS_QUEUED);
			if (!image.exist())
				image.save();
		}
		this.mail = HtmlParser.getEmail(html);
	}

	@Override
	public boolean save() {
		return ContentManager.saveLecturer(this);
	}

	@Override
	public void delete() {
	}

	@Override
	public boolean exist() {
		return ContentManager.existLecturer(this);
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
		public static final String EMAIL = "EMAIL";
		public static final String THUMBNAIL = "THUMBNAIL";
	}
}
