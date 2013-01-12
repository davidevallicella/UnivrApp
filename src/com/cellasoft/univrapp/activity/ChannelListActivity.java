package com.cellasoft.univrapp.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.adapter.ChannelAdapter;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.service.SynchronizationService;
import com.cellasoft.univrapp.utils.ActiveList;
import com.cellasoft.univrapp.utils.ChannelView;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.OnChannelViewListener;
import com.cellasoft.univrapp.utils.Settings;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ChannelListActivity extends SherlockListActivity {

	private static final String TAG = ChannelListActivity.class.getSimpleName();

	private ArrayList<Channel> channels = null;
	private ChannelAdapter channelAdp;

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
					ContentManager.markChannelToStarred(channels.get(position));
				else
					ContentManager.unmarkChannelToStarred(channels
							.get(position));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.DEBUG_MODE)
			Log.d(Constants.LOG_TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadData();
		startSyncProcess();
	}

	private void init() {
		// Init GUI
		initListSelector();
		// initListScrollListener();
		// Add the footer before adding the adapter, else the footer will not
		// load!
		// initListFooter()
	//	initBanner();
	}

	private void initListSelector() {
		getListView().setSelector(R.drawable.list_selector_on_top);
		getListView().setDrawSelectorOnTop(true);
		getListView().invalidateViews();
	}

	private void initBanner() {
		AdView adView = new AdView(this, AdSize.BANNER, Settings.ID_EDITORE);
		LinearLayout layout = (LinearLayout) findViewById(R.id.footer_banner);
		layout.addView(adView);
		adView.loadAd(new AdRequest());
	}

	private void loadData() {
		loadChannels();
		channelAdp = new ChannelAdapter(this, channels, channelListener);
		getListView().setAdapter(channelAdp);
	}

	private void loadChannels() {
		channels = ContentManager
				.loadAllChannels(ContentManager.LIGHTWEIGHT_CHANNEL_LOADER);
		if (channels == null || channels.isEmpty()) {
			channels = new ActiveList<Channel>();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Channel channel = (Channel) channelAdp.getItem(position);
		showChannel(channel);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_subscribe:
			Intent intent = new Intent(this, SubscribeActivity.class);
			startActivityForResult(intent, 1);
			break;
		case R.id.menu_unsubscribe:
			confirmDeleteChannel();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.channel_menu, menu);
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		channelAdp.refresh();
	};
	
	private void startSyncProcess() {
		Intent service = new Intent(ChannelListActivity.this,
				SynchronizationService.class);
		startService(service);
	}

	private void showChannel(Channel channel) {
		Intent intent = new Intent(this, ItemListActivity.class);
		intent.putExtra(ItemListActivity.CHANNEL_ID_PARAM, channel.id);
		intent.putExtra(ItemListActivity.CHANNEL_TITLE_PARAM, channel.title);
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

	private void deleteChannel() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Unsubscribe selected channels");
		AsyncTask<Void, Void, Void> unsubscribingTask = new AsyncTask<Void, Void, Void>() {

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
}
