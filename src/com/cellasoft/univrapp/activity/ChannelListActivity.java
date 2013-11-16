package com.cellasoft.univrapp.activity;

import static com.cellasoft.univrapp.utils.LogUtils.LOGD;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.GCMUtils;
import com.cellasoft.univrapp.utils.ImageFetcher;
import com.cellasoft.univrapp.utils.UIUtils;
import com.cellasoft.univrapp.widget.ChannelListView;
import com.cellasoft.univrapp.widget.ChannelView;
import com.cellasoft.univrapp.widget.OnChannelViewListener;
import com.cellasoft.univrapp.widget.SynchronizationListener;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;
import com.google.android.gcm.GCMRegistrar;

public class ChannelListActivity extends BaseListActivity {

	private static final String TAG = makeLogTag(ChannelListActivity.class);

	private static final int FIRST_TIME = 1;

	private boolean loadingData;
	private List<Channel> channels;
	private ChannelListView listView;
	private ImageFetcher imageFetcher;

	private SynchronizationListener synchronizationListener = new SynchronizationListener() {
		public void onStart(int id) {
			if (channels == null)
				return;
			for (Channel channel : channels) {
				if (channel.id == id) {
					channel.updating = true;
					listView.refresh();
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
					listView.refresh();
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
				if (running) {
					listView.refresh();
				}
			};

			@Override
			protected Void doInBackground(Boolean... isStarred) {
				if (isStarred != null && isStarred.length > 0 && isStarred[0]) {
					channels.get(position).markChannelToStarred();
				} else {
					channels.get(position).unmarkChannelToStarred();
				}
				return null;
			}

		}.execute(value);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onCreate()");
		}

		super.onCreate(savedInstanceState);

		if (isFinishing()) {
			return;
		}

		// UIUtils.enableDisableActivities(this);

		imageFetcher = ImageFetcher.getInstance(this);
		setContentView(R.layout.channel_list);
		init();
	}

	@Override
	protected void onStart() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onStart()");
		}
		super.onStart();
		Application.parents.clear();
		loadData();
	}

	@Override
	protected void onDestroy() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onDestroy()");
		}
		super.onDestroy();

		GCMUtils.onDistroyGCMClient(this);

		if (imageFetcher != null) {
			imageFetcher.destroy();
		}

		if (channels != null) {
			channels.clear();
		}

		if (listView != null) {
			listView.clean();
			unbindDrawables(listView);
			listView = null;
		}

		System.gc();
	}

	@Override
	protected void onResume() {
		super.onResume();

		SynchronizationManager.getInstance().registerSynchronizationListener(
				synchronizationListener);
		imageFetcher.setExitTasksEarly(false);
		listView.refresh();
	}

	@Override
	protected void onPause() {
		super.onPause();
		imageFetcher.stop();
		SynchronizationManager.getInstance().unregisterSynchronizationListener(
				synchronizationListener);
	}

	private void init() {
		startServices();

		if (Settings.getFirstTime()) {
			onFirstTime();
		} else if (Settings.isEnabledNotificationUnivrApp()) {
			GCMUtils.doRegister(this);
		} else if (GCMUtils.isRegistered(this)) {
			GCMUtils.doUnregister(this);
		}

		cancelNotification();

		initActionBar();
		initListView();
	}

	@Override
	protected void initActionBar() {
		getSupportActionBar().setTitle(
				getResources().getString(R.string.channel_title));
	}

	@Override
	protected void initListView() {
		listView = (ChannelListView) getListView();
		listView.setChannelViewlistener(channelListener);
	}

	private void onFirstTime() {
		Intent intent = new Intent(this, DepartmentsActivity.class);
		startActivityForResult(intent, FIRST_TIME);
	}

	private void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(Config.NOTIFICATION_ID);
	}

	private void startServices() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "Begin startServices " + new Date());
		}
		Intent service = new Intent(this, SynchronizationService.class);
		startService(service);
	}

	private void stopServices() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "Begin startServices " + new Date());
		}
		Intent service = new Intent(this, SynchronizationService.class);
		stopService(service);
	}

	@Override
	protected void loadData() {
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected void onPostExecute(Boolean success) {
				if (running && success) {
					listView.setChannels(channels);
					refreshUnreadCounts();
				}

				loadingData = false;
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				if (loadingData == false) {
					loadingData = true;
					channels = Channel
							.loadAllChannels(ContentManager.FULL_CHANNEL_LOADER);
					return true;
				}

				return false;
			}
		}.execute();

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Channel channel = channels.get(position);
		showChannel(channel);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.channel_menu, menu);
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

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
			return (super.onOptionsItemSelected(item));
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FIRST_TIME) {
			if (resultCode == RESULT_OK) {
				saveFirstTime();
				GCMUtils.doRegister(this);
				listView.refresh();
			}
			if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
	};

	private void saveFirstTime() {
		new AsyncTask<Void, Void, Void>() {

			protected void onPostExecute(Void result) {
				if (running)
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
		listView.refresh();
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
		Intent intent = new Intent(this, ContactListActivity.class);
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
				progressDialog = new ProgressDialog(ChannelListActivity.this);
				progressDialog.setMessage(getResources().getString(
						R.string.unsub_channel_dialog2));
				progressDialog.show();
			}

			@SuppressLint("ShowToast")
			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();

				if (running) {
					String message = getResources().getString(R.string.success);
					Toast.makeText(ChannelListActivity.this, message, 1000)
							.show();

					loadData();
				}
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
				progressDialog = new ProgressDialog(ChannelListActivity.this);
				progressDialog.setMessage("Reset");
				progressDialog.setCancelable(false);
				progressDialog.show();

			}

			@SuppressLint("ShowToast")
			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();

				if (running) {
					String message = getResources().getString(R.string.success);
					Toast.makeText(ChannelListActivity.this, message, 1000)
							.show();

					startServices();

					onFirstTime();
				}
			}

			@Override
			protected Void doInBackground(Void... params) {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

				stopServices();
				cancelNotification();

				for (Channel channel : channels) {
					channel.delete();
				}
				channels.clear();

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						listView.clean();
						unbindDrawables(listView);
					}
				});

				ContentManager.deleteAllLecturers();
				ContentManager.deleteAllImages();
				imageFetcher.clearCache();

				Settings.setFirstTime(true);

				GCMRegistrar.unregister(getApplicationContext());

				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				return null;
			}
		}.execute((Void[]) null);
	}

	private void refreshUnreadCounts() {
		BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
				this) {

			@Override
			protected void after(Context arg0, Void arg1) {
				if (running)
					listView.refresh();
			}

			protected void handleError(Context context, Exception e) {
				if (running) {
					String message = getResources().getString(
							R.string.not_load_notification);
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				}
			}

		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
			public Void call(BetterAsyncTask<Void, Void, Void> task)
					throws Exception {

				SparseIntArray unreadCounts = ContentManager
						.countUnreadItemsForEachChannel();
				for (Channel channel : channels) {
					channel.unread = unreadCounts.get(channel.id, 0);
				}
				unreadCounts.clear();
				unreadCounts = null;
				return null;
			}
		});
		task.disableDialog();
		UIUtils.execute(task, (Void[]) null);
	}

	private void refreshAllChannels() {
		BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(
				this) {

			protected void handleError(Context context, Exception e) {
				if (running) {
					String message = getResources().getString(
							R.string.not_load_notification);
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				}
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
		UIUtils.execute(task, (Void[]) null);

	}
}
