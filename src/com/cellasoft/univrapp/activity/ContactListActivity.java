package com.cellasoft.univrapp.activity;

import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.model.University;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.Lists;
import com.cellasoft.univrapp.utils.UIUtils;
import com.cellasoft.univrapp.widget.ContactItemInterface;
import com.cellasoft.univrapp.widget.LecturerListView;
import com.cellasoft.univrapp.widget.LecturerView;
import com.cellasoft.univrapp.widget.OnLecturerViewListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

public class ContactListActivity extends BaseListActivity implements
		OnQueryTextListener {

	private final static String TAG = makeLogTag(ContactListActivity.class);

	private LecturerListView listView;
	private List<ContactItemInterface> lecturers = null;
	private List<ContactItemInterface> filterList = null;
	private SearchListTask curSearchTask = null;

	private ImageFetcher imageFetcher;
	private University university;
	private PostData post_data;

	OnLecturerViewListener lecturerListener = new OnLecturerViewListener() {
		@Override
		public void onSelected(LecturerView view, boolean selected) {
			final int position = listView.getPositionForView(view);
			ContactItemInterface item = listView.getItemAtPosition(position);
			if (item instanceof Lecturer) {
				((Lecturer) item).isSelected = selected;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onCreate()");
		}

		super.onCreate(savedInstanceState);
		imageFetcher = ImageFetcher.getInstance(this);
		setContentView(R.layout.lecturer_list);

		Application.parents.push(getClass());
		init();
	}

	private void init() {
		university = Settings.getUniversity();
		lecturers = Lists.newArrayList();
		filterList = Lists.newArrayList();
		post_data = new PostData();

		initActionBar();
		initListView();
		initAnimation();
	}

	@Override
	protected void initActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.rss);
		getSupportActionBar().setTitle(
				getResources().getString(R.string.subscribe_title));
		getSupportActionBar().setSubtitle(university.name);
	}

	@Override
	protected void initListView() {
		listView = (LecturerListView) getListView();

		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView,
					int scrollState) {
				// Pause fetcher to ensure smoother scrolling when
				// flinging
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

		// Set a listener to be invoked when the list should be refreshed.
		listView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// Do work to refresh the list here.
				refresh();
			}
		});

		listView.setLecturerViewlistener(lecturerListener);
		loadData();
	}

	private void initSearchView(Menu menu) {
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		if (searchView != null) {
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(false);
		}

		searchView.setOnQueryTextListener(this);
	}

	private void initAnimation() {
		LayoutAnimationController controller = AnimationUtils
				.loadLayoutAnimation(this, R.anim.list_layout_controller);
		controller.getAnimation().reset();

		listView.setLayoutAnimation(controller);
	}

	@Override
	protected void onPause() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onPause()");
		}
		super.onPause();

		imageFetcher.stop();
	}

	@Override
	protected void onResume() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onResume()");
		}
		super.onResume();

		imageFetcher.setExitTasksEarly(false);
		listView.refresh();
	}

	@Override
	protected void onDestroy() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onDestroy()");
		}
		super.onDestroy();

		if (curSearchTask != null) {
			curSearchTask.cancel();
			curSearchTask = null;
		}

		if (listView != null) {
			listView.clean();
			unbindDrawables(listView);
		}

		if (filterList != null) {
			filterList.clear();
		}

		if (lecturers != null) {
			lecturers.clear();
		}

	}

	@Override
	protected void loadData() {
		if (refresh)
			return;

		refresh = true;

		BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
				this) {

			@Override
			protected void before(Context context) {
				listView.clean();
			}

			@Override
			protected void after(Context arg0, Void items) {
				if (running) {
					listView.setItems(lecturers);
					listView.startLayoutAnimation();
				}
				refresh = false;
			}

			protected void handleError(Context context, Exception e) {
				if (running) {
					String message = getResources().getString(
							R.string.not_load_lecturer_notification);
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				}
				refresh = false;
			}
		};

		task.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
			public Void call(BetterAsyncTask<Void, Void, Void> task)
					throws Exception {
				lecturers = Lecturer.loadFullLecturers();
				return null;
			}
		});
		task.disableDialog();
		UIUtils.execute(task, (Void[]) null);
	}

	private void refresh() {
		if (refresh)
			return;

		refresh = true;

		BetterAsyncTask<Void, Void, List<ContactItemInterface>> task = new BetterAsyncTask<Void, Void, List<ContactItemInterface>>(
				this) {

			@Override
			protected void after(Context context,
					final List<ContactItemInterface> newItems) {
				if (running) {
					String size = "0";
					if (newItems != null && newItems.size() > 0) {
						new Runnable() {
							public void run() {
								try {
									listView.addItems(newItems);
									listView.refreshIndexer();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.run();

						size = String.valueOf(newItems.size());
					}

					listView.onRefreshComplete();
					String message = getResources().getString(
							R.string.new_lecturers_notification).replace(
							"{total}", size);
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				}
				refresh = false;
			}

			@Override
			protected void handleError(Context context, Exception e) {
				if (running) {
					listView.onRefreshComplete();
					String message = getResources().getString(
							R.string.not_load_lecturer_notification);
					Toast.makeText(context, message + "\n" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
				refresh = false;
			}
		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, List<ContactItemInterface>>() {

			@Override
			public List<ContactItemInterface> call(
					BetterAsyncTask<Void, Void, List<ContactItemInterface>> arg0)
					throws Exception {
				if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
					return university.update();
				} else
					throw new UnivrReaderException(getResources().getString(
							R.string.univrapp_connection_exception));
			}
		});
		task.disableDialog();
		UIUtils.execute(task, (Void[]) null);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		ContactItemInterface item = listView.getItemAtPosition(position);
		if (item instanceof Lecturer) {
			showContact((Lecturer) item, v);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.lecturer_menu, menu);
		initSearchView(menu);
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_subscribe:
			confirmBeforeSavingSubscriptions();
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
		BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
				this) {

			@Override
			protected void after(Context context, Void arg1) {
				if (running) {
					listView.refresh();
					progressDialog.dismiss();
					String message = getResources().getString(R.string.success);
					Toast.makeText(ContactListActivity.this, message,
							Toast.LENGTH_LONG).show();
				}
			}

			@Override
			protected void handleError(Context context, Exception e) {
				progressDialog.dismiss();
				if (running) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
			}
		};

		task.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
			public Void call(BetterAsyncTask<Void, Void, Void> arg0)
					throws Exception {
				for (ContactItemInterface item : lecturers) {
					Lecturer lecturer = (Lecturer) item;
					if (lecturer.isSelected) {
						post_data.setPersoneMittente(lecturer.key);
						String description = lecturer.email;
						String url = post_data.getUrl().toString();
						if (new Channel(lecturer.id, lecturer.name, url,
								lecturer.thumbnail, description).subscribe()) {
							lecturer.isSelected = false;
						}
					}
				}
				return null;
			}
		});
		task.disableDialog();
		progressDialog.show();

		UIUtils.execute(task, (Void[]) null);
	}

	/**
	 * Called by the ViewPager child fragments to load images via the one
	 * ImageFetcher
	 */
	public ImageFetcher getImageFetcher() {
		return imageFetcher;
	}

	@SuppressLint("NewApi")
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

	class PostData {
		public static final int ALL = -1;
		public static final int NO_ONE = 0;

		private int gi;
		private int mi;
		private int ai;
		private int gf;
		private int mf;
		private int af;
		private int personeMittente;
		private int struttureMittente = NO_ONE;
		private int biblioCRMittente = NO_ONE;
		private int csMittente = NO_ONE;
		private int oiMittente = NO_ONE;

		public PostData() {
			Calendar c = Calendar.getInstance();
			gi = c.get(Calendar.DAY_OF_MONTH);
			mi = c.get(Calendar.MONTH) + 1;
			ai = c.get(Calendar.YEAR);
			gf = gi;
			mf = mi;
			af = ai + 7;
		}

		public void setPersoneMittente(int personeMittente) {
			this.personeMittente = personeMittente;
		}

		public Uri getUrl() {
			return Uri
					.parse(university.domain)
					.buildUpon()
					.path("/")
					.appendQueryParameter("ent", "avviso")
					.appendQueryParameter("rss", "1")
					.appendQueryParameter("dest",
							String.valueOf(university.dest))
					.appendQueryParameter("gi", String.valueOf(gi))
					.appendQueryParameter("mi", String.valueOf(mi))
					.appendQueryParameter("ai", String.valueOf(ai))
					.appendQueryParameter("gf", String.valueOf(gf))
					.appendQueryParameter("mf", String.valueOf(mf))
					.appendQueryParameter("af", String.valueOf(af))
					.appendQueryParameter("personeMittente",
							String.valueOf(personeMittente))
					.appendQueryParameter("struttureMittente",
							String.valueOf(struttureMittente))
					.appendQueryParameter("biblioCRMittente",
							String.valueOf(biblioCRMittente))
					.appendQueryParameter("csMittente",
							String.valueOf(csMittente))
					.appendQueryParameter("oiMittente",
							String.valueOf(oiMittente)).build();
		}
	}

	private Object searchLock = new Object();

	private class SearchListTask extends AsyncTask<String, Void, String> {

		boolean inSearchMode = false;

		@Override
		protected String doInBackground(String... params) {
			filterList.clear();

			String keyword = params[0];

			inSearchMode = (keyword.length() > 0);

			if (inSearchMode) {
				// get all the items matching this
				for (ContactItemInterface item : lecturers) {
					Lecturer contact = (Lecturer) item;

					if ((contact.getItemForIndex()
							.toUpperCase(Locale.getDefault()).indexOf(keyword) > -1)) {
						filterList.add(item);
					}

				}

			}
			return null;
		}

		protected void onPostExecute(String result) {
			if (running) {
				synchronized (searchLock) {

					runOnUiThread(new Runnable() {
						public void run() {
							if (inSearchMode) {
								listView.setInSearchMode(true);
								listView.setItems(filterList);
							} else {
								listView.setInSearchMode(false);
								listView.setItems(lecturers);
							}
						}
					});
				}
			}
		}

		public void cancel() {
			if (getStatus() != AsyncTask.Status.FINISHED) {
				try {
					cancel(true);
				} catch (Exception e) {
					Log.i(TAG, "Fail to cancel running search task");
				}
			}
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (curSearchTask != null) {
			curSearchTask.cancel();
		}
		curSearchTask = new SearchListTask();
		curSearchTask.execute(newText.toUpperCase(Locale.getDefault()));
		return true;
	}

}
