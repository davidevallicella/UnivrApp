package com.cellasoft.univrapp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.cellasoft.univrapp.adapter.ItemImageLoaderHandler;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.utils.StreamDrawable;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;

public class ContactActivity extends SherlockActivity {
	private static final int CORNER_RADIUS = 3; // dips
	private static final int MARGIN = 1; // dips

	public static final String LECTURER_ID_PARAM = "LecturerId";
	public static final String LECTURER_NAME_PARAM = "LecturerName";
	public static final String LECTURER_OFFICE_PARAM = "LecturerOffice";
	public static final String LECTURER_THUMB_PARAM = "LecturerThumb";
	private Lecturer lecturer;

	public static int mCornerRadius;
	public static int mMargin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageLoader.initialize(this);

		setContentView(R.layout.contact);

		if (getIntent().hasExtra(LECTURER_ID_PARAM)) {
			Intent intent = getIntent();
			int lecturerId = intent.getIntExtra(LECTURER_ID_PARAM, 0);
			String lecturerName = intent.getStringExtra(LECTURER_NAME_PARAM);
			String lecturerOffice = intent
					.getStringExtra(LECTURER_OFFICE_PARAM);
			String lecturerThumb = intent.getStringExtra(LECTURER_THUMB_PARAM);
			loadContactInfo(lecturerId, lecturerName, lecturerOffice,
					lecturerThumb);
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// go back
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void imageLoader(ImageView image, String imageUrl) {
		if (imageUrl != null && imageUrl.length() > 0) {

			// default image
			image.setImageResource(R.drawable.thumb);
			image.setTag(imageUrl);
			try {
				// 1st level cache
				Bitmap bitmap = ImageLoader.get(imageUrl);
				if (bitmap != null) {
					StreamDrawable d = new StreamDrawable(bitmap,
							mCornerRadius, mMargin);
					image.setImageDrawable(d);
				} else {
					// 2st level cache
					// 3st downloading
					ImageLoader.start(imageUrl, new ItemImageLoaderHandler(
							image, imageUrl));
				}
			} catch (RuntimeException e) {
			}

		} else {
			image.setTag(null);
			// default image
			image.setImageResource(R.drawable.thumb);
		}
	}

	private void loadContactInfo(final int lecturerId,
			final String lecturerName, final String lecturerOffice,
			final String lecturerThumb) {
		BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
				this) {

			@Override
			protected void before(Context context) {
				final float density = getResources().getDisplayMetrics().density;
				mCornerRadius = (int) (CORNER_RADIUS * density + 0.5f);
				mMargin = (int) (MARGIN * density + 0.5f);

				ImageView image = (ImageView) findViewById(R.id.contact_image);
				imageLoader(image, lecturerThumb);

				((TextView) findViewById(R.id.contact_title))
						.setText(lecturerName);
				((TextView) findViewById(R.id.contact_office))
						.setText(lecturerOffice);
			}

			@Override
			protected void handleError(Context context, Exception e) {
			}

			@Override
			protected void after(Context arg0, Void arg1) {

			}

		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
			public Void call(BetterAsyncTask<Void, Void, Void> task)
					throws Exception {

				lecturer = Lecturer.findById(lecturerId);

				final Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

				runOnUiThread(new Runnable() {
					public void run() {
						if (lecturer.telephone != null
								&& lecturer.telephone.length() > 0) {
							((TextView) findViewById(R.id.contact_phone))
									.setText(lecturer.telephone);
							findViewById(R.id.contact_phone_action)
									.setOnTouchListener(new OnTouchListener() {

										@Override
										public boolean onTouch(View v,
												MotionEvent event) {
											if (MotionEvent.ACTION_DOWN == event
													.getAction()) {
												vib.vibrate(50);
												v.startAnimation(AnimationUtils
														.loadAnimation(
																ContactActivity.this,
																R.anim.image_click));
												Intent callIntent = new Intent(
														Intent.ACTION_CALL);
												callIntent.setData(Uri
														.parse("tel:"
																+ lecturer.telephone));
												startActivity(callIntent);
												return true;
											}
											return false;
										}
									});
							findViewById(R.id.contact_phone_row_view)
									.setVisibility(View.VISIBLE);
						} else {
							findViewById(R.id.contact_phone_row_view)
									.setVisibility(View.GONE);
						}

						if (lecturer.email != null
								&& lecturer.email.length() > 0) {
							((TextView) findViewById(R.id.contact_email))
									.setText(lecturer.email);
							findViewById(R.id.contact_email_action)
									.setOnTouchListener(new OnTouchListener() {

										@Override
										public boolean onTouch(View v,
												MotionEvent event) {
											if (MotionEvent.ACTION_DOWN == event
													.getAction()) {
												vib.vibrate(50);
												v.startAnimation(AnimationUtils
														.loadAnimation(
																ContactActivity.this,
																R.anim.image_click));
												String mailTo = lecturer.email;
												Intent email_intent = new Intent(
														Intent.ACTION_SENDTO,
														Uri.fromParts("mailto",
																mailTo, null));
												startActivity(Intent
														.createChooser(
																email_intent,
																"Send email..."));
												return true;
											}
											return false;
										}
									});
							findViewById(R.id.contact_mail_row_view)
									.setVisibility(View.VISIBLE);
						} else {
							findViewById(R.id.contact_mail_row_view)
									.setVisibility(View.GONE);
						}

						if (lecturer.department != null
								&& lecturer.department.length() > 0) {
							((TextView) findViewById(R.id.contact_department))
									.setText(lecturer.department);
							findViewById(R.id.contact_department_row_view)
									.setVisibility(View.VISIBLE);
						} else {
							findViewById(R.id.contact_department_row_view)
									.setVisibility(View.GONE);
						}
					}
				});

				return null;
			}
		});
		task.disableDialog();
		task.execute();
	}
}
