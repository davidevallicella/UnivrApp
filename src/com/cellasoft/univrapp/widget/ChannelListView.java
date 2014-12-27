package com.cellasoft.univrapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.adapter.ChannelAdapter;
import com.cellasoft.univrapp.model.Channel;

import java.util.List;

public class ChannelListView extends BaseListView<Channel> {

    public ChannelListView(Context context) {
        this(context, null, 0);
    }

    public ChannelListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChannelListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        adapter = new ChannelAdapter(context, R.layout.channel_list_item);
        this.setAdapter(adapter);
    }

    public void setChannels(List<Channel> channels) {
        super.setItems(channels);
    }

    public void setChannelViewlistener(OnChannelViewListener channelListener) {
        ((ChannelAdapter) adapter).setChannelViewlistener(channelListener);
    }
}