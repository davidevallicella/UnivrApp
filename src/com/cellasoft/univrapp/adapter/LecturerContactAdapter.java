package com.cellasoft.univrapp.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.widget.ContactItemInterface;

public class LecturerContactAdapter extends ContactListAdapter {

	public LecturerContactAdapter(Context context, int resource) {
		super(context, resource);
	}

	public LecturerContactAdapter(Context context, int resource,
			List<ContactItemInterface> items) {
		super(context, resource, items);
	}

	@Override
	protected void populateDataForRow(ViewHolder viewHolder,
			ContactItemInterface item, int position) {
		super.populateDataForRow(viewHolder, item, position);

		if (item instanceof Lecturer) {
			Lecturer contactItem = (Lecturer) item;
			if (viewHolder instanceof Holder) {
				Holder holder = (Holder) viewHolder;
				checked(holder, contactItem.id);
				imageLoader(holder, contactItem.thumbnail);
			}
		}

	}

	protected void checked(final Holder holder, int id) {

		new AsyncTask<Integer, Void, Boolean>() {

			@Override
			protected void onPostExecute(Boolean exist) {
				if (exist) {
					holder.subscribed.setVisibility(View.VISIBLE);
				}
			};

			@Override
			protected Boolean doInBackground(Integer... id) {
				if (id != null && id.length > 0) {
					return ContentManager.existSubscription(id[0]);
				}
				return false;
			}
		}.execute(id);
	}

	public void refreshIndexer() {
		getIndexer().initPositions(items);
	}
}
