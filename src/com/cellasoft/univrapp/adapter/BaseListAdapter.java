package com.cellasoft.univrapp.adapter;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.utils.ActiveList;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.Lists;

public abstract class BaseListAdapter<T> extends ArrayAdapter<T> {

	private static final int REFRESH_MESSAGE = 1;
	protected int resource; // store the resource layout id for 1 row
	protected List<T> items;

	class ViewHolder {
		int position;
		ImageView thumbnail;

		public boolean isSameView(String tag) {
			if (tag == null) {
				return thumbnail.getTag() == null;
			}
			return tag.equals((String) thumbnail.getTag());
		}
	}

	static class IncomingHandler extends Handler {
		private final WeakReference<BaseListAdapter<?>> mAdapter;

		IncomingHandler(BaseListAdapter<?> adapter) {
			this.mAdapter = new WeakReference<BaseListAdapter<?>>(adapter);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			BaseListAdapter<?> adapter = mAdapter.get();
			if (adapter != null && msg.what == REFRESH_MESSAGE) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	protected IncomingHandler handler = new IncomingHandler(this);

	protected ActiveList.ActiveListListener<Item> activeListListener = new ActiveList.ActiveListListener<Item>() {
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

		@Override
		public void onAddAll(Collection<? extends Item> items) {
			refresh();
		}
	};

	public BaseListAdapter(Context context, int resource) {
		super(context, resource);
		this.resource = resource;
		this.items = Lists.newArrayList();
	}

	public BaseListAdapter(Context context, int resource, List<T> items) {
		super(context, resource, items);
		this.resource = resource;
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public T getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	protected abstract void populateDataForRow(ViewHolder holder, T item,
			int position);

	@Override
	public abstract View getView(int position, View convertView,
			ViewGroup parent);

	protected void imageLoader(ViewHolder holder, String imageUrl) {
		if (URLUtil.isValidUrl(imageUrl)) {
			if (!holder.isSameView(imageUrl)) {
				ImageFetcher.getInstance(getContext()).loadThumbnailImage(
						imageUrl, holder.thumbnail, R.drawable.user);
			}
		} else if (!holder.isSameView(null)) {
			ImageFetcher.getInstance(getContext()).loadThumbnailImage(null,
					holder.thumbnail, R.drawable.user);
		}
	}

	public synchronized void setItems(List<T> items) {
		this.items.clear();
		this.items = items;
		this.notifyDataSetInvalidated();
	}

	public synchronized void addItems(List<T> items) {
		if (this.items != null) {
			this.items.addAll(items);
		} else {
			this.items = Lists.newArrayList();
			this.items.addAll(items);
		}
		refresh();
	}

	public synchronized void addItemsOnTop(List<T> items) {
		if (this.items != null) {
			this.items.addAll(0, items);
			refresh();
		} else {
			addItems(items);
		}
	}

	public synchronized void refresh() {
		handler.sendEmptyMessage(REFRESH_MESSAGE);
	}

	public synchronized List<T> getItems() {
		return items;
	}

	@Override
	public synchronized void clear() {
		items.clear();
		this.notifyDataSetInvalidated();
	}

}
