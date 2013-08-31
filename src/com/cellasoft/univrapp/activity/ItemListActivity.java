package com.cellasoft.univrapp.activity;

import static com.cellasoft.univrapp.utils.LogUtils.LOGD;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.adapter.ItemAdapter.OnItemRequestListener;
import com.cellasoft.univrapp.criteria.LatestItems;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.ActiveList;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.DateUtils;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.UIUtils;
import com.cellasoft.univrapp.widget.ItemListView;
import com.cellasoft.univrapp.widget.SynchronizationListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

/**
 * @author Davide Vallicella
 * @version 1.0
 */

public class ItemListActivity extends BaseListActivity {

	private static final String TAG = makeLogTag(ItemListActivity.class);

	public static final String CHANNEL_ID_PARAM = "ChannelId";
	public static final String CHANNEL_TITLE_PARAM = "ChannelTitle";
	public static final String CHANNEL_THUMB_PARAM = "ChannelThumb";
	public static final String CHANNEL_URL_PARAM = "ChannelUrl";
	public static final String CHANNEL_LECTURER_ID_PARAM = "LecturerId";

	private Channel channel;
	private ItemListView listView;
	private ProgressBar progressBar;

	private boolean loading = false;

	private SynchronizationListener synchronizationListener = new SynchronizationListener() {
		public void onStart(int id) {
		}

		public void onProgress(int id, long updateTime) {
		}

		public void onFinish(final int totalNewItems) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (totalNewItems > 0) {
						if (BuildConfig.DEBUG) {
							LOGD(TAG, "Synchronization Listener, load item");
						}
						loadData();
					}
				}
			});
		}
	};

	private OnItemRequestListener onItemRequestListener = new OnItemRequestListener() {
		public void onRequest(Item lastItem) {
			loadMoreItems(lastItem);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onCreate()");
		}
		super.onCreate(savedInstanceState);

		Application.parents.push(getClass());

		setContentView(R.layout.item_list);

		if (getIntent().hasExtra(CHANNEL_ID_PARAM)) {
			int channelId = getIntent().getIntExtra(CHANNEL_ID_PARAM, 0);
			int lecturerId = getIntent().getIntExtra(CHANNEL_LECTURER_ID_PARAM,
					0);
			String title = getIntent().getStringExtra(CHANNEL_TITLE_PARAM);
			String url = getIntent().getStringExtra(CHANNEL_URL_PARAM);
			String imageUrl = getIntent().getStringExtra(CHANNEL_THUMB_PARAM);
			channel = new Channel(channelId);
			channel.imageUrl = imageUrl;
			channel.url = url;
			channel.title = title;
			channel.lecturerId = lecturerId;
			init();
		}
	}

	@Override
	protected void onResume() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onResume()");
		}
		super.onResume();
		SynchronizationManager.getInstance().registerSynchronizationListener(
				synchronizationListener);
		onChannelUpdated();
	}

	@Override
	protected void onPause() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onPause()");
		}
		super.onPause();
		SynchronizationManager.getInstance().unregisterSynchronizationListener(
				synchronizationListener);
	}

	@Override
	protected void onDestroy() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onDestroy()");
		}
		super.onDestroy();

		channel.clearItems();
		listView.clean();
		listView.clearAnimation();

		channel = null;
		progressBar = null;
		synchronizationListener = null;

		unbindDrawables(listView);
		System.gc();
	}

	private void init() {
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		initActionBar();
		initListView();
		initAnimation();
	}

	@Override
	protected void initActionBar() {
		try {
			getSupportActionBar().setIcon(
					ImageFetcher.getInstance(this).get(channel.imageUrl));
		} catch (Exception e) {
			getSupportActionBar().setIcon(R.drawable.user);
		}

		getSupportActionBar().setTitle(channel.title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void initListView() {
		listView = (ItemListView) getListView();
		// Set a listener to be invoked when the list should be refreshed.
		listView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// Do work to refresh the list here.
				refresh();
			}
		});
		loadData();
	}

	private void initAnimation() {
		LayoutAnimationController controller = AnimationUtils
				.loadLayoutAnimation(ItemListActivity.this,
						R.anim.list_layout_controller);
		controller.getAnimation().reset();

		listView.setLayoutAnimation(controller);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_keep_silence).setVisible(!channel.mute);
		menu.findItem(R.id.menu_up).setVisible(channel.mute);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		new MenuInflater(this).inflate(R.menu.item_menu, menu);

		if (channel.url.equals(Settings.getUniversity().url)) {
			menu.findItem(R.id.menu_contact).setVisible(false);
		}

		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Item item = (Item) l.getItemAtPosition(position);
		markItemAsRead(item);
		showItem(item);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_clear:
			cleanList();
			return true;
		case android.R.id.home:
			// NavUtils.navigateUpFromSameTask(this);
			finish();
			return true;
		case R.id.menu_contact:
			showContact();
			return true;
		case R.id.menu_all_read:
			markAllItemsRead();
			return true;
		case R.id.menu_keep_silence:
			markChannelToMute();
			return true;
		case R.id.menu_up:
			unmarkChannelToMute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onChannelUpdated() {
		String lastUpdate = getResources().getString(
				R.string.last_update_notification).replace(
				"{date}",
				DateUtils.formatTimeMillis(getApplicationContext(),
						channel.updateTime));
		getSupportActionBar().setSubtitle(lastUpdate);

	}

	private void refresh() {
		refresh = true;
		final int maxItemsForChannel = Settings.getMaxItemsForChannel();

		BetterAsyncTask<Void, Void, List<Item>> task = new BetterAsyncTask<Void, Void, List<Item>>(
				this) {

			@Override
			protected void after(Context context, final List<Item> newItems) {
				String size = "0";
				if (newItems != null && newItems.size() > 0) {
					new Runnable() {
						public void run() {
							listView.addItems(newItems);
						}
					}.run();

					size = String.valueOf(newItems.size());
				}
				listView.onRefreshComplete();
				onChannelUpdated();
				String message = getResources().getString(
						R.string.new_items_notification).replace("{total}",
						size);
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				refresh = false;
			}

			@Override
			protected void handleError(Context context, Exception e) {
				listView.onRefreshComplete();
				String message = getResources().getString(
						R.string.not_load_notification);
				Toast.makeText(context, message + "\n" + e.getMessage(),
						Toast.LENGTH_SHORT).show();
				refresh = false;
			}
		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, List<Item>>() {

			@Override
			public List<Item> call(BetterAsyncTask<Void, Void, List<Item>> arg0)
					throws Exception {
				if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
					return channel.update(maxItemsForChannel);
				} else
					throw new UnivrReaderException(getResources().getString(
							R.string.univrapp_connection_exception));
			}
		});
		task.disableDialog();
		if (UIUtils.hasHoneycomb()) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		} else
			task.execute((Void[]) null);
	}

	@Override
	protected void loadData() {
		BetterAsyncTask<Void, Void, ActiveList<Item>> task = new BetterAsyncTask<Void, Void, ActiveList<Item>>(
				this) {

			@Override
			protected void before(Context context) {
				listView.clean();
			}

			protected void after(Context context, final ActiveList<Item> items) {
				listView.setItemRequestListener(onItemRequestListener);
				listView.setItems(items);
				listView.startLayoutAnimation();

				if (items.size() == Config.MAX_ITEMS)
					listView.addFooterView();
				else
					listView.removeFooterView();

				onChannelUpdated();
			}

			protected void handleError(Context context, Exception e) {
				e.printStackTrace();
				String message = getResources().getString(
						R.string.not_load_notification);
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, ActiveList<Item>>() {
			public ActiveList<Item> call(
					BetterAsyncTask<Void, Void, ActiveList<Item>> task)
					throws Exception {

				List<Item> items = ContentManager.loadItems(new LatestItems(
						channel.id), ContentManager.FULL_ITEM_LOADER,
						ContentManager.LIGHTWEIGHT_CHANNEL_LOADER);

				ActiveList<Item> result = new ActiveList<Item>();
				result.addAll(items);
				return result;
			}
		});
		task.disableDialog();
		if (UIUtils.hasHoneycomb()) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		} else
			task.execute((Void[]) null);
	}

	protected void loadMoreItems(final Item lastItem) {
		if (loading) {
			return;
		}

		loading = true;

		BetterAsyncTask<Void, Void, List<Item>> loadMoreItemsTask = new BetterAsyncTask<Void, Void, List<Item>>(
				this) {
			protected void after(Context context, final List<Item> items) {
				listView.addItems(items);

				if (items.size() < Config.MAX_ITEMS
						|| listView.getCount() >= Settings
								.getMaxItemsForChannel()) {

					listView.removeFooterView();
					listView.setItemRequestListener(null);
				}

				loading = false;
			}

			protected void handleError(Context context, Exception e) {
				e.printStackTrace();
				loading = false;
			}
		};
		loadMoreItemsTask
				.setCallable(new BetterAsyncTaskCallable<Void, Void, List<Item>>() {
					public List<Item> call(
							BetterAsyncTask<Void, Void, List<Item>> task)
							throws Exception {
						return ContentManager.loadItems(new LatestItems(
								channel.id, lastItem, LatestItems.OLDER,
								Config.MAX_ITEMS),
								ContentManager.FULL_ITEM_LOADER,
								ContentManager.LIGHTWEIGHT_CHANNEL_LOADER);
					}
				});
		loadMoreItemsTask.disableDialog();

		if (UIUtils.hasHoneycomb()) {
			loadMoreItemsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		} else
			loadMoreItemsTask.execute((Void[]) null);
	}

	private void markChannelToMute() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				if (channel != null) {
					channel.markChannelToMute();
				}
				return null;
			}

		}.execute((Void[]) null);
	}

	private void unmarkChannelToMute() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				if (channel != null) {
					channel.unmarkChannelToMute();
				}
				return null;
			}

		}.execute((Void[]) null);
	}

	private void markAllItemsRead() {
		new AsyncTask<Void, Void, Void>() {

			protected void onPostExecute(Void result) {
				loadData();
			};

			@Override
			protected Void doInBackground(Void... params) {
				if (channel != null) {
					ContentManager.markAllItemsOfChannelAsRead(channel);
				}
				return null;
			}
		}.execute((Void[]) null);

	}

	private void showItem(final Item item) {
		Intent in = new Intent(getApplicationContext(),
				DisPlayWebPageActivity.class);

		// getting page url
		Toast.makeText(getApplicationContext(), item.title, Toast.LENGTH_SHORT)
				.show();
		in.putExtra("page_url", item.link);
		in.putExtra(DisPlayWebPageActivity.ITEM_ID_PARAM, item.id);
		startActivity(in);
	}

	private void showContact() {
		new AsyncTask<Void, Void, Lecturer>() {

			protected void onPostExecute(Lecturer lecturer) {
				Intent intent = new Intent(ItemListActivity.this,
						ContactActivity.class);
				intent.putExtra(ContactActivity.LECTURER_ID_PARAM, lecturer.id);
				intent.putExtra(ContactActivity.LECTURER_NAME_PARAM,
						lecturer.name);
				intent.putExtra(ContactActivity.LECTURER_OFFICE_PARAM,
						lecturer.office);
				intent.putExtra(ContactActivity.LECTURER_THUMB_PARAM,
						lecturer.thumbnail);
				startActivity(intent);
			};

			@Override
			protected Lecturer doInBackground(Void... params) {
				return ContentManager.loadLecturer(channel.lecturerId,
						ContentManager.FULL_LECTURER_LOADER);

			}

		}.execute((Void[]) null);

	}

	private void markItemAsRead(Item item) {
		if (!item.isRead()) {
			new AsyncTask<Item, Void, Void>() {

				@Override
				protected Void doInBackground(Item... item) {
					item[0].markItemAsRead();
					return null;
				}
			}.execute(item);
		}
	}

	private void cleanList() {
		if (refresh)
			return;

		refresh = true;
		new AsyncTask<Void, Void, Integer>() {

			protected void onPreExecute() {
				progressBar.setVisibility(View.VISIBLE);
				listView.setEnabled(false);
				listView.setClickable(false);
			};

			protected void onPostExecute(Integer delettedItems) {
				progressBar.setVisibility(View.GONE);
				listView.setEnabled(true);
				listView.setClickable(true);
				listView.clean();
				Toast.makeText(
						ItemListActivity.this,
						getResources().getString(R.string.clean).replace(
								"{total}", String.valueOf(delettedItems)),
						Toast.LENGTH_SHORT).show();
				refresh = false;
			};

			@Override
			protected Integer doInBackground(Void... params) {
				return channel.clean();
			}
		}.execute((Void[]) null);

	}
}
