package com.cellasoft.univrapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.model.University;

public class UniversitylAdapter extends BaseAdapter {

	private ArrayList<University> universites;

	private Context context;

	static class ViewHolder {
		ImageView logo;
		TextView name;
	}

	public UniversitylAdapter(Context context) {
		this.context = context;
		this.universites = University.getAllUniversity();
	}

	public int getCount() {
		return universites.size();
	}

	public University getItem(int position) {
		return universites.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		University university = universites.get(position);

		if (convertView == null) {
			convertView = View.inflate(context, R.layout.university_item, null);
			holder = new ViewHolder();
			holder.logo = (ImageView) convertView.findViewById(R.id.univr_logo);
			holder.name = (TextView) convertView.findViewById(R.id.univr_name);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		holder.logo.setImageResource(university.logo_from_resource);
		holder.name.setText(university.name);
		convertView.setBackgroundResource(university.color_from_resource);
		return convertView;
	}

	public void setUniversites(ArrayList<University> universites) {
		this.universites = universites;
		notifyDataSetChanged();
	}

	public void refresh() {
		notifyDataSetChanged();
	}
}
