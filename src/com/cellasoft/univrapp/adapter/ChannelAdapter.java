package com.cellasoft.univrapp.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.utils.DateUtils;
import com.cellasoft.univrapp.utils.RecyclingImageView;
import com.cellasoft.univrapp.widget.ChannelView;
import com.cellasoft.univrapp.widget.OnChannelViewListener;

import java.util.List;

public class ChannelAdapter extends BaseListAdapter<Channel> {

    public static int mCornerRadius;
    public static int mMargin;

    private OnChannelViewListener channelListener;
    // private int color_university;
    private int logo_university;
    private Context context;

    public ChannelAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        logo_university = Settings.getUniversity().logo_from_resource;
    }

    @Override
    protected void populateDataForRow(ViewHolder viewHolder, Channel channel,
                                      int position) {
        if (viewHolder instanceof Holder) {
            Holder holder = (Holder) viewHolder;

            if (channel.unread > 0) {
                holder.unreadCount.setText(String.valueOf(channel.unread));
                holder.unreadCount.setVisibility(View.VISIBLE);
            } else {
                holder.unreadCount.setVisibility(View.GONE);
            }
            if (channel.updating) {
                holder.updated.setText(getContext().getResources().getString(
                        R.string.updating));
            } else {
                holder.updated.setText(getContext().getResources().getString(
                        R.string.updated)
                        + " "
                        + DateUtils.formatTimeMillis(getContext(),
                        channel.updateTime));
            }

            holder.title.setText(channel.title);
            imageLoader(viewHolder, channel.imageUrl);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        ChannelView view;
        Channel item = new Channel(items.get(position));

        switch (position) {
            case 0:
                if (convertView == null || (convertView instanceof ChannelView)) {
                    convertView = mInflater.inflate(R.layout.university_list_item, parent, false);
                    holder = new Holder();
                    holder.position = position;
                    holder.title = (TextView) convertView.findViewById(R.id.univr_name);
                    holder.thumbnail = (RecyclingImageView) convertView
                            .findViewById(R.id.univr_logo);
                    holder.title.setText(item.title);
                    Drawable drawable = context.getResources().getDrawable(
                            logo_university);
                    holder.thumbnail.setImageDrawable(drawable);
                    convertView.setTag(holder);
                }

                return convertView;
            default:
                if (convertView == null || !(convertView instanceof ChannelView)) {
                    view = (ChannelView) mInflater.inflate(resource, parent, false);
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

    class Holder extends ViewHolder {
        TextView title;
        TextView updated;
        TextView unreadCount;
    }
}
