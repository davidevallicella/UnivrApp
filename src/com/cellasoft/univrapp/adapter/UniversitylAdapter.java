package com.cellasoft.univrapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.Constants.UNIVERSITY;

public class UniversitylAdapter extends BaseAdapter {

	private ArrayList<String> universites;

	private Context context;

	static class ViewHolder {
		ImageView logo;
		TextView name;
	}

	public UniversitylAdapter(Context context) {
		this.context = context;
		this.universites = Constants.UNIVERSITY.UNIVERSITES;
	}

	public int getCount() {
		return universites.size();
	}

	public String getItem(int position) {
		return universites.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		String item = universites.get(position);

		if (convertView == null) {
			convertView = View.inflate(context, R.layout.university_item, null);
			holder = new ViewHolder();
			holder.logo = (ImageView) convertView.findViewById(R.id.univr_logo);
			holder.name = (TextView) convertView.findViewById(R.id.univr_name);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		holder.logo.setImageDrawable(UNIVERSITY.LOGO.get(item));
		holder.name.setText(item);

		return convertView;
	}

	public void setUniversites(ArrayList<String> universites) {
		this.universites = universites;
		notifyDataSetChanged();
	}

	public void refresh() {
		notifyDataSetChanged();
	}
}
