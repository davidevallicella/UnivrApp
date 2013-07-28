package com.cellasoft.univrapp.widget;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.adapter.ItemAdapter;
import com.cellasoft.univrapp.adapter.ItemAdapter.OnItemRequestListener;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.utils.ActiveList;
import com.markupartist.android.widget.PullToRefreshListView;

public class ItemListView extends PullToRefreshListView {

	private ItemAdapter adapter;
	private TextView footer;

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

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footer = (TextView) inflater.inflate(R.layout.listfooter, null);

		this.setChoiceMode(CHOICE_MODE_SINGLE);
		this.setVerticalScrollBarEnabled(false);
		this.setSelector(R.drawable.list_selector_on_top);
		this.setDivider(getResources().getDrawable(
				android.R.drawable.divider_horizontal_bright));
		this.setDrawSelectorOnTop(true);
	}

	public void setItems(ActiveList<Item> items) {
		this.setSelection(1);
		adapter.setItems(items);
	}

	public void addItems(List<Item> list) {
		this.setSelection(1);
		adapter.addItems(list);
	}

	public void addItemsOnTop(List<Item> list) {
		this.setSelection(1);
		adapter.addItemsOnTop(list);
	}

	public int size() {
		return adapter.getCount();
	}

	public void refresh() {
		adapter.refresh();
	}

	public void clean() {
		adapter.clear();
		removeFooterView(footer);
	}

	public void setItemRequestListener(OnItemRequestListener listener) {
		adapter.setItemRequestListener(listener);
	}

	public void addFooterView() {
		addFooterView(footer, null, false);
	}

	public boolean removeFooterView() {
		return removeFooterView(footer);
	}
}