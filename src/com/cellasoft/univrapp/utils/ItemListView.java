package com.cellasoft.univrapp.utils;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;

import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.adapter.ItemAdapter;
import com.cellasoft.univrapp.model.Item;
import com.markupartist.android.widget.PullToRefreshListView;

public class ItemListView extends PullToRefreshListView {
	private ItemAdapter adapter;

	public ItemListView(Context context) {
		super(context);
		init(context);
	}

	public ItemListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		adapter = new ItemAdapter(context);
		this.setAdapter(adapter);

		this.setDivider(getResources().getDrawable(
				android.R.drawable.divider_horizontal_bright));
		this.setVerticalScrollBarEnabled(false);
		this.setSelector(R.drawable.list_selector_on_top);
		this.setDrawSelectorOnTop(true);
		this.invalidateViews();
	}
	
	

	public void setItems(ActiveList<Item> items) {
		this.setSelection(-1);
		adapter.setItems(items);
	}
	
	public void addItems(List<Item> list) {
		this.setSelection(-1);
		adapter.addItems(list);
	}
	
	public int size() {
		return adapter.getCount();
	}

	public void refresh() {
		this.setSelection(-1);
		adapter.notifyDataSetChanged();
	}
	
	public void clean(){
		adapter.clear();
	}
}