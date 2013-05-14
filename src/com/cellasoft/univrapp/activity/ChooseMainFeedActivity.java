package com.cellasoft.univrapp.activity;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.adapter.UniversitylAdapter;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.model.University;
import com.cellasoft.univrapp.reader.UnivrReader;
import com.cellasoft.univrapp.utils.AsyncTask;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.Utils;
import com.github.droidfu.concurrent.BetterAsyncTask;

public class ChooseMainFeedActivity extends SherlockListActivity {

	private UniversitylAdapter adapter;
	private Channel channel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.university_list);
		init();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
		super.onPostCreate(savedInstanceState);
	}

	private void init() {
		setResult(RESULT_CANCELED);
		adapter = new UniversitylAdapter(getApplicationContext());
		getListView().setAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		University university = adapter.getItem(position);
		channel = new Channel(university.name, university.url);
		saveLecturers();
	}

	BetterAsyncTask<Void, String, Void> saveLecturersTask;

	private void saveLecturers() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		Spanned message = Html.fromHtml(getString(R.string.progress_txt)
				.replace("{message}", "<b>Connect to </b>" + channel.title));
		progressDialog.setMessage(message);
		progressDialog.setCancelable(true);
		progressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						saveLecturersTask.cancel(true);
						saveLecturersTask = null;
					}
				});

		saveLecturersTask = new BetterAsyncTask<Void, String, Void>(this) {

			@Override
			protected void after(Context context, Void arg1) {
				progressDialog.dismiss();
				String message = getResources().getString(R.string.success);
				Toast.makeText(ChooseMainFeedActivity.this, message,
						Toast.LENGTH_SHORT).show();
				setResult(RESULT_OK);
				finish();
			}

			@Override
			protected Void doCheckedInBackground(Context context,
					Void... params) throws Exception {
				
				Settings.setUniversity(channel.title);
				
				if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
					List<Lecturer> lecturers = UnivrReader.getLecturers();
					if (!isCancelled()) {
						progressDialog.setCancelable(false);
						progressDialog.setMax(lecturers.size());
						
						channel.starred = true;
						channel.save();

						for (Lecturer lecturer : lecturers) {
							publishProgress("<b>Save Lecturer</b><br/>"
									+ lecturer.name + "...");

							lecturer.save();
						}

						lecturers.clear();
					}
				} else {
					throw new UnivrReaderException(getResources().getString(
							R.string.univrapp_connection_exception));
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				if (values[0].length() != 0) {
					Spanned message = Html.fromHtml(getString(
							R.string.progress_txt).replace("{message}",
							values[0]));
					progressDialog.setMessage(message);
				}
				progressDialog.incrementProgressBy(1);
			}

			@Override
			protected void handleError(Context context, Exception e) {
				progressDialog.dismiss();
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			}
		};

		saveLecturersTask.disableDialog();
		progressDialog.show();
		if (Utils.hasHoneycomb()) {
			saveLecturersTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		} else {
			saveLecturersTask.execute((Void[]) null);
		}
	}
}
