package com.cellasoft.univrapp.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.adapter.LecturerContactAdapter;

public class LecturerListView extends ContactListView {

	public LecturerListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void init(Context context) {
		super.init(context);
		adapter = new LecturerContactAdapter(context,
				R.layout.lecturer_list_item);
		this.setAdapter(adapter);
		setFastScrollEnabled(true);
	}

	public void createScroller() {
		mScroller = new IndexScroller(getContext(), this);

		mScroller.setAutoHide(autoHide);

		// style 1
		// 0 mScroller.setShowIndexContainer(false);
		// mScroller.setIndexPaintColor(Color.argb(255, 49, 64, 91));

		// style 2
		mScroller.setShowIndexContainer(true);
		mScroller.setIndexPaintColor(Color.WHITE);

		if (autoHide)
			mScroller.hide();
		else
			mScroller.show();

	}

	public void refreshIndexer() {
		((LecturerContactAdapter) adapter).refreshIndexer();
		mScroller.setAdapter(adapter);
	}

	@Override
	public void setItems(List<ContactItemInterface> items) {
		super.setItems(items);
		refreshIndexer();
	}

	@Override
	public void addItems(List<ContactItemInterface> items) {
		super.addItems(items);
		refreshIndexer();
	}

	public void setLecturerViewlistener(OnLecturerViewListener lecturerListener) {
		((LecturerContactAdapter) adapter)
				.setOnLecturerViewListener(lecturerListener);
	}

	public void setInSearchMode(boolean inSearchMode) {
		super.setInSearchMode(inSearchMode);
		((LecturerContactAdapter) adapter).setInSearchMode(inSearchMode);
	}
}
