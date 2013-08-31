package com.cellasoft.univrapp.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.utils.DateUtils;
import com.cellasoft.univrapp.utils.RecyclingImageView;
import com.cellasoft.univrapp.widget.ChannelView;
import com.cellasoft.univrapp.widget.OnChannelViewListener;

public class ChannelAdapter extends BaseListAdapter<Channel> {

	public static int mCornerRadius;
	public static int mMargin;

	private OnChannelViewListener channelListener;
	private int color_university;
	private int logo_university;

	class Holder extends ViewHolder {
		TextView title;
		TextView updated;
		TextView unreadCount;
	}

	public ChannelAdapter(Context context, int resource) {
		super(context, resource);
		color_university = Settings.getUniversity().color_from_resource;
		logo_university = Settings.getUniversity().logo_from_resource;
	}

	@Override
	protected void populateDataForRow(ViewHolder viewHolder, Channel item,
			int position) {
		if (viewHolder instanceof Holder) {
			Holder holder = (Holder) viewHolder;

			int unreadItems = item.countUnreadItems();
			if (unreadItems > 0) {
				holder.unreadCount.setText(String.valueOf(unreadItems));
				holder.unreadCount.setVisibility(View.VISIBLE);
			} else {
				holder.unreadCount.setVisibility(View.GONE);
			}
			if (item.updating) {
				holder.updated.setText(getContext().getResources().getString(
						R.string.updating));
			} else {
				holder.updated.setText(getContext().getResources().getString(
						R.string.updated)
						+ " "
						+ DateUtils.formatTimeMillis(getContext(),
								item.updateTime));
			}

			holder.title.setText(item.title);
		}
		imageLoader(viewHolder, item.imageUrl);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder;
		ChannelView view;
		Channel item = new Channel(items.get(position));

		switch (position) {
		case 0:
			RelativeLayout layout;
			if (convertView == null || (convertView instanceof ChannelView)) {
				layout = (RelativeLayout) View.inflate(getContext(),
						R.layout.university_list_item, null);
				holder = new Holder();
				holder.position = position;
				holder.title = (TextView) layout.findViewById(R.id.univr_name);
				holder.thumbnail = (RecyclingImageView) layout
						.findViewById(R.id.univr_logo);
				holder.title.setText(item.title);
				holder.thumbnail.setImageResource(logo_university);
				layout.setTag(holder);
				layout.setBackgroundResource(color_university);
			} else {
				layout = (RelativeLayout) convertView;
			}

			return layout;
		default:
			if (convertView == null || !(convertView instanceof ChannelView)) {
				view = (ChannelView) View.inflate(getContext(), resource, null);
				view.setChannelListener(channelListener);
				holder = new Holder();
				holder.position = position;
				holder.title = (TextView) view.findViewById(R.id.channel_title);
				holder.unreadCount = (TextView) view
						.findViewById(R.id.channel_unreadCount);
				holder.updated = (TextView) view
						.findViewById(R.id.channel_updated);
				holder.thumbnail = (RecyclingImageView) view
						.findViewById(R.id.channel_image);
				view.setTag(holder);
			} else {
				view = (ChannelView) convertView;
				holder = (Holder) view.getTag();
			}
		}

		view.setItemViewSelected(item.isSelected);
		view.setItemViewStarred(item.starred);
		populateDataForRow(holder, item, position);

		return view;
	}

	public void setChannels(List<Channel> channels) {
		super.setItems(channels);
	}

	public void addChannels(List<Channel> channels) {
		super.addItems(channels);
	}

	public void setChannelViewlistener(OnChannelViewListener channelListener) {
		this.channelListener = channelListener;
	}
}
