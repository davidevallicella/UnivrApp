package com.cellasoft.univrapp.activity;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Image;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.ImageCache;
import com.cellasoft.univrapp.utils.Settings;
import com.cellasoft.univrapp.utils.Utils;


@SuppressLint("NewApi")
public class ChooseMainFeedActivity extends Activity {

	private Spinner spinner;
	private Exception error;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_univr);
		init();
	}

	private void init() {
		if (ContentManager.isEmpty()) {
			initSpinner();
		} else
			startMainActivity();
	}

	private void initSpinner() {
		spinner = (Spinner) findViewById(R.id.choose_school);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		adapter.addAll(Constants.UNIVERSITY.DEST.keySet());

		spinner.setAdapter(adapter);

		findViewById(R.id.choose_confirm_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							String univeristy = spinner.getSelectedItem()
									.toString();
							String url = Constants.UNIVERSITY.URL
									.get(univeristy);
							Settings.setUniversity(univeristy);
							Channel channel = new Channel(univeristy, url);
							ContentManager.saveChannel(channel);
							new SaveLecturers(univeristy).execute();
						} catch (Exception e) {
							printError(e);
						}
					}
				});
	}

	private void startMainActivity() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	public void printError(Exception error) {
		error.printStackTrace();
		finish();
	}

	public class SaveLecturers extends AsyncTask<Void, String, Void> {

		String url;
		int dest;
		ProgressDialog dialog;
		private static final int INCREMENT = 1;

		public SaveLecturers(String uni) {
			url = Constants.UNIVERSITY.URL.get(uni).replace("&rss=1", "");
			dest = Constants.UNIVERSITY.DEST.get(uni);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(ChooseMainFeedActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMessage(Html.fromHtml(getString(R.string.progress_txt)
					.replace("x", "<b>Connect to </b>" + url)));
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
			startMainActivity();
		}

		@Override
		protected Void doInBackground(Void... params) {
			
			DefaultHttpClient httpClient = new DefaultHttpClient();

			HttpGet get = new HttpGet(url);
			try {
				HttpResponse response = httpClient.execute(get);
				String html = Utils.inputStreamToString(response.getEntity()
						.getContent());
				Document doc = Jsoup.parse(html);
				Elements options = doc
						.select("select[name=personeMittente]>option");

				dialog.setMax(options.size());
				for (Element option : options) {

					int id = Integer.parseInt(option.attr("value"));
					if (id > 0) {
						String name = option.text();
						String imageUrl = getThumbnailUrl(url.split("ent")[0]
								+ "ent=persona&id=" + id);
						ContentManager.saveLecturer(new Lecturer(id, dest,
								name, imageUrl));
						if (!imageUrl.equals("")) {
							Image image = new Image(imageUrl,
									Image.IMAGE_STATUS_QUEUED);
							if (!ContentManager.existImage(imageUrl))
								ContentManager.saveImage(image);
						}
						publishProgress("<b>Initialization Lecturer: </b>"
								+ name + "...");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			dialog.setMessage(Html.fromHtml(getString(R.string.progress_txt)
					.replace("x", values[0])));
			dialog.incrementProgressBy(INCREMENT);
		}

		private String getThumbnailUrl(String url)
				throws ClientProtocolException, IOException {
			String thumbnailUrl = "";
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse response = httpClient.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String html = Utils
							.inputStreamToString(entity.getContent());
					Document doc = Jsoup.parse(html);
					Element img = doc.select("img[src*=/Persona/]").first();
					if (img != null) {
						thumbnailUrl = url.split("\\.it")[0] + ".it"
								+ img.attr("src");
					}
				}
			}

			return thumbnailUrl;
		}
	}
}
