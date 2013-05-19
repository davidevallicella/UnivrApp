package com.cellasoft.univrapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.utils.DateUtils;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.RecyclingImageView;
import com.cellasoft.univrapp.widget.ChannelView;
import com.cellasoft.univrapp.widget.OnChannelViewListener;

public class ChannelAdapter extends BaseAdapter {

	private static final int REFRESH_MESSAGE = 1;
	private static final int CORNER_RADIUS = 3; // dips
	private static final int MARGIN = 1; // dips

	public static int mCornerRadius;
	public static int mMargin;

	public ArrayList<Channel> channels = new ArrayList<Channel>();
	private OnChannelViewListener channelListener;

	private Context context;

	static class ViewHolder {
		int position;
		ImageButton check;
		ImageButton star;
		RecyclingImageView thumbnail;
		TextView title;
		TextView updated;
		TextView unreadCount;
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == REFRESH_MESSAGE) {
				ChannelAdapter.this.notifyDataSetChanged();
			}
		}
	};

	public ChannelAdapter(Context context) {
		this.context = context;
		ImageFetcher.inizialize(context);

		final float density = context.getResources().getDisplayMetrics().density;
		mCornerRadius = (int) (CORNER_RADIUS * density + 0.5f);
		mMargin = (int) (MARGIN * density + 0.5f);
	}

	public ChannelAdapter(Context context, ArrayList<Channel> channels) {
		this(context);
		this.channels = channels;
	}

	@Override
	public int getCount() {
		return channels.size();
	}

	@Override
	public Channel getItem(int position) {
		return channels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		ChannelView view;
		Channel item = channels.get(position);

		switch (position) {
		case 0:
			RelativeLayout layout;
			if (convertView == null || (convertView instanceof ChannelView)) {
				layout = (RelativeLayout) View.inflate(context,
						R.layout.university_item, null);
				holder = new ViewHolder();
				holder.position = position;
				holder.title = (TextView) layout.findViewById(R.id.univr_name);
				holder.thumbnail = (RecyclingImageView) layout
						.findViewById(R.id.univr_logo);
				holder.title.setText(item.title);
				holder.position = position;
				holder.thumbnail
						.setImageResource(Settings.getUniversity().logo_from_resource);
				layout.setTag(holder);
			} else {
				layout = (RelativeLayout) convertView;
				holder = (ViewHolder) layout.getTag();
			}

			layout.setBackgroundResource(Settings.getUniversity().color_from_resource);

			return layout;
		default:
			if (convertView == null || !(convertView instanceof ChannelView)) {
				view = (ChannelView) View.inflate(context,
						R.layout.channel_item, null);
				view.setChannelListener(channelListener);
				holder = new ViewHolder();
				holder.position = position;
				holder.title = (TextView) view.findViewById(R.id.channel_title);
				holder.unreadCount = (TextView) view
						.findViewById(R.id.channel_unreadCount);
				holder.updated = (TextView) view
						.findViewById(R.id.channel_updated);
				holder.check = (ImageButton) view
						.findViewById(R.id.channel_chek);
				holder.star = (ImageButton) view
						.findViewById(R.id.channel_star);
				holder.thumbnail = (RecyclingImageView) view
						.findViewById(R.id.channel_image);
				view.setTag(holder);
			} else {
				view = (ChannelView) convertView;
				holder = (ViewHolder) view.getTag();
			}
		}

		view.setItemViewSelected(item.isSelected);
		view.setItemViewStarred(item.starred);
		int unreadItems = item.countUnreadItems();
		if (unreadItems > 0) {
			holder.unreadCount.setText(String.valueOf(unreadItems));
			holder.unreadCount.setVisibility(View.VISIBLE);
		} else {
			holder.unreadCount.setVisibility(View.GONE);
		}
		if (item.updating) {
			holder.updated.setText(context.getResources().getString(
					R.string.updating));
		} else {
			holder.updated.setText(context.getResources().getString(
					R.string.updated)
					+ " " + DateUtils.formatTimeMillis(item.updateTime));
		}

		holder.title.setText(item.title);
		imageLoader(holder, item.imageUrl);

		return view;
	}

	public void setChannels(ArrayList<Channel> channels) {
		this.channels = channels;
		this.notifyDataSetInvalidated();
	}

	public void setChannelViewlistener(OnChannelViewListener channelListener) {
		this.channelListener = channelListener;
	}

	public void refresh() {
		handler.sendEmptyMessage(REFRESH_MESSAGE);
	}

	private void imageLoader(ViewHolder holder, String imageUrl) {
		if (imageUrl != null && imageUrl.length() > 0) {
			if (!imageUrl.equals((String) holder.thumbnail.getTag())) {
				holder.thumbnail.setTag(imageUrl);
				ImageFetcher.getInstance()
						.loadImage(imageUrl, holder.thumbnail);
			}
		} else if (holder.thumbnail.getTag() != null) {
			holder.thumbnail.setTag(null);
			// default image
			holder.thumbnail.setImageResource(R.drawable.thumb);
		}
	}

	public void clear() {
		channels.clear();
		this.notifyDataSetChanged();
	}

}
