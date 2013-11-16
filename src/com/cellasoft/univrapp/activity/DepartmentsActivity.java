package com.cellasoft.univrapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.adapter.UniversitylAdapter;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.University;
import com.cellasoft.univrapp.utils.AsyncTask;

public class DepartmentsActivity extends BaseListActivity {

	private UniversitylAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.university_list);
		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		adapter.clear();
		unbindDrawables(getListView());
	}

	private void init() {
		initListView();
		initActionBar();
		setResult(RESULT_CANCELED);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		University university = new University(adapter.getItem(position));
		Channel channel = new Channel(university.name, university.url);
		saveLecturers(channel);
	}

	private void saveLecturers(Channel channel) {
		new AsyncTask<Channel, Void, Boolean>() {

			protected void onPostExecute(Boolean success) {
				if (success) {
					setResult(RESULT_OK);
					finish();
				}
			};

			@Override
			protected Boolean doInBackground(Channel... params) {
				if (params != null && params.length > 0) {
					params[0].save();
					Settings.setUniversity(params[0].title);
					return true;
				}
				return false;
			}

		}.execute(channel);
	}

	@Override
	protected void loadData() {
		// do nothing
	}

	@Override
	protected void initListView() {
		adapter = new UniversitylAdapter(getApplicationContext(),
				R.layout.university_list_item);
		getListView().setAdapter(adapter);
	}

	@Override
	protected void initActionBar() {
		getSupportActionBar().setTitle("Dipartimenti");
	}
}
