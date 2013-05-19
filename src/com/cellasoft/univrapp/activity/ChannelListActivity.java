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
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.ClosableAdView;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.Utils;
import com.cellasoft.univrapp.widget.ChannelListView;
import com.cellasoft.univrapp.widget.ChannelView;
import com.cellasoft.univrapp.widget.OnChannelViewListener;
import com.cellasoft.univrapp.widget.SynchronizationListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;

public class ChannelListActivity extends SherlockListActivity {

	private static final String TAG = ChannelListActivity.class.getSimpleName();

	private static final int FIRST_TIME = 1;

	private ArrayList<Channel> channels = null;
	private ChannelListView channelListView;
	private ClosableAdView adView;

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
		if (Constants.DEBUG_MODE) {
			Log.d(TAG, "onCreate()");
			Utils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		ImageFetcher.inizialize(this);
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
		super.onDestroy();
		if (adView != null) {
			adView.hideAd();
		}
		ImageFetcher.getInstance().closeCache();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SynchronizationManager.getInstance().registerSynchronizationListener(
				synchronizationListener);
		ImageFetcher.getInstance().setExitTasksEarly(false);
		channelListView.refresh();
		showAdmodBanner();
	}

	@Override
	protected void onPause() {
		super.onPause();
		ImageFetcher.getInstance().setPauseWork(false);
		ImageFetcher.getInstance().setExitTasksEarly(true);
		ImageFetcher.getInstance().flushCache();
		SynchronizationManager.getInstance().unregisterSynchronizationListener(
				synchronizationListener);
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
					ImageFetcher.getInstance().setPauseWork(true);
				} else {
					ImageFetcher.getInstance().setPauseWork(false);
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
		adView.inizialize(this);
		adView.loadAd();
	}

	private void showAdmodBanner() {
		if (adView != null) {
			adView.viewAd();
		}
	}

	private void onFirstTime() {
		Intent intent = new Intent(this, ChooseMainFeedActivity.class);
		startActivityForResult(intent, FIRST_TIME);
	}

	private void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(Constants.NOTIFICATION_ID);
	}

	private void startServices() {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "Begin startServices " + new Date());
		Intent service = new Intent(this, SynchronizationService.class);
		startService(service);
	}

	private void stopServices() {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "Begin startServices " + new Date());
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
				ImageFetcher.getInstance().clearCache();

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
						Log.e("ERROR", e.getMessage());
					}
				}

				return null;
			}
		});
		task.disableDialog();
		if (Utils.hasHoneycomb()) {
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
		if (Utils.hasHoneycomb()) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		} else
			task.execute((Void[]) null);

	}
}
