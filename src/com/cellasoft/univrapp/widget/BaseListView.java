package com.cellasoft.univrapp.widget;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.adapter.BaseListAdapter;
import com.cellasoft.univrapp.utils.Lists;

public class BaseListView<T> extends ListView {

	protected BaseListAdapter<T> adapter;

	public BaseListView(Context context) {
		super(context);
		init(context);
	}

	public BaseListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public BaseListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	protected void init(Context context) {
		setDivider(getResources().getDrawable(
				android.R.drawable.divider_horizontal_bright));
		setSelector(R.drawable.list_selector_on_top);
		setDrawSelectorOnTop(true);
	}

	public void refresh() {
		adapter.refresh();
	}

	public int size() {
		return adapter.getCount();
	}

	public boolean isEmpty() {
		return adapter.getCount() == 0;
	}

	public void clean() {
		adapter.clear();
	}

	public void setItems(List<T> items) {
		if (items == null) {
			items = Lists.newArrayList();
		}
		adapter.setItems(items);
		this.setSelection(1);
	}

	public void addItems(List<T> items) {
		adapter.addAll(items);
	}
}
