package com.cellasoft.univrapp.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.adapter.ItemAdapter;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.manager.DialogManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.RSSItem;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.Settings;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;


/**
 * {@link ItemListActivity} � un Feed Reader dedicato alla Universit&agrave;
 * di Verona (Facolt&agrave; di Scienze Matematiche Fisiche e Naturali).
 * 
 * @author Davide Vallicella
 * @version 1.0
 */
public class ItemListActivity extends SherlockListActivity {

	private static final String TAG = ItemListActivity.class.getSimpleName();
	public static final String CHANNEL_TITLE_PARAM = "ChannelTitle";
	public static final String CHANNEL_ID_PARAM = "ChannelId";
	public static final String STRINGS_KEY = "strings";

	private Channel channel;
	private String channelName;
	private ItemAdapter rssAdapter;
	private DialogManager dialogManager;

	private boolean loadingMore = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (savedInstanceState != null) {
		} else {
			int channelId = getIntent().getIntExtra(CHANNEL_ID_PARAM, 0);
			channel = new Channel(channelId);
		}

		init();
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.DEBUG_MODE)
				Log.d(Constants.LOG_TAG, "Messaggio ricevuto!");

			Bundle b = intent.getExtras();
			if (b.containsKey("action"))
				switch (b.getInt("action")) {
				case Constants.UPDATE_UI:
					runOnUiThread(showItems);
					break;
				case Constants.ERROR_MESSAGE:
					if (b.containsKey("message"))
						showErrorMessage(b.getString("message"));
					break;
				default:
					break;
				}

			getWindow().getDecorView().postDelayed(new Runnable() {
				@Override
				public void run() {
					refreshAnimEnd();
				}
			}, 1000);
		}
	};

	private Handler rssHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.UPDATE_UI:
				rssAdapter.notifyDataSetChanged();
				break;
			case Constants.ERROR_MESSAGE:
				showDialog(DialogManager.ERROR_DIALOG_ID);
				break;
			default:
				break;
			}
		}
	};

	private void init() {
		dialogManager = DialogManager.getInstance(ItemListActivity.this);
		// Init GUI
		initListSelector();
		initListScrollListener();
		initListAdapter();
	//	initBanner();
	}

	private void initListAdapter() {
		rssAdapter = new ItemAdapter(ItemListActivity.this);
		getListView().setAdapter(rssAdapter);
	}

	private void initListSelector() {
		getListView().setSelector(R.drawable.list_selector_on_top);
		getListView().setDrawSelectorOnTop(true);
		getListView().invalidateViews();
		LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(ItemListActivity.this, R.anim.list_layout_controller);
		getListView().setLayoutAnimation(controller);
	}

	private void initListScrollListener() {
		getListView().setOnScrollListener(new OnScrollListener() {

			// useless here, skip!
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				// what is the bottom item that is visible
				int lastInScreen = firstVisibleItem + visibleItemCount;

				// is the bottom item visible & not loading more already ? Load
				// more !
				if ((lastInScreen == totalItemCount) && !(loadingMore)) {
					loadingMore = true;
					runOnUiThread(dinamicShowItems);
				}
			}
		});
	}

	private void initBanner() {
		AdView adView = new AdView(this, AdSize.BANNER, Settings.ID_EDITORE);
		LinearLayout layout = (LinearLayout) findViewById(R.id.footer_banner);
		layout.addView(adView);
		adView.loadAd(new AdRequest().addTestDevice(AdRequest.TEST_EMULATOR));
	}

	private void showErrorMessage(String msgError) {
		dialogManager.setErrorMessage(msgError);
		rssHandler.sendEmptyMessage(Constants.ERROR_MESSAGE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DialogManager.ERROR_DIALOG_ID:
			return dialogManager.getAllertDialog();
		case DialogManager.PROGRESS_DIALOG_ID:
			return dialogManager.getProgressDialog();
		case DialogManager.SUBSCRIBE_DIALOG_ID:
			return dialogManager.getSubscribeDialog();
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.item_menu, menu);
		return true;
	}

	View refreshView;
	ImageView refreshImage;
	com.actionbarsherlock.view.MenuItem refreshItem;
	Animation rotateClockwise;

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refreshItem = item;
			refreshAnim();
			// Refresh the data
			startSyncProcess();
			break;
		case R.id.menu_clear:
			ContentManager.cleanChannel(channel, 3);
			loadData();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshAnim() {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// The inflated layout and loaded animation are put into members to
		// avoid reloading them every time.
		// For convenience, the ImageView is also extracted into a member
		if (refreshView == null || rotateClockwise == null) {
			refreshView = inflater.inflate(R.layout.refresh_actionview, null);
			rotateClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate);
			refreshImage = (ImageView) refreshView
					.findViewById(R.id.actionbar_progress_image);
		}
		// reset some stuff - make the animation infinite again,
		// and make the containing view visible
		rotateClockwise.setRepeatCount(Animation.INFINITE);
		refreshView.setVisibility(View.VISIBLE);
		rotateClockwise.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// This is important to avoid the overlapping problem.
				// First hide the animated icon
				// setActionView(null) does NOT hide it!
				// as long as the animation is running, it will be visible
				// hiding an animated view also doesn't work
				// as long as the animation is running
				// but hiding the parent does.
				refreshView.setVisibility(View.GONE);
				// make the static button appear again
				refreshItem.setActionView(null);
			}
		});
		refreshItem.setActionView(refreshView);
		// everything is set up, start animating.
		refreshImage.startAnimation(rotateClockwise);
	}

	private void refreshAnimEnd() {
		// sanity
		if (refreshImage == null || refreshItem == null) {
			return;
		}
		Animation anim = refreshImage.getAnimation();

		// more sanity
		if (anim != null) {
			// let the animation finish nicely
			anim.setRepeatCount(0);
		} else {
			// onAnimationEnd won't run in this case, so restore the static
			// button
			refreshItem.setActionView(null);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		RSSItem item = (RSSItem) rssAdapter.getItem(position);

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()
				.toString()));
		startActivity(intent);
	}

	private void startSyncProcess() {
		Intent service = new Intent(ItemListActivity.this,
				SynchronizationService.class);
		service.putExtra("channel", channel.id);
		startService(service);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		loadData();
	}

	private void loadData(){
		channel.loadFullItems();
		dinamicShowItems.run();
	}
	
	@Override
	protected void onPause() {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "onPause()");
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				broadcastReceiver);
	}

	@Override
	protected void onResume() {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "onResume()");
		super.onResume();
		IntentFilter inf = new IntentFilter(SynchronizationService.ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				broadcastReceiver, inf);
		rssHandler.sendEmptyMessage(Constants.UPDATE_UI);
	}

	@Override
	public void onDestroy() {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "onDestroy()");
		super.onDestroy();
		Intent service = new Intent(ItemListActivity.this,
				SynchronizationService.class);
		stopService(service);
	}

	private Runnable showItems = new Runnable() {
		@Override
		public void run() {
			channel.loadFullItems();
			rssAdapter.setItems(channel.getItems());
		}
	};

	private Runnable dinamicShowItems = new Runnable() {
		@Override
		public void run() {
			int start = rssAdapter.getCount();
			int end = Math.min(start + 10, channel.size());
			rssAdapter.addItems(channel.getItems().subList(start, end));
			loadingMore = false;
		}
	};
}
