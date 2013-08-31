package com.cellasoft.univrapp.widget;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.adapter.ChannelAdapter;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.utils.ImageFetcher;

public class ChannelListView extends BaseListView<Channel> implements
		OnScrollListener {

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

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// do nothing
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Pause fetcher to ensure smoother scrolling when flinging
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
			ImageFetcher.getInstance(getContext()).setPauseWork(true);
		} else {
			ImageFetcher.getInstance(getContext()).setPauseWork(false);
		}
	}
}