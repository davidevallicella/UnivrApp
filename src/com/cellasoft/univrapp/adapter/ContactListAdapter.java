package com.cellasoft.univrapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.widget.ContactItemInterface;
import com.cellasoft.univrapp.widget.ContactsSectionIndexer;
import com.cellasoft.univrapp.widget.LecturerView;
import com.cellasoft.univrapp.widget.OnLecturerViewListener;

import java.util.List;

public class ContactListAdapter extends BaseListAdapter<ContactItemInterface> {

    private boolean inSearchMode = false;

    private ContactsSectionIndexer indexer = null;
    private OnLecturerViewListener lecturerListener;

    public ContactListAdapter(Context _context, int _resource) {
        super(_context, _resource);
        setIndexer(new ContactsSectionIndexer());
    }

    public ContactListAdapter(Context _context, int _resource,
                              List<ContactItemInterface> _items) {
        super(_context, _resource, _items);
        setIndexer(new ContactsSectionIndexer(_items));
    }

    // get the section textview from row view
    // the section view will only be shown for the first item
    protected TextView getSectionTextView(View rowView) {
        return (TextView) rowView.findViewById(R.id.header);
    }

    protected LecturerView getLecturerView(View rowView) {
        return (LecturerView) rowView.findViewById(R.id.lecturerView);
    }

    protected void showSectionViewIfFirstItem(View rowView,
                                              ContactItemInterface item, int position) {
        TextView sectionTextView = getSectionTextView(rowView);

        // if in search mode then dun show the section header
        if (inSearchMode) {
            sectionTextView.setVisibility(View.GONE);
        } else {
            // if first item then show the header

            if (indexer.isFirstItemInSection(position)) {
                sectionTextView.setText(indexer.getSectionTitle(item.getItemForIndex()));
                sectionTextView.setVisibility(View.VISIBLE);

            } else {
                sectionTextView.setVisibility(View.GONE);
            }
        }
    }

    // do all the data population for the row here
    // subclass overwrite this to draw more items
    @Override
    protected void populateDataForRow(ViewHolder viewHolder,
                                      ContactItemInterface item, int position) {
        if (viewHolder instanceof Holder) {
            Holder holder = (Holder) viewHolder;
            holder.name.setText(item.getItemForIndex());
            holder.subscribed.setVisibility(View.GONE);
        }
        viewHolder.position = position;
    }

    // this should be override by subclass if necessary
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Lecturer item = (Lecturer) getItem(position);
        LecturerView view;
        Holder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(resource, parent, false);
            view = getLecturerView(convertView);
            view.setLecturerListener(lecturerListener);
            holder = new Holder();
            holder.name = (TextView) view.findViewById(R.id.lecturer_name);
            holder.thumbnail = (ImageView) view.findViewById(R.id.lecturer_image);
            holder.subscribed = (LinearLayout) view.findViewById(R.id.lecturer_subscribed);

            convertView.setTag(holder);
        } else {
            view = getLecturerView(convertView);
            holder = (Holder) convertView.getTag();
        }

        view.setItemViewSelected(item.isSelected);
        // for the very first section item, we will draw a section on top
        showSectionViewIfFirstItem(convertView, item, position);
        // set row items here
        populateDataForRow(holder, item, position);

        return convertView;
    }

    public boolean isInSearchMode() {
        return inSearchMode;
    }

    public void setInSearchMode(boolean inSearchMode) {
        this.inSearchMode = inSearchMode;
    }

    public ContactsSectionIndexer getIndexer() {
        return indexer;
    }

    public void setIndexer(ContactsSectionIndexer indexer) {
        this.indexer = indexer;
    }

    public void setOnLecturerViewListener(
            OnLecturerViewListener lecturerListener) {
        this.lecturerListener = lecturerListener;
    }

    class Holder extends ViewHolder {
        LinearLayout subscribed;
        TextView name;
        TextView email;
        TextView header;
    }
}
