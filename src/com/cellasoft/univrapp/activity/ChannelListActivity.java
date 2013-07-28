package com.cellasoft.univrapp.activity;

import static com.cellasoft.univrapp.Config.GCM_SENDER_ID;
import static com.cellasoft.univrapp.utils.LogUtils.LOGD;
import static com.cellasoft.univrapp.utils.LogUtils.LOGE;
import static com.cellasoft.univrapp.utils.LogUtils.LOGI;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

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
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.gcm.ServerUtilities;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.ClosableAdView;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.UIUtils;
import com.cellasoft.univrapp.widget.ChannelListView;
import com.cellasoft.univrapp.widget.ChannelView;
import com.cellasoft.univrapp.widget.OnChannelViewListener;
import com.cellasoft.univrapp.widget.SynchronizationListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;
import com.google.android.gcm.GCMRegistrar;

public class ChannelListActivity extends SherlockListActivity {

	private static final String TAG = makeLogTag(ChannelListActivity.class);

	private static final int FIRST_TIME = 1;

	private ArrayList<Channel> channels = null;
	private ChannelListView channelListView;
	private ClosableAdView adView;
	private ImageFetcher imageFetcher;

	private AsyncTask<Void, Void, Void> gcmRegisterTask;

	private SynchronizationListener synchronizationListener = new SynchronizationListener() {
		public void onStart(int id) {
			if (channels == null)
				return;
			for (Channel channel : channels) {
				if (channel.id == id) {
					channel.updating = true;
					channelListView.refresh();
					break;
				}
			}
		}

		public void onProgress(int id, long updateTime) {
			if (channels == null)
				return;
			for (Channel channel : channels) {
				if (channel.id == id) {
					channel.updating = false;
					channel.updateTime = updateTime;
					channelListView.refresh();
					break;
				}
			}
		}

		public void onFinish(int totalNewItems) {
			if (totalNewItems > 0) {
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
				enableChannelUpdating(position, starred);
			}
		}
	};

	private void enableChannelUpdating(final int position, boolean value) {
		new AsyncTask<Boolean, Void, Void>() {

			protected void onPostExecute(Void result) {
				channelListView.refresh();
			};

			@Override
			protected Void doInBackground(Boolean... isStarred) {
				if (isStarred[0])
					channels.get(position).markChannelToStarred();
				else
					channels.get(position).unmarkChannelToStarred();
				return null;
			}

		}.execute(value);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Config.DEBUG_MODE) {
			LOGD(TAG, "onCreate()");
			UIUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);

		if (isFinishing()) {
			return;
		}

		UIUtils.enableDisableActivities(this);

		registerGCMClient();

		imageFetcher = new ImageFetcher(this);
		setContentView(R.layout.channel_view);
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
		Application.parents.clear();
		loadData();
	}

	@Override
	protected void onDestroy() {
		if (gcmRegisterTask != null) {
			gcmRegisterTask.cancel(true);
		}
		try {
			GCMRegistrar.onDestroy(getApplicationContext());
		} catch (Exception e) {
			LOGE("UnRegister Receiver Error", "> " + e.getMessage());
		}

		if (adView != null) {
			adView.hideAd();
		}

		imageFetcher.closeCache();

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		SynchronizationManager.getInstance().registerSynchronizationListener(
				synchronizationListener);
		imageFetcher.setExitTasksEarly(false);
		channelListView.refresh();
		showAdmodBanner();
	}

	@Override
	protected void onPause() {
		super.onPause();
		imageFetcher.setPauseWork(false);
		imageFetcher.setExitTasksEarly(true);
		imageFetcher.flushCache();
		SynchronizationManager.getInstance().unregisterSynchronizationListener(
				synchronizationListener);
	}

	private void showAdmodBanner() {
		if (adView != null) {
			adView.viewAd();
		}
	}

	private void init() {
		startServices();
		if (Settings.getFirstTime()) {
			onFirstTime();
		}

		cancelNotification();
		getSupportActionBar().setTitle(
				getResources().getString(R.string.channel_title));

		// Init GUI
		channelListView = (ChannelListView) getListView();
		channelListView.setChannelViewlistener(channelListener);
		channelListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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

		// Add the footer before adding the adapter, else the footer will not
		// load!
		initBanner();
	}

	private void initBanner() {
		// Look up the AdView as a resource and load a request.
		adView = (ClosableAdView) this.findViewById(R.id.adView);
		adView.inizialize();
		adView.loadAd();
	}

	private void registerGCMClient() {
		GCMRegistrar.checkDevice(this);

		if (Config.DEBUG_MODE) {
			GCMRegistrar.checkManifest(this);
		}

		final String regId = GCMRegistrar.getRegistrationId(this);

		if (TextUtils.isEmpty(regId)) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, GCM_SENDER_ID);

		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration
				LOGI(TAG, "Already registered on the GCM server");

			} else {
				// Try to register again, but not on the UI thread.
				// It's also necessary to cancel the task in onDestroy().
				gcmRegisterTask = new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						boolean registered = ServerUtilities.register(
								getApplicationContext(),
								Settings.getUniversity().name, regId);
						if (!registered) {
							GCMRegistrar.unregister(getApplicationContext());
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						gcmRegisterTask = null;
					}
				};
				gcmRegisterTask.execute(null, null, null);
			}
		}
	}

	/**
	 * Called by the ViewPager child fragments to load images via the one
	 * ImageFetcher
	 */
	public ImageFetcher getImageFetcher() {
		return imageFetcher;
	}

	private void onFirstTime() {
		Intent intent = new Intent(this, ChooseMainFeedActivity.class);
		startActivityForResult(intent, FIRST_TIME);
	}

	private void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(Config.NOTIFICATION_ID);
	}

	private void startServices() {
		if (Config.DEBUG_MODE)
			LOGD(TAG, "Begin startServices " + new Date());
		Intent service = new Intent(this, SynchronizationService.class);
		startService(service);
	}

	private void stopServices() {
		if (Config.DEBUG_MODE)
			LOGD(TAG, "Begin startServices " + new Date());
		Intent service = new Intent(this, SynchronizationService.class);
		stopService(service);
	}

	private void loadData() {
		loadChannels();
	}

	private void loadChannels() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				channelListView.setChannels(channels);
				refreshUnreadCounts();
			}

			@Override
			protected Void doInBackground(Void... params) {
				channels = ContentManager
						.loadAllChannels(ContentManager.FULL_CHANNEL_LOADER);
				if (channels == null || channels.isEmpty()) {
					channels = new ArrayList<Channel>();
				}
				return null;
			}
		}.execute();

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
				saveFirstTime();
			}
			if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
	};

	private void saveFirstTime() {
		new AsyncTask<Void, Void, Void>() {

			protected void onPostExecute(Void result) {
				infoDialog();
			};

			@Override
			protected Void doInBackground(Void... params) {
				Settings.saveFirstTime();
				return null;
			}

		}.execute((Void[]) null);
	}

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
		startActivity(intent);
	}

	private void showChannel(Channel channel) {
		Intent intent = new Intent(this, ItemListActivity.class);
		intent.putExtra(ItemListActivity.CHANNEL_ID_PARAM, channel.id);
		intent.putExtra(ItemListActivity.CHANNEL_LECTURER_ID_PARAM,
				channel.lecturerId);
		intent.putExtra(ItemListActivity.CHANNEL_TITLE_PARAM, channel.title);
		intent.putExtra(ItemListActivity.CHANNEL_URL_PARAM, channel.url);
		intent.putExtra(ItemListActivity.CHANNEL_THUMB_PARAM, channel.imageUrl);
		startActivity(intent);
	}

	private void confirmDeleteChannel() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isFinishing()) {
					Resources res = getResources();
					String confirmMessage = res
							.getString(R.string.unsub_channel_dialog);
					AlertDialog dialog = new AlertDialog.Builder(
							ChannelListActivity.this)
							.setTitle(
									res.getString(R.string.unsub_channel_dialog_title))
							.setMessage(confirmMessage)
							.setPositiveButton(res.getString(R.string.yes),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											deleteChannel();
										}
									})
							.setNegativeButton(res.getString(R.string.no),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).create();
					dialog.show();
				}
			}
		});

	}

	private void infoDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isFinishing()) {
					LayoutInflater inflater = LayoutInflater
							.from(ChannelListActivity.this);
					View addView = inflater.inflate(R.layout.info_dialog, null);
					String infoMessage = "Ancora pochi passi e ci siamo ;)";
					AlertDialog dialog = new AlertDialog.Builder(
							ChannelListActivity.this)
							.setView(addView)
							.setTitle("Benvenuto")
							.setMessage(infoMessage)
							.setIcon(android.R.drawable.ic_dialog_info)
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									}).create();

					dialog.show();
				}
			}
		});

	}

	private void confirmReset() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isFinishing()) {
					String confirmMessage = "Reset all channels and settings, continue?";
					AlertDialog dialog = new AlertDialog.Builder(
							ChannelListActivity.this)
							.setTitle("Reset")
							.setMessage(confirmMessage)
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											reset();
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).create();
					dialog.show();
				}
			}
		});

	}

	private void deleteChannel() {
		new AsyncTask<Void, Void, Void>() {
			ProgressDialog progressDialog;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog = new ProgressDialog(ChannelListActivity.this);
				progressDialog.setMessage(getResources().getString(
						R.string.unsub_channel_dialog2));
				progressDialog.show();
			}

			@SuppressLint("ShowToast")
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				progressDialog.dismiss();

				String message = getResources().getString(R.string.success);
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
		}.execute((Void[]) null);
	}

	private void reset() {
		new AsyncTask<Void, Void, Void>() {
			ProgressDialog progressDialog;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog = new ProgressDialog(ChannelListActivity.this);
				progressDialog.setMessage("Reset");
				progressDialog.show();
			}

			@SuppressLint("ShowToast")
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				progressDialog.dismiss();

				channels.clear();
				channelListView.setChannels(channels);

				String message = getResources().getString(R.string.success);
				Toast.makeText(ChannelListActivity.this, message, 1000).show();

				startServices();
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
				imageFetcher.clearCache();

				Settings.setFirstTime(true);

				return null;
			}
		}.execute((Void[]) null);
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
						LOGE(TAG, e.getMessage());
					}
				}

				return null;
			}
		});
		task.disableDialog();
		if (UIUtils.hasHoneycomb()) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		} else
			task.execute((Void[]) null);
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
		if (UIUtils.hasHoneycomb()) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		} else
			task.execute((Void[]) null);

	}
}
