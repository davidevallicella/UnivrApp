package com.cellasoft.univrapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cellasoft.univrapp.activity.ChannelListActivity;
import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.activity.SubscribeActivity;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.RecyclingImageView;
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
	private ImageFetcher imageFetcher;

	static class ViewHolder {
		RecyclingImageView thumbnail;
		ImageButton checkBox;
		LinearLayout subscribed;
		TextView name;
		TextView email;
		TextView separator;
	}

	public LecturerAdapter(Context context, ArrayList<Lecturer> lecturers,
			OnLecturerViewListener lecturerListener) {
		// Use the parent activity to load the image asynchronously into the
		// ImageView (so a single
		// cache can be used over all pages in the ViewPager
		if (SubscribeActivity.class.isInstance(context)) {
			System.out.println("----ok");
			imageFetcher = ((SubscribeActivity) context).getImageFetcher();
		}
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
		return lecturers.get(position).id;
	}

	@Override
	public boolean hasStableIds() {
		return true;
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
			holder.checkBox = (ImageButton) view
					.findViewById(R.id.lecturer_check);
			holder.thumbnail = (RecyclingImageView) view
					.findViewById(R.id.lecturer_image);
			holder.subscribed = (LinearLayout) view
					.findViewById(R.id.lecturer_subscribed);
			view.setTag(holder);
		} else {
			view = (LecturerView) convertView;
			holder = (ViewHolder) view.getTag();
		}

		view.setItemViewSelected(lecturer.isSelected);
		holder.name.setText(lecturer.name);
		holder.subscribed.setVisibility(View.GONE);
		checked(holder, lecturer.id);
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

	private void checked(final ViewHolder holder, int id) {
		AsyncTask<Integer, Void, Boolean> checkTask = new AsyncTask<Integer, Void, Boolean>() {

			protected void onPostExecute(Boolean exist) {
				if (exist) {
					holder.subscribed.setVisibility(View.VISIBLE);
				}
			};

			@Override
			protected Boolean doInBackground(Integer... id) {
				return ContentManager.existSubscription(id[0]);
			}
		};

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			checkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
		else
			checkTask.execute(id);
	}

	private void imageLoader(ViewHolder holder, String imageUrl) {
		if (imageUrl != null && imageUrl.length() > 0) {
			if (!imageUrl.equals((String) holder.thumbnail.getTag())) {
				imageFetcher.loadThumbnailImage(imageUrl, holder.thumbnail, R.drawable.thumb);
			}
		}
		else if (holder.thumbnail.getTag() != null) {
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