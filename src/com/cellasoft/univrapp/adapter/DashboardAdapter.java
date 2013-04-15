package com.cellasoft.univrapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellasoft.univrapp.widget.DashboardEntry;

public class DashboardAdapter extends ArrayAdapter<DashboardEntry> {
	private Context context;
	private DashboardEntry[] entries;

	static class ViewHolder {
		ImageView image;
		TextView title;
	}

	public DashboardAdapter(Context context, int textViewResourceId,
			DashboardEntry[] objects) {
		super(context, textViewResourceId, objects);

		this.context = context;
		entries = objects;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
//			convertView = View.inflate(context, R.layout.dashboard_entry, null);
			holder = new ViewHolder();
//			holder.title = (TextView) convertView.findViewById(R.id.ItemText);
//			holder.image = (ImageView) convertView.findViewById(R.id.ItemImage);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		holder.title.setText(entries[position].getTitle());
		holder.image.setImageResource(entries[position].getIcon());

		return convertView;
	}

	public int getCount() {
		return entries.length;
	}

	public long getItemId(int position) {
		return 0;
	}
}