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

public class ItemListView extends BasePullListView<Item> {

	private TextView footer;

	public ItemListView(Context context) {
		super(context);
	}

	public ItemListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void init(Context context) {
		super.init(context);
		adapter = new ItemAdapter(context, R.layout.item_list_item);
		this.setAdapter(adapter);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footer = (TextView) inflater.inflate(R.layout.listfooter, null);
	}

	public void addItemsOnTop(List<Item> items) {
		adapter.addItemsOnTop(items);
		this.setSelection(1);
	}

	@Override
	public void clean() {
		super.clean();
		removeFooterView(footer);
	}

	public void setItemRequestListener(OnItemRequestListener listener) {
		if (adapter instanceof ItemAdapter) {
			((ItemAdapter) adapter).setItemRequestListener(listener);
		}
	}

	public void addFooterView() {
		addFooterView(footer, null, false);
	}

	public boolean removeFooterView() {
		return removeFooterView(footer);
	}
}