package com.cellasoft.univrapp.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.service.DownloadingService;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.FileCache;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.ImageCache;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.widget.ChannelListView;
import com.cellasoft.univrapp.widget.ChannelView;
import com.cellasoft.univrapp.widget.OnChannelViewListener;
import com.cellasoft.univrapp.widget.SynchronizationListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

public class ChannelListActivity extends SherlockListActivity {

	private static final String TAG = ChannelListActivity.class.getSimpleName();

	private static final int FIRST_TIME = 1;
	private static final int SUBSCRIBE = 2;

	private ArrayList<Channel> channels = null;
	private ChannelListView channelListView;
	private AdView adView;

	private SynchronizationListener synchronizationListener = new SynchronizationListener() {
		public void onStart() {
		}

		public void onProgress(String progressText) {
		}

		public void onFinish(final int totalNewItems) {

			if (totalNewItems > 0) {
				if (Constants.DEBUG_MODE)
					Log.d(TAG, "Synchronization Listener, refresh");
				refreshUnreadCounts();
			}

		}
	};

	private OnChannelViewListener channelListener = new OnChannelViewListener() {

		@Override
		public void onSelected(ChannelView view, boolean selected) {
			final int position = getListView().getPositionForView(view);
			if (position != ListView.INVALID_POSITION) {
				channels.get(position).isSelected = selected;
			}
		}

		@Override
		public void onStarred(ChannelView view, boolean starred) {
			final int position = getListView().getPositionForView(view);
			if (position != ListView.INVALID_POSITION) {
				if (starred)
					channels.get(position).markChannelToStarred();
				else
					channels.get(position).unmarkChannelToStarred();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.DEBUG_MODE)
			Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		ImageLoader.initialize(this);
		setContentView(R.layout.channel_view);
		getSupportActionBar().setTitle(
				getResources().getString(R.string.channel_title));
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
	protected void onResume() {
		super.onResume();
		SynchronizationManager.getInstance().registerSynchronizationListener(
				synchronizationListener);
		showAdmodBanner();
	}

	@Override
	protected void onPause() {
		super.onPause();
		SynchronizationManager.getInstance().unregisterSynchronizationListener(
				synchronizationListener);
	}

	private void init() {
		startServices();
		if (Settings.getFirstTime()) {
			onFirstTime();
		}

		cancelNotification();
		// Init GUI
		channelListView = (ChannelListView) getListView();
		channelListView.setChannelViewlistener(channelListener);
		// Add the footer before adding the adapter, else the footer will not
		// load!
		initBanner();
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
				while (adView.getHeight() == 0 && !isCancelled()) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						cancel(true);
					}
				}

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

	private void onFirstTime() {
		FileCache.clearCacheFolder();
		Intent intent = new Intent(this, ChooseMainFeedActivity.class);
		startActivityForResult(intent, FIRST_TIME);
	}

	private void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(Constants.NOTIFICATION_ID);
	}

	private void startServices() {
		if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
			if (Constants.DEBUG_MODE)
				Log.d(Constants.LOG_TAG, "Begin startServices " + new Date());
			Intent service = new Intent(this, SynchronizationService.class);
			startService(service);

			Intent downloadService = new Intent(this, DownloadingService.class);
			startService(downloadService);
			if (Constants.DEBUG_MODE)
				Log.d(Constants.LOG_TAG, "End startServices " + new Date());
		}
	}

	private void stopServices() {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "Begin startServices " + new Date());
		Intent service = new Intent(this, SynchronizationService.class);
		stopService(service);

		Intent downloadService = new Intent(this, DownloadingService.class);
		stopService(downloadService);
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "End startServices " + new Date());

	}

	private void loadData() {
		loadChannels();
		channelListView.setChannels(channels);
		refreshUnreadCounts();
	}

	private void loadChannels() {
		channels = ContentManager
				.loadAllChannels(ContentManager.FULL_CHANNEL_LOADER);
		if (channels == null || channels.isEmpty()) {
			channels = new ArrayList<Channel>();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Channel channel = channels.get(position);
		showChannel(channel);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_subscribe:
			showSubscriptions();
			return true;
		case R.id.menu_unsubscribe:
			confirmDeleteChannel();
			return true;
		case R.id.menu_reload:
			refreshAllChannels();
			return true;
		case R.id.menu_selectAll:
			selectAll();
			return true;
		case R.id.menu_settings:
			showSettings();
			return true;
		case R.id.menu_reset:
			confirmReset();
			return true;
		case R.id.menu_about:
			showAboutScreen();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.channel_menu, menu);
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FIRST_TIME) {
			if (resultCode == RESULT_OK) {
				Settings.saveFirstTime();
				infoDialog();
			}
			if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
	};

	private void selectAll() {
		for (int i = 1; i < channels.size(); i++) {
			channels.get(i).isSelected = true;
		}
		channelListView.refresh();
	}

	private void showAboutScreen() {
		Intent intent = new Intent(this, AboutScreen.class);
		startActivity(intent);
	}

	private void showSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	private void showSubscriptions() {
		Intent intent = new Intent(this, SubscribeActivity.class);
		startActivityForResult(intent, SUBSCRIBE);
	}

	private void showChannel(Channel channel) {
		Intent intent = new Intent(this, ItemListActivity.class);
		intent.putExtra(ItemListActivity.CHANNEL_ID_PARAM, channel.id);
		startActivity(intent);
	}

	private void confirmDeleteChannel() {
		String confirmMessage = "The selected channels will be removed, continue?";
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Unsubscribe channels")
				.setMessage(confirmMessage)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								deleteChannel();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

	private void infoDialog() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View addView = inflater.inflate(R.layout.info_dialog, null);
		String infoMessage = "Ancora pochi passi e ci siamo ;)";
		AlertDialog dialog = new AlertDialog.Builder(ChannelListActivity.this)
				.setView(addView).setTitle("Benvenuto").setMessage(infoMessage)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).create();

		dialog.show();

	}

	private void confirmReset() {
		String confirmMessage = "Reset all channels and settings, continue?";
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Reset")
				.setMessage(confirmMessage)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								reset();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

	private void deleteChannel() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Unsubscribe selected channels");
		AsyncTask<Void, Void, Void> unsubscribingTask = new AsyncTask<Void, Void, Void>() {

			@SuppressLint("ShowToast")
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				progressDialog.dismiss();

				String message = "Successfull";
				Toast.makeText(ChannelListActivity.this, message, 1000).show();

				loadData();
			}

			@Override
			protected Void doInBackground(Void... params) {
				for (Channel channel : channels) {
					if (channel.isSelected) {
						ContentManager.unsubscribe(channel);
					}
				}

				return null;
			}
		};

		progressDialog.show();
		unsubscribingTask.execute();
	}

	private void reset() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Reset");
		AsyncTask<Void, Void, Void> unsubscribingTask = new AsyncTask<Void, Void, Void>() {

			@SuppressLint("ShowToast")
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				progressDialog.dismiss();

				String message = "Successfull";
				Toast.makeText(ChannelListActivity.this, message, 1000).show();

				startServices();
				Settings.setFirstTime(true);
				onFirstTime();
			}

			@Override
			protected Void doInBackground(Void... params) {
				stopServices();
				cancelNotification();

				for (Channel channel : channels) {
					channel.delete();
				}

				ContentManager.deleteAllLecturers();
				ContentManager.deleteAllImages();

				runOnUiThread(new Runnable() {
					public void run() {
						channels.clear();
						channelListView.setChannels(channels);
					}
				});

				ImageCache.getInstance().clear();

				return null;
			}
		};

		progressDialog.show();
		unsubscribingTask.execute();
	}

	private void refreshUnreadCounts() {
		BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
				this) {

			@Override
			protected void after(Context arg0, Void arg1) {
				channelListView.refresh();
			}

			protected void handleError(Context context, Exception e) {
				e.printStackTrace();
				String message = getResources().getString(
						R.string.not_load_notification);
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}

		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
			public Void call(BetterAsyncTask<Void, Void, Void> task)
					throws Exception {

				Map<Integer, Integer> unreadCounts = ContentManager
						.countUnreadItemsForEachChannel();
				for (Channel channel : channels) {
					try {
						if (unreadCounts.containsKey(channel.id)) {
							channel.setUnreadItems(unreadCounts.get(channel.id));
						} else {
							channel.setUnreadItems(0);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("ERROR", e.getMessage());
					}
				}

				return null;
			}
		});
		task.disableDialog();
		task.execute();
	}

	private void refreshAllChannels() {
		BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
				this) {
			
			protected void handleError(Context context, Exception e) {
				e.printStackTrace();
				String message = getResources().getString(
						R.string.not_load_notification);
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}

			@Override
			protected void after(Context arg0, Void arg1) {	
			}

		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
			public Void call(BetterAsyncTask<Void, Void, Void> task)
					throws Exception {
				SynchronizationManager.getInstance().startSynchronizing();
				return null;
			}
		});
		task.disableDialog();
		task.execute();
		
	}
}
