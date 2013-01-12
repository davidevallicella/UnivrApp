package com.cellasoft.univrapp.activity;

import com.cellasoft.univrapp.adapter.DashboardAdapter;
import com.cellasoft.univrapp.service.DownloadingService;
import com.cellasoft.univrapp.utils.DashboardEntry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class MainActivity extends Activity implements OnItemClickListener {

	private DashboardEntry[] dashboardEntries = { new DashboardEntry(
			R.string.launcher_notes_feed, R.drawable.ic_menu_feed,
			ChannelListActivity.class) };
	private GridView dashboardGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grid_menu);

		dashboardGridView = (GridView) findViewById(R.id.dashboardGridView);
		dashboardGridView.setOnItemClickListener(this);
		dashboardGridView.setAdapter(new DashboardAdapter(MainActivity.this,
				R.layout.dashboard_entry, dashboardEntries));

	}
	
	@Override
	protected void onStart() {
		super.onStart();
		startDownloadingService();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		startActivity(new Intent(this, dashboardEntries[position].getActivity()));
	}


	
	private void startDownloadingService() {
		Intent service = new Intent(MainActivity.this,
				DownloadingService.class);
		startService(service);
	}

}
