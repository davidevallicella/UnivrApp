package com.cellasoft.univrapp.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.adapter.ItemAdapter.OnItemRequestListener;
import com.cellasoft.univrapp.criteria.LatestItems;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.ActiveList;
import com.cellasoft.univrapp.utils.DateUtils;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.widget.ItemListView;
import com.cellasoft.univrapp.widget.SynchronizationListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

/**
 * @author Davide Vallicella
 * @version 1.0
 */
@SuppressLint("NewApi")
public class ItemListActivity extends SherlockListActivity {

	private static final String TAG = ItemListActivity.class.getSimpleName();
	public static final String CHANNEL_ID_PARAM = "ChannelId";

	private Channel channel;
	private ItemListView itemListView;
	private AdView adView;

	private boolean loading = false;
	private boolean refresh = false;

	private SynchronizationListener synchronizationListener = new SynchronizationListener() {
		public void onStart() {
		}

		public void onProgress(String progressText) {
		}

		public void onFinish(final int totalNewItems) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (totalNewItems > 0) {
						if (Constants.DEBUG_MODE)
							Log.d(TAG, "Synchronization Listener, load item");
						loadItems();
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
		super.onCreate(savedInstanceState);
		ImageLoader.initialize(this);

		setContentView(R.layout.item_view);

		if (getIntent().hasExtra(CHANNEL_ID_PARAM)) {
			int channelId = getIntent().getIntExtra(CHANNEL_ID_PARAM, 0);
			channel = Channel.findById(channelId,
					ContentManager.FULL_CHANNEL_LOADER);
			init();
		}

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			System.out.println("----- ENABLE");
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}
	}
	
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		if (Constants.DEBUG_MODE)
			Log.d(TAG, "onStart()");
		super.onStart();
		loadItems();
	}

	@Override
	protected void onResume() {
		if (Constants.DEBUG_MODE)
			Log.d(TAG, "onResume()");
		super.onResume();
		SynchronizationManager.getInstance().registerSynchronizationListener(
				synchronizationListener);
		onChannelUpdated();
		showAdmodBanner();
	}

	@Override
	protected void onPause() {
		if (Constants.DEBUG_MODE)
			Log.d(TAG, "onPause()");
		super.onPause();
		SynchronizationManager.getInstance().unregisterSynchronizationListener(
				synchronizationListener);
	}

	@Override
	public void onDestroy() {
		if (Constants.DEBUG_MODE)
			Log.d(TAG, "onDestroy()");
		super.onDestroy();
		Intent service = new Intent(ItemListActivity.this,
				SynchronizationService.class);
		stopService(service);
	}

	private void init() {
		// Init GUI
		itemListView = (ItemListView) getListView();
		// Set a listener to be invoked when the list should be refreshed.
		itemListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// Do work to refresh the list here.
				refresh();
			}
		});

		if (!channel.url.equals(Settings.getUniversity().url)) {

			getSupportActionBar().setIcon(
					new BitmapDrawable(getResources(),
							imageLoader(channel.imageUrl)));
		} else {
			getSupportActionBar().setIcon(
					Settings.getUniversity().logo_from_resource);
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(channel.title);

		initAnimation();
		initBanner();
	}

	private Bitmap imageLoader(String imageUrl) {
		if (imageUrl != null && imageUrl.length() > 0) {
			return ImageLoader.get(imageUrl);
		}
		// default image
		return BitmapFactory.decodeResource(getResources(), R.drawable.thumb);
	}

	private void initAnimation() {
		LayoutAnimationController controller = AnimationUtils
				.loadLayoutAnimation(ItemListActivity.this,
						R.anim.list_layout_controller);
		controller.getAnimation().reset();

		itemListView.setLayoutAnimation(controller);
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

	private void showAdmodBanner() {
		if (adView != null && closeAdmodButton != null) {
			adView.setVisibility(View.VISIBLE);
			closeAdmodButton.setVisibility(View.VISIBLE);
		}
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_keep_silence).setVisible(!channel.mute);
		menu.findItem(R.id.menu_up).setVisible(channel.mute);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		getSupportMenuInflater().inflate(R.menu.item_menu, menu);
		if (channel.url.equals(Settings.getUniversity().url)) {
			menu.findItem(R.id.menu_contact).setVisible(false);
		}
		return true;
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
			if (!refresh)
				cleanList();
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_contact:
			showContact();
			return true;
		case R.id.menu_all_read:
			markAllItemsRead();
			return true;
		case R.id.menu_keep_silence:
			channel.markChannelToMute();
			return true;
		case R.id.menu_up:
			channel.unmarkChannelToMute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onChannelUpdated() {
		String lastUpdate = getResources().getString(
				R.string.last_update_notification).replace("{date}",
				DateUtils.formatTimeMillis(channel.updateTime));
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
							itemListView.addItems(newItems);
							itemListView.startLayoutAnimation();
						}
					}.run();

					size = String.valueOf(newItems.size());
				}
				itemListView.onRefreshComplete();
				onChannelUpdated();
				String message = getResources().getString(
						R.string.new_items_notification).replace("{total}",
						size);
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				refresh = false;
			}

			@Override
			protected void handleError(Context context, Exception e) {
				itemListView.onRefreshComplete();
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
		task.execute();
	}

	private void loadItems() {
		BetterAsyncTask<Void, Void, ActiveList<Item>> task = new BetterAsyncTask<Void, Void, ActiveList<Item>>(
				this) {

			@Override
			protected void before(Context context) {
				itemListView.clean();
			}

			protected void after(Context context, final ActiveList<Item> items) {

				itemListView.setItemRequestListener(onItemRequestListener);
				itemListView.setItems(items);
				itemListView.startLayoutAnimation();

				if (items.size() == Constants.MAX_ITEMS)
					itemListView.addFooterView();
				else
					itemListView.removeFooterView();
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
		task.execute();
	}

	protected void loadMoreItems(final Item lastItem) {
		if (loading) {
			return;
		}

		loading = true;

		BetterAsyncTask<Void, Void, List<Item>> loadMoreItemsTask = new BetterAsyncTask<Void, Void, List<Item>>(
				this) {
			protected void after(Context context, final List<Item> items) {
				itemListView.addItems(items);

				if (items.size() < Constants.MAX_ITEMS
						|| itemListView.getCount() >= Settings
								.getMaxItemsForChannel()) {

					itemListView.removeFooterView();
					itemListView.setItemRequestListener(null);
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
								Constants.MAX_ITEMS),
								ContentManager.FULL_ITEM_LOADER,
								ContentManager.LIGHTWEIGHT_CHANNEL_LOADER);
					}
				});
		loadMoreItemsTask.disableDialog();
		loadMoreItemsTask.execute();
	}

	private void markAllItemsRead() {
		ContentManager.markAllItemsOfChannelAsRead(channel);
		loadItems();
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
		Intent intent = new Intent(this, ContactActivity.class);

		Lecturer lecturer = ContentManager.loadLecturer(channel.lecturerId,
				ContentManager.FULL_LECTURER_LOADER);

		intent.putExtra(ContactActivity.LECTURER_ID_PARAM, lecturer.id);
		intent.putExtra(ContactActivity.LECTURER_NAME_PARAM, lecturer.name);
		intent.putExtra(ContactActivity.LECTURER_OFFICE_PARAM, lecturer.office);
		intent.putExtra(ContactActivity.LECTURER_THUMB_PARAM,
				lecturer.thumbnail);
		startActivity(intent);
	}

	private void markItemAsRead(Item item) {
		if (!item.isRead()) {
			item.markItemAsRead();
		}
	}

	private void cleanList() {
		int deletted = channel.clean();
		itemListView.clean();
		Toast.makeText(
				this,
				getResources().getString(R.string.clean).replace("{total}",
						String.valueOf(deletted)), Toast.LENGTH_SHORT).show();

	}
}
