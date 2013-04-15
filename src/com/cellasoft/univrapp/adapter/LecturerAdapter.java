package com.cellasoft.univrapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.utils.StreamDrawable;
import com.cellasoft.univrapp.widget.LecturerView;
import com.cellasoft.univrapp.widget.OnLecturerViewListener;

public class LecturerAdapter extends BaseAdapter {
	private static final int CORNER_RADIUS = 3; // dips
	private static final int MARGIN = 1; // dips

	public static int mCornerRadius;
	public static int mMargin;

	private Context context;
	private ArrayList<Lecturer> lecturers = new ArrayList<Lecturer>();
	private OnLecturerViewListener lecturerListener;

	static class ViewHolder {
		ImageView thumbnail;
		LinearLayout subscribed;
		TextView name;
		TextView email;
		TextView separator;
	}

	public LecturerAdapter(Context context, ArrayList<Lecturer> lecturers,
			OnLecturerViewListener lecturerListener) {
		ImageLoader.initialize(context);
		this.context = context;
		this.lecturers = lecturers;
		this.lecturerListener = lecturerListener;

		final float density = context.getResources().getDisplayMetrics().density;
		mCornerRadius = (int) (CORNER_RADIUS * density + 0.5f);
		mMargin = (int) (MARGIN * density + 0.5f);
	}

	@Override
	public int getCount() {
		return lecturers.size();
	}

	@Override
	public Lecturer getItem(int position) {
		return lecturers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		LecturerView view;
		Lecturer lecturer = getItem(position);

		if (convertView == null) {
			view = (LecturerView) View.inflate(context, R.layout.lecturer_item,
					null);
			view.setLecturerListener(lecturerListener);
			holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.lecturer_name);
			holder.thumbnail = (ImageView) view
					.findViewById(R.id.lecturer_image);
			holder.subscribed = (LinearLayout) view.findViewById(R.id.lecturer_subscribed);
			//holder.email = (TextView) view.findViewById(R.id.lecturer_description);
			view.setTag(holder);
		} else {
			view = (LecturerView) convertView;
			holder = (ViewHolder) view.getTag();
		}
		if(ContentManager.existSubscription(lecturer.id)) {
			holder.subscribed.setVisibility(View.VISIBLE);
		} else {
			holder.subscribed.setVisibility(View.GONE);
		}

		view.setItemViewSelected(lecturer.isSelected);
		holder.name.setText(lecturer.name);
		imageLoader(holder, lecturer.thumbnail);

		return view;
	}

	public void setLecturers(ArrayList<Lecturer> lecturers) {
		this.lecturers = lecturers;
		notifyDataSetChanged();
	}

	public void refresh() {
		notifyDataSetChanged();
	}

	public void clear() {
		lecturers.clear();
		notifyDataSetChanged();
	}

	private void imageLoader(ViewHolder holder, String imageUrl) {
		if (imageUrl != null && imageUrl.length() > 0) {
			if (!imageUrl.equals((String) holder.thumbnail.getTag())) {
				holder.thumbnail.setImageResource(R.drawable.thumb);
				holder.thumbnail.setTag(imageUrl);
				try {
					// 1st level cache
					Bitmap bitmap = ImageLoader.get(imageUrl);

					if (bitmap != null) {
						StreamDrawable d = new StreamDrawable(bitmap,
								mCornerRadius, mMargin);
						holder.thumbnail.setImageDrawable(d);
					} else {
						// 2st level cache
						// 3st downloading
						ImageLoader.start(imageUrl, new ItemImageLoaderHandler(
								holder.thumbnail, imageUrl));
					}
				} catch (RuntimeException e) {
				}
			}
		} else if (holder.thumbnail.getTag() != null) {
			holder.thumbnail.setTag(null);
			// default image
			holder.thumbnail.setImageResource(R.drawable.thumb);
		}
	}

	public void setOnLecturerViewListener(
			OnLecturerViewListener lecturerListener) {
		this.lecturerListener = lecturerListener;
	}
}