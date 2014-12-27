package com.cellasoft.univrapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.adapter.BaseListAdapter;
import com.cellasoft.univrapp.utils.Lists;
import com.markupartist.android.widget.PullToRefreshListView;

import java.util.List;

public class BasePullListView<T> extends PullToRefreshListView {

    protected BaseListAdapter<T> adapter;

    public BasePullListView(Context context) {
        super(context);
        init(context);
    }

    public BasePullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BasePullListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context) {
        setDivider(getResources().getDrawable(
                android.R.drawable.divider_horizontal_bright));
        setSelector(R.drawable.list_selector_on_top);
        setDrawSelectorOnTop(true);
    }

    public void refresh() {
        adapter.refresh();
    }

    public int size() {
        return adapter.getCount();
    }

    public boolean isEmpty() {
        return adapter.getCount() == 0;
    }

    public void clean() {
        adapter.clear();
    }

    public void addItems(List<T> items) {
        if (items != null) {
            adapter.addItems(items);
        }
    }

    public List<T> getItems() {
        return adapter.getItems();
    }

    public void setItems(List<T> items) {
        if (items == null) {
            items = Lists.newArrayList();
        }
        adapter.setItems(items);
        this.setSelection(1);
    }

    @Override
    public T getItemAtPosition(int position) {
        return adapter.getItem(position - 1); // -1 because add header
        // (PullToRefresh button)
    }
}
