package com.cellasoft.univrapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.utils.ChannelView;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.utils.OnChannelViewListener;

public class ChannelAdapter extends BaseAdapter {

	private final int[] bgColors = new int[] { R.color.list_bg_1,
			R.color.list_bg_2 };
	private ArrayList<Channel> channels;
	private OnChannelViewListener channelListener;

	private Context context;

	static class ViewHolder {
		ImageButton check;
		ImageButton star;
		ImageView image;
		TextView title;
		TextView unreadCount;
	}

	public ChannelAdapter(Context context, ArrayList<Channel> channels,
			OnChannelViewListener channelListener) {
		this.context = context;
		this.channels = channels;
		this.channelListener = channelListener;
	}

	public int getCount() {
		return channels.size();
	}

	public Channel getItem(int position) {
		return channels.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Channel item = channels.get(position);
		ChannelView view = null;

		if (convertView == null) {
			view = (ChannelView) View.inflate(context, R.layout.channel_item,
					null);
			view.setChannelListener(channelListener);
			holder = new ViewHolder();
			holder.title = (TextView) view.findViewById(R.id.channel_title);
			holder.check = (ImageButton) view.findViewById(R.id.channel_chek);
			holder.star = (ImageButton) view.findViewById(R.id.channel_star);
			holder.image = (ImageView) view.findViewById(R.id.channel_image);
			view.setTag(holder);
		} else {
			view = (ChannelView) convertView;
			holder = (ViewHolder) view.getTag();
		}

		holder.title.setText(item.title);
		view.setItemViewSelected(item.isSelected);
		view.setItemViewStarred(item.starred);
		String imageUrl = item.imageUrl;
		holder.image.setTag(imageUrl);
		if (imageUrl != null) {
			try {
				Bitmap itemImage = ImageLoader.get(imageUrl);
				if (itemImage != null) {
					holder.image.setVisibility(View.VISIBLE);
					holder.image.setImageBitmap(itemImage);
				} else {
					ImageLoader.start(imageUrl, new ItemImageLoaderHandler(
							holder.image, imageUrl));
				}
			} catch (RuntimeException e) {
			}
		} else {
			holder.image.setImageResource(R.drawable.univr);
		}
		
		return view;
	}

	public void setChannels(ArrayList<Channel> channels) {
		this.channels = channels;
		notifyDataSetChanged();
	}

	public void refresh() {
		notifyDataSetChanged();
	}
}
