package com.cellasoft.univrapp.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.utils.ActiveList;
import com.cellasoft.univrapp.utils.DateUtils;

public class ItemAdapter extends BaseAdapter {

	private static final int REFRESH_MESSAGE = 1;
	private final int[] bgColors = new int[] { R.color.aliceBlue,
			android.R.color.white };
	private ActiveList<Item> items = new ActiveList<Item>();
	private int lastRequestPosition = -1;
	private Context context;

	static class ViewHolder {
		TextView title;
		TextView description;
		TextView date;
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == REFRESH_MESSAGE) {
				ItemAdapter.this.notifyDataSetChanged();
			}
		}
	};

	private OnItemRequestListener itemRequestListener;

	private ActiveList.ActiveListListener<Item> activeListListener = new ActiveList.ActiveListListener<Item>() {
		@Override
		public void onAdd(Item item) {
			refresh();
		}

		@Override
		public void onInsert(final int location, final Item item) {
			refresh();
		}

		@Override
		public void onClear() {
			refresh();
		}
	};

	public ItemAdapter(Context context) {
		this.context = context;
	}

	public void refresh() {
		handler.sendEmptyMessage(REFRESH_MESSAGE);
	}

	public synchronized void setItems(ActiveList<Item> items) {
		lastRequestPosition = -1;
		if (this.items != null) {
			this.items.removeListener(activeListListener);
		}
		this.items = items;
		this.items.addListener(activeListListener);
		this.notifyDataSetInvalidated();
	}

	public synchronized void addItems(List<Item> items) {
		if (this.items != null) {
			this.items.addAll(items);
		} else {
			this.items = new ActiveList<Item>();
			this.items.addAll(items);
			this.items.addListener(activeListListener);
		}
		this.notifyDataSetChanged();
	}

	public synchronized void addItemsOnTop(List<Item> items) {
		if (this.items != null) {
			this.items.addAll(0, items);
		} else {
			this.items = new ActiveList<Item>();
			this.items.addAll(items);
			this.items.addListener(activeListListener);
		}
		this.notifyDataSetChanged();
	}

	public synchronized void clear() {
		items.clear();
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Item item = items.get(position);

		if (convertView == null) {
			convertView = View.inflate(context, R.layout.item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.item_title);
			holder.description = (TextView) convertView
					.findViewById(R.id.item_description);
			holder.date = (TextView) convertView.findViewById(R.id.item_date);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		holder.title.setText(item.getTitle());

		String description = item.getDescription().replace("Pubblicato da:",
				"<b>Pubblicato da:</b>");
		holder.description.setText(Html.fromHtml(description));
		holder.date.setText(DateUtils.formatDate(item.pubDate));

		if (!item.isRead()) {
			convertView.setBackgroundResource(bgColors[0]);
			holder.title.setTypeface(Typeface.DEFAULT_BOLD, 0);
		} else {
			convertView.setBackgroundResource(bgColors[1]);
			holder.title.setTypeface(Typeface.DEFAULT);
		}

		// request more items if we reach the to 2/3 items
		int requestPosition = (2 * getCount() / 3);

		if (((position == requestPosition) || (position == getCount() - 1))
				&& lastRequestPosition != requestPosition
				&& itemRequestListener != null) {
			lastRequestPosition = requestPosition;
			itemRequestListener.onRequest((Item) getItem(getCount() - 1));
		}

		return convertView;
	}

	public void setItemRequestListener(OnItemRequestListener listener) {
		this.itemRequestListener = listener;
	}

	public interface OnItemRequestListener {
		void onRequest(Item lastItem);
	}

}
