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
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.UnivrReaderFactory;
import com.cellasoft.univrapp.adapter.LecturerSectionAdapter;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.ClosableAdView;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.UIUtils;
import com.cellasoft.univrapp.widget.LecturerView;
import com.cellasoft.univrapp.widget.OnLecturerViewListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;

@SuppressLint("NewApi")
public class SubscribeActivity extends SherlockListActivity {

	private PostData post_data;
	private ArrayList<Lecturer> lecturers;
	private LecturerSectionAdapter sectionAdapter;
	private ClosableAdView adView;
	private ImageFetcher imageFetcher;
	
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
		if (Constants.DEBUG_MODE) {
			//UIUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		imageFetcher = new ImageFetcher(this);
		Application.parents.push(getClass());
		setContentView(R.layout.lecturer_view);
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

	@Override
	protected void onPause() {
		super.onPause();
		imageFetcher.setPauseWork(false);
		imageFetcher.setExitTasksEarly(true);
		imageFetcher.flushCache();
	}

	@Override
	protected void onResume() {
		super.onResume();
		imageFetcher.setExitTasksEarly(false);
		showAdmodBanner();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (adView != null) {
			adView.hideAd();
		}
		imageFetcher.closeCache();
	}

	private void init() {
		initListView();
		initAnimation();
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

	private void initAnimation() {
		LayoutAnimationController controller = AnimationUtils
				.loadLayoutAnimation(SubscribeActivity.this,
						R.anim.list_layout_controller);
		controller.getAnimation().reset();

		getListView().setLayoutAnimation(controller);
	}

	private void initListView() {
		getListView().setFastScrollEnabled(true);
		getListView().setDrawSelectorOnTop(true);
		getListView().setSelector(R.drawable.list_selector_on_top);
		getListView().invalidateViews();
		getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView,
					int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					imageFetcher.setPauseWork(true);
				} else {
					imageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}

	private void initBanner() {
		// Look up the AdView as a resource and load a request.
		adView = (ClosableAdView) this.findViewById(R.id.adView);
		adView.inizialize(this);
		adView.loadAd();
	}

	private void showAdmodBanner() {
		if (adView != null) {
			adView.viewAd();
		}
	}
	
	/**
     * Called by the ViewPager child fragments to load images via the one ImageFetcher
     */
    public ImageFetcher getImageFetcher() {
        return imageFetcher;
    }

	private void loadData() {
		loadLecturers();
	}

	private void loadLecturers() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				sectionAdapter = new LecturerSectionAdapter(
						SubscribeActivity.this, lecturers, lecturerListener,
						R.layout.section_header, R.id.title);
				setListAdapter(sectionAdapter);
			}

			@Override
			protected Void doInBackground(Void... params) {
				int dest = Settings.getUniversity().dest;
				lecturers = ContentManager.loadLecturersOfDest(dest,
						ContentManager.FULL_LECTURER_LOADER);
				if (lecturers == null || lecturers.isEmpty()) {
					lecturers = new ArrayList<Lecturer>();
				}
				return null;
			}
		}.execute();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Lecturer lecturer = (Lecturer) sectionAdapter.getItem(position);
		showContact(lecturer, v);
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
			// NavUtils.navigateUpFromSameTask(this);
			finish();
			return true;
		case R.id.clear_cache:
			imageFetcher.clearCache();
			Toast.makeText(this, R.string.clear_cache_complete_toast,
					Toast.LENGTH_SHORT).show();
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
		if (UIUtils.hasHoneycomb())
			subscribingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		else
			subscribingTask.execute((Void[]) null);
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
				imageFetcher.clearCache();
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
				if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {

					List<Lecturer> lecturers = UnivrReaderFactory
							.getUnivrReader().getLecturers();

					if (!isCancelled()) {
						progressDialog.setCancelable(false);
						progressDialog.setMax(lecturers.size());

						for (Lecturer lecturer : lecturers) {
							publishProgress("<b>Save Lecturer</b><br/>"
									+ lecturer.name + "...");
							lecturer.save();
						}

						lecturers.clear();
					} else {
						updated = true;
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
		if (UIUtils.hasHoneycomb()) {
			saveLecturersTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		} else
			saveLecturersTask.execute((Void[]) null);
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

	private void showContact(Lecturer lecturer, View v) {
		Intent intent = new Intent(this, ContactActivity.class);
		intent.putExtra(ContactActivity.LECTURER_ID_PARAM, lecturer.id);
		intent.putExtra(ContactActivity.LECTURER_NAME_PARAM, lecturer.name);
		intent.putExtra(ContactActivity.LECTURER_OFFICE_PARAM, lecturer.office);
		intent.putExtra(ContactActivity.LECTURER_THUMB_PARAM,
				lecturer.thumbnail);
		if (UIUtils.hasJellyBean()) {
			// makeThumbnailScaleUpAnimation() looks kind of ugly here as the
			// loading spinner may
			// show plus the thumbnail image in GridView is cropped. so using
			// makeScaleUpAnimation() instead.
			ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v,
					0, 0, v.getWidth(), v.getHeight());
			startActivity(intent, options.toBundle());
		} else {
			startActivity(intent);
		}
	}
}
