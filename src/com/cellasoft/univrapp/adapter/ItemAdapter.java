package com.cellasoft.univrapp.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.model.RSSItem;
import com.cellasoft.univrapp.utils.ActiveList;


public class ItemAdapter extends BaseAdapter {

	private static final int REFRESH_MESSAGE = 1;
	private final int[] bgColors = new int[] { R.color.list_bg_1,
			R.color.list_bg_2 };
	private ActiveList<RSSItem> items = new ActiveList<RSSItem>();
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

	private ActiveList.ActiveListListener<RSSItem> activeListListener = new ActiveList.ActiveListListener<RSSItem>() {
		@Override
		public void onAdd(RSSItem item) {
			refresh();
		}

		@Override
		public void onInsert(final int location, final RSSItem item) {
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

	private void refresh() {
		handler.sendEmptyMessage(REFRESH_MESSAGE);
	}

	public synchronized void setItems(ActiveList<RSSItem> items) {
		// lastRequestPosition = -1;
		if (this.items != null) {
			this.items.removeListener(activeListListener);
		}
		this.items = items;
		this.items.addListener(activeListListener);
		this.notifyDataSetInvalidated();
	}

	public synchronized void addItems(List<RSSItem> items) {
		if (this.items != null) {
			this.items.addAll(items);
		} else {
			this.items = new ActiveList<RSSItem>();
			this.items.addAll(items);
			this.items.addListener(activeListListener);
		}
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
		RSSItem item = items.get(position);

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
		holder.description.setText(item.getDescription());
		holder.date.setText(item.getDate());

		int colorPosition = position % bgColors.length;
		convertView.setBackgroundResource(bgColors[colorPosition]);

		return convertView;
	}
}
