package com.cellasoft.univrapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.utils.ActiveList;
import com.cellasoft.univrapp.utils.DateUtils;

import java.util.List;

public class ItemAdapter extends BaseListAdapter<Item> {

    private final int[] bgColors = new int[]{R.color.aliceBlue,
            android.R.color.white};

    private int lastRequestPosition = -1;
    private OnItemRequestListener itemRequestListener;

    public ItemAdapter(Context context, int resources) {
        super(context, resources);
        items = new ActiveList<Item>();
    }

    public ItemAdapter(Context context, int resource, ActiveList<Item> items) {
        super(context, resource, items);
    }

    @Override
    public synchronized void setItems(List<Item> items) {
        lastRequestPosition = -1;
        if (this.items != null) {
            ((ActiveList<Item>) this.items).removeListener(activeListListener);
        }
        this.items = items;
        ((ActiveList<Item>) this.items).addListener(activeListListener);
        this.notifyDataSetInvalidated();
    }

    @Override
    public synchronized void addItems(List<Item> items) {
        if (this.items == null) {
            this.items = new ActiveList<Item>();
            ((ActiveList<Item>) this.items).addListener(activeListListener);
        }
        this.items.addAll(items);
    }

    protected void setItemViewIfRead(View parentView, ViewHolder viewHolder,
                                     Item item) {
        if (viewHolder instanceof Holder) {
            if (!item.isRead()) {
                parentView.setBackgroundResource(bgColors[0]);
                ((Holder) viewHolder).title.setTypeface(Typeface.DEFAULT_BOLD,
                        0);
            } else {
                parentView.setBackgroundResource(bgColors[1]);
                ((Holder) viewHolder).title.setTypeface(Typeface.DEFAULT);
            }
        }
    }

    @Override
    protected void populateDataForRow(ViewHolder viewHolder, Item item,
                                      int position) {
        if (viewHolder instanceof Holder) {
            Holder holder = (Holder) viewHolder;
            holder.title.setText(item.title);

            String description = item.description.replace("Pubblicato da:","<b>Pubblicato da:</b>");
            holder.description.setText(Html.fromHtml(description));
            holder.date.setText(DateUtils.formatDate(item.pubDate));
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        Item item = items.get(position);

        if (convertView == null) {
            convertView = mInflater.inflate(resource, parent, false);
            holder = new Holder();
            holder.title = (TextView) convertView.findViewById(R.id.item_title);
            holder.description = (TextView) convertView.findViewById(R.id.item_description);
            holder.date = (TextView) convertView.findViewById(R.id.item_date);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        setItemViewIfRead(convertView, holder, item);

        populateDataForRow(holder, item, position);

        // request more items if we reach the to 2/3 items
        int requestPosition = (2 * getCount() / 3);

        if (((position == requestPosition) || (position == getCount() - 1))
                && lastRequestPosition != requestPosition
                && itemRequestListener != null) {
            lastRequestPosition = requestPosition;
            itemRequestListener.onRequest(getItem(getCount() - 1));
        }

        return convertView;
    }

    public void setItemRequestListener(OnItemRequestListener listener) {
        this.itemRequestListener = listener;
    }

    public interface OnItemRequestListener {
        void onRequest(Item lastItem);
    }

    class Holder extends ViewHolder {
        TextView title;
        TextView description;
        TextView date;
    }

}
