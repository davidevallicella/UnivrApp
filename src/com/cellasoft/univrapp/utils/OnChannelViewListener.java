package com.cellasoft.univrapp.utils;

public interface OnChannelViewListener {
    /**
     * Called when the selection state changed
     * 
     * @param view The {@link ChannelView} whose selection state
     *            changed
     * @param selected The new selection state
     */
    void onSelected(ChannelView view, boolean selected);

    /**
     * Called when the selection starred changed
     * 
     * @param view The {@link ChannelView} whose starred state
     *            changed
     * @param selected The new starred state
     */
    void onStarred(ChannelView view, boolean starred);
}