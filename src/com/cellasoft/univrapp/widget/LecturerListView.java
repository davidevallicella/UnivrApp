package com.cellasoft.univrapp.widget;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.adapter.LecturerAdapter;
import com.cellasoft.univrapp.model.Lecturer;
import com.woozzu.android.widget.IndexableListView;

public class LecturerListView extends IndexableListView {
	private LecturerAdapter adapter = null;
	private Context context;

	public LecturerListView(Context context) {
		this(context, null, 0);
	}

	public LecturerListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LecturerListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init(context);
	}

	private void init(Context context) {
		setFastScrollEnabled(true);
		setDivider(getResources().getDrawable(
				android.R.drawable.divider_horizontal_bright));

		setSelector(R.drawable.list_selector_on_top);
		setDrawSelectorOnTop(true);
		invalidateViews();
	}

	public void setLecturers(ArrayList<Lecturer> lecturers) {
		adapter.setLecturers(lecturers);
	}

	public void setLecturerViewlistener(OnLecturerViewListener lecturerListener) {
		adapter.setOnLecturerViewListener(lecturerListener);
	}

	public void refresh() {
		adapter.refresh();
	}

	public void clean() {
		adapter.clear();
	}
}