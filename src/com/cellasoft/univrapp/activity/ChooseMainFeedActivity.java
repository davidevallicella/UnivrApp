package com.cellasoft.univrapp.activity;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.adapter.UniversitylAdapter;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.HtmlParser;
import com.cellasoft.univrapp.utils.Settings;
import com.github.droidfu.concurrent.BetterAsyncTask;

public class ChooseMainFeedActivity extends SherlockListActivity {

	private UniversitylAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_univr);
		init();
	}

	private void init() {
		adapter = new UniversitylAdapter(getApplicationContext());
		getListView().setAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String univeristy = adapter.getItem(position);
		String url = Constants.UNIVERSITY.URL.get(univeristy);
		Settings.setUniversity(univeristy);
		if (new Channel(univeristy, url).save())
			saveLecturers();
	}
	
	private void saveLecturers() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage(Html
				.fromHtml(getString(R.string.progress_txt).replace("{message}",
						"<b>Connect to </b>" + Settings.getUniversity())));
		BetterAsyncTask<Void, String, Void> saveLecturersTask = new BetterAsyncTask<Void, String, Void>(
				this) {

			@Override
			protected void after(Context context, Void arg1) {
				progressDialog.dismiss();
				String message = "Successfull";
				Toast.makeText(ChooseMainFeedActivity.this, message,
						Toast.LENGTH_SHORT).show();
				finish();
			}

			@Override
			protected Void doCheckedInBackground(Context context,
					Void... params) throws Exception {
				Elements lecturers = HtmlParser.getLecturerElements();
				progressDialog.setMax(lecturers.size());

				for (Element option : lecturers) {
					if (isCancelled())
						finish();
					int id = Integer.parseInt(option.attr("value"));
					if (id > 0) {
						Lecturer lecturer = new Lecturer(option);
						if (lecturer.save())
							publishProgress("<b>Initialization Lecturer: </b>"
									+ lecturer.name + "...");
						else
							publishProgress("");
					}
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				if (!values[0].isEmpty())
					progressDialog.setMessage(Html.fromHtml(getString(
							R.string.progress_txt).replace("{message}",
							values[0])));
				progressDialog.incrementProgressBy(1);
			}

			@Override
			protected void handleError(Context context, Exception arg1) {
				progressDialog.dismiss();
				Toast.makeText(context, arg1.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
		};

		saveLecturersTask.disableDialog();
		progressDialog.show();
		saveLecturersTask.execute();
	}
}
