package com.cellasoft.univrapp.activity;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.ActiveList;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.HtmlParser;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.utils.ItemListView;
import com.cellasoft.univrapp.utils.Settings;
import com.cellasoft.univrapp.utils.SynchronizationListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

/**
 * @author Davide Vallicella
 * @version 1.0
 */
public class ItemListActivity extends SherlockListActivity {

	private static final String TAG = ItemListActivity.class.getSimpleName();
	public static final String CHANNEL_ID_PARAM = "ChannelId";

	private Channel channel;
	private ItemListView itemListView;

	private boolean updated = true;

	private SynchronizationListener synchronizationListener = new SynchronizationListener() {
		public void onStart() {
		}

		public void onProgress(String progressText) {
		}

		public void onFinish(final int totalNewItems) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (totalNewItems > 0) {
						loadItems();
					}
				}
			});
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageLoader.initialize(this);

		setContentView(R.layout.item_view);

		if (getIntent().hasExtra(CHANNEL_ID_PARAM)) {
			int channelId = getIntent().getIntExtra(CHANNEL_ID_PARAM, 0);
			channel = new Channel(channelId);
			init();
		}
	}

	@Override
	protected void onStart() {
		if (Constants.DEBUG_MODE)
			Log.d(TAG, "onStart()");
		super.onStart();
		showChannel(channel.id);
	}

	@Override
	protected void onResume() {
		super.onResume();

		SynchronizationManager.getInstance().registerSynchronizationListener(
				synchronizationListener);
	}

	@Override
	protected void onPause() {
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
		initAnimation();
		initBanner();
	}

	private void initAnimation() {
		LayoutAnimationController controller = AnimationUtils
				.loadLayoutAnimation(ItemListActivity.this,
						R.anim.list_layout_controller);
		itemListView.setLayoutAnimation(controller);
	}

	private void initBanner() {
		AdView adView = new AdView(this, AdSize.BANNER, Settings.ID_EDITORE);
		LinearLayout layout = (LinearLayout) findViewById(R.id.footer_banner);
		layout.addView(adView);
		adView.loadAd(new AdRequest().addTestDevice(AdRequest.TEST_EMULATOR));
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.item_menu, menu);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Item item = (Item) l.getItemAtPosition(position);
		showItem(item);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		// case R.id.menu_refresh:
		// refreshItem = item;
		// // Refresh the data
		// refresh();
		// break;
		case R.id.menu_clear:
			cleanList();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onChannelUpdated(Channel channel) {
		this.channel = channel;
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

	private void refresh() {
		final int maxItemsForChannel = Settings.getMaxItemsForChannel();

		BetterAsyncTask<Void, Void, Integer> task = new BetterAsyncTask<Void, Void, Integer>(
				this) {

			@Override
			protected void before(Context context) {
				updated = false;
				// refreshAnim();
			}

			@Override
			protected void after(Context context, Integer items) {
				updated = true;
				itemListView.onRefreshComplete();
				onChannelUpdated(channel);
				Toast.makeText(context, "Find " + items + " news.", Toast.LENGTH_LONG).show();
			}

			@Override
			protected void handleError(Context context, Exception e) {
				updated = true;
				itemListView.onRefreshComplete();
				Toast.makeText(context, "Cannot load the feed.", Toast.LENGTH_LONG).show();
			}
		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, Integer>() {
			@Override
			public Integer call(BetterAsyncTask<Void, Void, Integer> arg0)
					throws Exception {
				return channel.update(maxItemsForChannel);
			}
		});
		task.disableDialog();
		task.execute();
	}

	private void showChannel(final int channelId) {
		BetterAsyncTask<Channel, Void, Channel> task = new BetterAsyncTask<Channel, Void, Channel>(
				this) {

			@Override
			protected void after(Context context, Channel channel) {
				itemListView.setItems(channel.getItems());
				onChannelUpdated(channel);
			}

			@Override
			protected void handleError(Context context, Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "Cannot load the feed.", Toast.LENGTH_LONG).show();
			}
		};
		task.setCallable(new BetterAsyncTaskCallable<Channel, Void, Channel>() {
			@Override
			public Channel call(BetterAsyncTask<Channel, Void, Channel> task)
					throws Exception {
				channel = Channel.findById(channelId,
						ContentManager.LIGHTWEIGHT_CHANNEL_LOADER);
				channel.loadFullItems();
				return channel;
			}
		});
		task.disableDialog();
		task.execute();
	}

	private void loadItems() {
		BetterAsyncTask<Void, Void, ActiveList<Item>> task = new BetterAsyncTask<Void, Void, ActiveList<Item>>(
				this) {
			protected void after(Context context, ActiveList<Item> items) {
				itemListView.setItems(items);
				onChannelUpdated(channel);
			}

			protected void handleError(Context context, Exception e) {
				e.printStackTrace();
			}
		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, ActiveList<Item>>() {
			public ActiveList<Item> call(
					BetterAsyncTask<Void, Void, ActiveList<Item>> task)
					throws Exception {
				channel.loadFullItems();
				return channel.getItems();
			}
		});
		task.disableDialog();
		task.execute();
	}

	private void showItem(final Item item) {
		Intent in = new Intent(getApplicationContext(),
				DisPlayWebPageActivity.class);

		// getting page url
		Toast.makeText(getApplicationContext(), item.title, Toast.LENGTH_SHORT)
				.show();
		in.putExtra("page_url", item.link);
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					String html = HtmlParser.get(item.link);
					for(String file : HtmlParser.getDocuments(html)){
						System.out.println(file);
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
		
		//startActivity(in);
	}

	private void cleanList() {
		channel.clean();
		itemListView.clean();
	}
}
