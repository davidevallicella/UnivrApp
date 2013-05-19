package com.cellasoft.univrapp.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.Utils;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;

public class ContactActivity extends SherlockActivity {

	public static final String LECTURER_ID_PARAM = "LecturerId";
	public static final String LECTURER_NAME_PARAM = "LecturerName";
	public static final String LECTURER_OFFICE_PARAM = "LecturerOffice";
	public static final String LECTURER_THUMB_PARAM = "LecturerThumb";
	private Lecturer lecturer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageFetcher.inizialize(this);
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

		getSupportActionBar().setTitle(
				getResources().getString(R.string.contact_title));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.contact_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// go back
			finish();
			return true;
		case R.id.menu_web_page:
			showWebPage();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private String tellOffice;
	private String tellLab;

	private void loadContactInfo(final int lecturerId,
			final String lecturerName, final String lecturerOffice,
			final String lecturerThumb) {
		BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
				this) {

			@Override
			protected void before(Context context) {
				ImageView image = (ImageView) findViewById(R.id.contact_image);

				try {
					ImageFetcher.getInstance().loadImage(lecturerThumb, image);
				} catch (Exception e) {
				}

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

							tellOffice = lecturer.telephone;

							if (tellOffice.contains("/")) {
								String[] tells = tellOffice.split("/");
								tellOffice = tells[0].trim();
								tellLab = tells[1].trim();
							}

							((TextView) findViewById(R.id.contact_phone))
									.setText(tellOffice.replace(" - ", "\n").trim());
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

												callNumber(tellOffice);
												return true;
											}
											return false;
										}
									});
							findViewById(R.id.contact_phone_row_view)
									.setVisibility(View.VISIBLE);

							if (tellLab != null && tellLab.length() > 0) {
								((TextView) findViewById(R.id.contact_phone_lab))
										.setText(tellLab.replace(" - ", "\n").trim());
								findViewById(R.id.contact_phone_lab_action)
										.setOnTouchListener(
												new OnTouchListener() {

													@Override
													public boolean onTouch(
															View v,
															MotionEvent event) {
														if (MotionEvent.ACTION_DOWN == event
																.getAction()) {
															vib.vibrate(50);
															v.startAnimation(AnimationUtils
																	.loadAnimation(
																			ContactActivity.this,
																			R.anim.image_click));

															callNumber(tellLab);
															return true;
														}
														return false;
													}
												});
								findViewById(R.id.contact_phone_lab_row_view)
										.setVisibility(View.VISIBLE);
							}
						} else {
							findViewById(R.id.contact_phone_row_view)
									.setVisibility(View.GONE);
							findViewById(R.id.contact_phone_lab_row_view)
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
		if (Utils.hasHoneycomb())
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		else
			task.execute((Void[]) null);
	}

	private void chooseNumberDialog(final String[] telephones) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isFinishing()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ContactActivity.this).setTitle(getResources()
							.getString(R.string.contact_dialog_title));
					builder.setItems(telephones, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							call(telephones[which].trim());
						}

					});
					builder.show();
				}
			}
		});
	}

	private void call(String telephone) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + telephone));
		startActivity(callIntent);
	}
	
	private void showWebPage() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(Settings.getUniversity().domain
						+ "/fol/main?ent=persona&id=" + lecturer.key));
		startActivity(browserIntent);
	}

	private void callNumber(String telephone) {
		if (telephone.contains("-")) {
			String[] telephones = telephone.split(" - ");
			chooseNumberDialog(telephones);
		} else {
			call(telephone);
		}
	}

}
