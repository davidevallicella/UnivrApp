package com.cellasoft.univrapp.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.SectionIndexer;

import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.widget.OnLecturerViewListener;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;

public class LecturerSectionAdapter extends SimpleSectionAdapter<Lecturer>
		implements SectionIndexer {
	private static final int REFRESH_MESSAGE = 1;

	private HashMap<String, Integer> alphaIndexer;
	private String[] sections;
	private ArrayList<Lecturer> lecturers;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == REFRESH_MESSAGE) {
				LecturerSectionAdapter.this.notifyDataSetChanged();
			}
		}
	};

	static Sectionizer<Lecturer> alphabetSectionizer = new Sectionizer<Lecturer>() {

		@Override
		public String getSectionTitleForItem(Lecturer lecturer) {
			return lecturer.name.substring(0, 1);
		}
	};

	public LecturerSectionAdapter(Context context,
			ArrayList<Lecturer> lecturers,
			OnLecturerViewListener lecturerListener, int sectionHeaderLayoutId,
			int sectionTitleTextViewId) {
		super(context,
				new LecturerAdapter(context, lecturers, lecturerListener),
				sectionHeaderLayoutId, sectionTitleTextViewId,
				alphabetSectionizer);
		this.lecturers = lecturers;
		initIndex();
	}

	private void initIndex() {
		alphaIndexer = new HashMap<String, Integer>();
		// in this hashmap we will store here the positions for
		// the sections
		for (int i = lecturers.size() - 1; i >= 0; i--) {
			String element = lecturers.get(i).name;

			alphaIndexer.put(element.substring(0, 1), i);

			// We store the first letter of the word, and its index.
			// The Hashmap will replace the value for identical keys are putted
			// in
		}

		Set<String> keys = alphaIndexer.keySet(); // set of letters ...sets
		// cannot be sorted...

		Iterator<String> it = keys.iterator();
		ArrayList<String> keyList = new ArrayList<String>(); // list can be
		// sorted

		while (it.hasNext()) {
			String key = it.next();
			keyList.add(key);
		}

		Collections.sort(keyList);

		sections = new String[keyList.size()]; // simple conversion to an
		// array of object
		keyList.toArray(sections);
	}

	@Override
	public int getPositionForSection(int section) {
		// If there is no item for current section, previous section will be
		// selected
		String letter = sections[section];

		return alphaIndexer.get(letter) + section;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		return sections;
	}

	public void refresh() {
		handler.sendEmptyMessage(REFRESH_MESSAGE);
	}

}