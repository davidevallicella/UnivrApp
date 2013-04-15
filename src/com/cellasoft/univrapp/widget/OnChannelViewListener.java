package com.cellasoft.univrapp.widget;


public interface OnChannelViewListener {

    void onSelected(ChannelView view, boolean selected);

    void onStarred(ChannelView view, boolean starred);
}