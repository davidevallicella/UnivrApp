package com.cellasoft.univrapp.widget;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.adapter.ChannelAdapter;
import com.cellasoft.univrapp.model.Channel;

public class ChannelListView extends ListView {
	private ChannelAdapter adapter = null;

	public ChannelListView(Context context) {
		this(context, null, 0);
	}

	public ChannelListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ChannelListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		adapter = new ChannelAdapter(context);
		this.setAdapter(adapter);
		
//		this.setBackgroundColor(getResources().getColor(R.color.itemBackground));
//		this.setCacheColorHint(getResources().getColor(R.color.itemBackground));
		this.setDivider(getResources().getDrawable(
				android.R.drawable.divider_horizontal_bright));
		
		setSelector(R.drawable.list_selector_on_top);
		setDrawSelectorOnTop(true);
		invalidateViews();
	}

	public void setChannels(ArrayList<Channel> channels) {
		adapter.setChannels(channels);
	}
	
	public void setChannelViewlistener(OnChannelViewListener channelListener) {
		adapter.setChannelViewlistener(channelListener);
	}

	public void refresh() {
		adapter.refresh();
	}

	public void clean() {
		adapter.clear();
	}
}