package com.cellasoft.univrapp.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.adapter.LecturerSectionAdapter;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.reader.UnivrReader;
import com.cellasoft.univrapp.utils.FileCache;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.widget.LecturerView;
import com.cellasoft.univrapp.widget.OnLecturerViewListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

@SuppressLint("NewApi")
public class SubscribeActivity extends SherlockListActivity {

	private PostData post_data;
	private ArrayList<Lecturer> lecturers;
	private LecturerSectionAdapter sectionAdapter;
	private AdView adView;

	private boolean updated = true;

	class PostData {
		public static final int ALL = -1;
		public static final int NO_ONE = 0;

		int gi;
		int mi;
		int ai;
		int gf;
		int mf;
		int af;
		int personeMittente;
		int struttureMittente = NO_ONE;
		int biblioCRMittente = NO_ONE;
		int csMittente = NO_ONE;
		int oiMittente = NO_ONE;

		public Map<String, Integer> getPostData() {
			Map<String, Integer> post = new HashMap<String, Integer>();
			post.put("gi", gi);
			post.put("mi", mi);
			post.put("ai", ai);
			post.put("gf", gf);
			post.put("mf", mf);
			post.put("af", af);
			post.put("personeMittente", personeMittente);
			post.put("struttureMittente", struttureMittente);
			post.put("biblioCRMittente", biblioCRMittente);
			post.put("csMittente", csMittente);
			post.put("oiMittente", oiMittente);
			return post;
		}

		public String setParams() {
			Map<String, Integer> params = getPostData();
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> postParams = new ArrayList<NameValuePair>();

				for (Map.Entry<String, Integer> entry : params.entrySet())
					if (entry.getValue() != null)
						postParams.add(new BasicNameValuePair(entry.getKey(),
								String.valueOf(entry.getValue())));
				return "&" + URLEncodedUtils.format(postParams, "utf-8");
			}
			return "";
		}
	}

	OnLecturerViewListener lecturerListener = new OnLecturerViewListener() {
		@Override
		public void onSelected(LecturerView view, boolean selected) {
			final int position = getListView().getPositionForView(view);
			if (position != ListView.INVALID_POSITION) {
				((Lecturer) sectionAdapter.getItem(position)).isSelected = selected;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lecturer_view);
		ImageLoader.initialize(this);
		init();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadData();
	}

	private void init() {
		initListView();
		initBanner();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.rss);
		getSupportActionBar().setTitle(
				getResources().getString(R.string.subscribe_title));
		getSupportActionBar().setSubtitle(Settings.getUniversity().name);

		post_data = new PostData();
		// get the current date
		Calendar c = Calendar.getInstance();
		post_data.gi = c.get(Calendar.DAY_OF_MONTH);
		post_data.mi = c.get(Calendar.MONTH) + 1;
		post_data.ai = c.get(Calendar.YEAR);
		post_data.gf = c.get(Calendar.DAY_OF_MONTH);
		post_data.mf = c.get(Calendar.MONTH) + 1;
		post_data.af = c.get(Calendar.YEAR) + 7;
	}

	private void initListView() {

		getListView().setFastScrollEnabled(true);
		getListView().setDivider(
				getResources().getDrawable(
						android.R.drawable.divider_horizontal_bright));

		getListView().setSelector(R.drawable.list_selector_on_top);

		getListView().setDrawSelectorOnTop(true);
		getListView().invalidateViews();

	}

	private ImageButton closeAdmodButton;

	private void initBanner() {
		// Look up the AdView as a resource and load a request.
		adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

		adView.setAdListener(new AdListener() {
			@Override
			public void onReceiveAd(Ad arg0) {
				if (closeAdmodButton == null) {
					addCloseButtonTask(adView);
				} else {
					adView.setVisibility(View.VISIBLE);
					closeAdmodButton.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onPresentScreen(Ad arg0) {
			}

			@Override
			public void onLeaveApplication(Ad arg0) {
			}

			@Override
			public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
			}

			@Override
			public void onDismissScreen(Ad arg0) {
			}
		});
	}

	private void addCloseButtonTask(final AdView adView) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPostExecute(Void result) {
				runOnUiThread(new Runnable() {
					public void run() {
						((RelativeLayout) findViewById(R.id.AdModLayout))
								.addView(closeAdmodButton);
					}
				});
			}

			@Override
			protected Void doInBackground(Void... params) {
				SystemClock.sleep(5000);

				RelativeLayout.LayoutParams closeLayoutParams = new RelativeLayout.LayoutParams(
						30, 30);
				closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
						RelativeLayout.TRUE);
				closeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT,
						RelativeLayout.TRUE);
				closeLayoutParams.bottomMargin = (int) adView.getHeight() - 15;
				closeLayoutParams.leftMargin = 15;

				closeAdmodButton = new ImageButton(getApplicationContext());
				closeAdmodButton.setLayoutParams(closeLayoutParams);
				closeAdmodButton.setImageResource(R.drawable.close_button);
				closeAdmodButton
						.setBackgroundResource(android.R.color.transparent);
				closeAdmodButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						closeAdmodButton.setVisibility(View.GONE);
						if (adView != null) {
							adView.setVisibility(View.GONE);
						}
					}
				});

				return null;
			}
		}.execute();
	}

	private void loadData() {
		loadLecturers();
		sectionAdapter = new LecturerSectionAdapter(this, lecturers,
				lecturerListener, R.layout.section_header, R.id.title);
		setListAdapter(sectionAdapter);
	}

	private void loadLecturers() {
		int dest = Settings.getUniversity().dest;
		lecturers = ContentManager.loadLecturersOfDest(dest,
				ContentManager.FULL_LECTURER_LOADER);
		if (lecturers == null || lecturers.isEmpty()) {
			lecturers = new ArrayList<Lecturer>();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Lecturer lecturer = (Lecturer) sectionAdapter.getItem(position);
		showContact(lecturer);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.lecturer_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_subscribe:
			confirmBeforeSavingSubscriptions();
			return true;
		case R.id.menu_reload:
			refreshItem = item;
			reloadLecturers();
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void confirmBeforeSavingSubscriptions() {
		Resources res = getResources();
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(res.getString(R.string.sub_channel_dialog_title))
				.setMessage(res.getString(R.string.sub_channel_dialog))
				.setPositiveButton(res.getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								saveSubscriptions();
							}
						})
				.setNegativeButton(res.getString(R.string.no),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		dialog.show();
	}

	private void saveSubscriptions() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(
				R.string.sub_channel_dialog2));
		BetterAsyncTask<Void, Void, Void> subscribingTask = new BetterAsyncTask<Void, Void, Void>(
				this) {

			@Override
			protected void after(Context context, Void arg1) {
				progressDialog.dismiss();
				String message = getResources().getString(R.string.success);
				Toast.makeText(SubscribeActivity.this, message,
						Toast.LENGTH_LONG).show();
			}

			@Override
			protected void handleError(Context context, Exception e) {
				progressDialog.dismiss();
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
		};

		subscribingTask.disableDialog();
		subscribingTask
				.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
					public Void call(BetterAsyncTask<Void, Void, Void> arg0)
							throws Exception {
						for (Lecturer lecturer : lecturers) {
							if (lecturer.isSelected) {
								post_data.personeMittente = lecturer.key;
								String description = lecturer.email;
								String url = Settings.getUniversity().url
										+ post_data.setParams();
								new Channel(lecturer.id, lecturer.name, url,
										lecturer.thumbnail, description)
										.subscribe();
							}
						}

						sectionAdapter.refresh();
						return null;
					}
				});
		progressDialog.show();
		subscribingTask.execute();
	}

	private void reloadLecturers() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage(Html
				.fromHtml(getString(R.string.progress_txt).replace("{message}",
						"<b>Connect to </b>" + Settings.getUniversity().name)));
		BetterAsyncTask<Void, String, Void> saveLecturersTask = new BetterAsyncTask<Void, String, Void>(
				this) {

			@Override
			protected void before(Context context) {
				updated = false;
				FileCache.clearCacheIfNecessary();
				refreshAnim();
			}

			@Override
			protected void after(Context context, Void arg1) {
				updated = true;
				loadData();
				progressDialog.dismiss();
				String message = getResources().getString(R.string.success);
				Toast.makeText(SubscribeActivity.this, message,
						Toast.LENGTH_LONG).show();
			}

			@Override
			protected Void doCheckedInBackground(Context context,
					Void... params) throws Exception {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

				if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {

					List<Lecturer> lecturers = UnivrReader.getLecturers();

					if (!isCancelled()) {
						progressDialog.setCancelable(false);
						progressDialog.setMax(lecturers.size());

						for (Lecturer lecturer : lecturers) {
							publishProgress("<b>Save Lecturer</b><br/>"
									+ lecturer.name + "...");
							lecturer.save();
						}

						lecturers.clear();
					}
				} else {
					throw new UnivrReaderException(getResources().getString(
							R.string.univrapp_connection_exception));
				}

				return null;
			}

			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				if (values[0].length() != 0)
					progressDialog.setMessage(Html.fromHtml(getString(
							R.string.progress_txt).replace("{message}",
							values[0])));
				progressDialog.incrementProgressBy(1);
			}

			@Override
			protected void handleError(Context context, Exception e) {
				updated = true;
				progressDialog.dismiss();
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
		};

		saveLecturersTask.disableDialog();
		progressDialog.show();
		saveLecturersTask.execute();
	}

	View refreshView;
	ImageView refreshImage;
	com.actionbarsherlock.view.MenuItem refreshItem;
	Animation rotateClockwise;

	private void refreshAnim() {
		// Inflate our custom layout.
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		refreshView = inflater.inflate(R.layout.refresh_actionview, null);

		// Load the animation
		final Animation rotateClockwise = AnimationUtils.loadAnimation(this,
				R.anim.rotate);
		rotateClockwise.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				if (updated)
					rotateClockwise.setRepeatCount(0);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (updated) { // Download complete? Stop.
					refreshView.clearAnimation();
					refreshItem.setActionView(null);
				} else { // Still downloading? Start again.
					refreshView.startAnimation(rotateClockwise);
				}
			}
		}); // Set the listener

		// Apply the View to our MenuItem
		refreshItem.setActionView(refreshView);
		// Apply the animation to our View
		refreshView.startAnimation(rotateClockwise);

	}

	private void showContact(Lecturer lecturer) {
		Intent intent = new Intent(this, ContactActivity.class);
		intent.putExtra(ContactActivity.LECTURER_ID_PARAM, lecturer.id);
		intent.putExtra(ContactActivity.LECTURER_NAME_PARAM, lecturer.name);
		intent.putExtra(ContactActivity.LECTURER_OFFICE_PARAM, lecturer.office);
		intent.putExtra(ContactActivity.LECTURER_THUMB_PARAM,
				lecturer.thumbnail);
		startActivity(intent);
	}
}
