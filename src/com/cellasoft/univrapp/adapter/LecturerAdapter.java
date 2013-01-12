package com.cellasoft.univrapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.ImageLoader;

public class LecturerAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Lecturer> lecturers;

	static class ViewHolder {
		ImageView thumbnail;
		TextView name;
	}

	public LecturerAdapter(Context context, ArrayList<Lecturer> lecturers) {
		this.context = context;
		this.lecturers = lecturers;
	}

	public int getCount() {
		return lecturers.size();
	}

	public Lecturer getItem(int position) {
		return lecturers.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Lecturer lecturer = lecturers.get(position);

		if (convertView == null) {
			convertView = View.inflate(context, R.layout.lecturer_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView
					.findViewById(R.id.lecturer_name);
			holder.thumbnail = (ImageView) convertView
					.findViewById(R.id.lecturer_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.name.setText(lecturer.name);
		String imageUrl = lecturer.thumbnail;
		holder.thumbnail.setTag(imageUrl);
		if (imageUrl != null && !imageUrl.equals("")) {
			try {
				Bitmap itemImage = ImageLoader.get(imageUrl);
				if (itemImage != null) {
					holder.thumbnail.setVisibility(View.VISIBLE);
					holder.thumbnail.setImageBitmap(itemImage);
				} else {
					ImageLoader.start(imageUrl, new ItemImageLoaderHandler(
							holder.thumbnail, imageUrl));
				}
			} catch (RuntimeException e) {
			}
		} else {
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.thumb);
			holder.thumbnail.setImageBitmap(bitmap);
		}

		return convertView;
	}

	public void setChannels(ArrayList<Lecturer> lecturers) {
		this.lecturers = lecturers;
		notifyDataSetChanged();
	}

	public void refresh() {
		notifyDataSetChanged();
	}
}