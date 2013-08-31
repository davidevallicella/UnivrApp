package com.cellasoft.univrapp.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.model.University;

public class UniversitylAdapter extends BaseListAdapter<University> {

	class Holder extends ViewHolder {
		TextView name;
	}

	public UniversitylAdapter(Context context, int resource) {
		super(context, resource);
		this.items = University.getAllUniversity();
	}

	@Override
	protected void populateDataForRow(ViewHolder viewHolder,
			University university, int position) {
		if (viewHolder instanceof Holder) {
			Holder holder = (Holder) viewHolder;
			holder.thumbnail.setImageResource(university.logo_from_resource);
			holder.name.setText(university.name);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		University university = new University(items.get(position));

		if (convertView == null) {
			convertView = View.inflate(getContext(), resource, null);
			holder = new Holder();
			holder.thumbnail = (ImageView) convertView
					.findViewById(R.id.univr_logo);
			holder.name = (TextView) convertView.findViewById(R.id.univr_name);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		populateDataForRow(holder, university, position);
		convertView.setBackgroundResource(university.color_from_resource);

		return convertView;
	}

	public void setUniversites(List<University> universites) {
		super.setItems(universites);
	}
}
