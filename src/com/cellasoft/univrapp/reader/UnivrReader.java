package com.cellasoft.univrapp.reader;

import static com.cellasoft.univrapp.utils.LogUtils.LOGD;
import static com.cellasoft.univrapp.utils.LogUtils.LOGI;
import static com.cellasoft.univrapp.utils.LogUtils.LOGV;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.rss.RSSFeed;
import com.cellasoft.univrapp.rss.RSSHandler.OnNewEntryCallback;
import com.cellasoft.univrapp.utils.ErrorResponse;
import com.cellasoft.univrapp.utils.HandlerException;
import com.cellasoft.univrapp.utils.JSONHandler;
import com.cellasoft.univrapp.utils.LecturersHandler;
import com.cellasoft.univrapp.utils.StreamUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class UnivrReader implements Serializable {

	private static final String TAG = makeLogTag(UnivrReader.class);
	private static final long serialVersionUID = 5743213346852835282L;

	protected static final int DEFAULT_RETRY_HANDLER_SLEEP_TIME = 3 * 1000;
	private static final int DEFAULT_NUM_RETRIES = 3;
	protected static int numRetries = DEFAULT_NUM_RETRIES;
	private String userAgent;
	private Context context;

	public UnivrReader(Context context) {
		this.context = context;
		userAgent = buildUserAgent(context);
	}

	public RSSFeed fetchEntriesOfFeed(Channel channel, int maxItems,
			OnNewEntryCallback callback) throws IOException {

		URL url = new URL(channel.url);
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();
		urlConnection.setRequestProperty("User-Agent", userAgent);
		urlConnection.setRequestProperty("Content-Type", "application/rss+xml");
		urlConnection.setDoOutput(true);

		urlConnection.connect();
		throwErrors(urlConnection);

		InputStream is = urlConnection.getInputStream();
		return RSSFeed.parse(is, maxItems, callback);
	}

	public List<Lecturer> getLecturers() throws UnivrReaderException {

		int timesTried = 1;

		while (timesTried <= numRetries) {
			final long startRemote = System.currentTimeMillis();
			LOGI(TAG, "Remote syncing lecturers " + timesTried);

			try {
				ArrayList<Lecturer> lecturers = executeGetJSON(
						Settings.getUniversity().GET_LECTURERS_LIST_URL,
						new LecturersHandler(context));

				if (lecturers != null && !lecturers.isEmpty()) {
					LOGD(TAG, "Remote sync took "
							+ (System.currentTimeMillis() - startRemote) + "ms");
					return lecturers;
				}
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}

			try {
				Thread.sleep(DEFAULT_RETRY_HANDLER_SLEEP_TIME);
			} catch (InterruptedException e) {
			}

			timesTried++;
		}

		String errorMessage = context.getResources().getString(
				R.string.univrapp_server_exception);

		throw new UnivrReaderException(errorMessage);
	}

	/**
	 * Build and return a user-agent string that can identify this application
	 * to remote servers. Contains the package name and version code.
	 */
	private static String buildUserAgent(Context context) {
		String versionName = "unknown";
		int versionCode = 0;

		try {
			final PackageInfo info = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			versionName = info.versionName;
			versionCode = info.versionCode;
		} catch (PackageManager.NameNotFoundException ignored) {
		}

		return context.getPackageName() + "/" + versionName + " ("
				+ versionCode + ") (gzip)";
	}

	private ArrayList<Lecturer> executeGetJSON(String urlString, JSONHandler handler)
			throws IOException {
		LOGD(TAG, "Requesting URL: " + urlString);
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();
		urlConnection.setRequestProperty("User-Agent", userAgent);
        urlConnection.setRequestProperty("Content-Type", "application/json");

		urlConnection.connect();
		throwErrors(urlConnection);

		String response = StreamUtils.readAllText(urlConnection
				.getInputStream());
		LOGV(TAG, "HTTP response: " + response);
		return handler.parse(response);
	}

	private void throwErrors(HttpURLConnection urlConnection)
			throws IOException {
		final int status = urlConnection.getResponseCode();
		if (status < 200 || status >= 300) {
			String errorMessage = null;
			try {
				String errorContent = StreamUtils.readAllText(urlConnection
						.getErrorStream());
				LOGV(TAG, "Error content: " + errorContent);
				ErrorResponse errorResponse = new Gson().fromJson(errorContent,
						ErrorResponse.class);
				errorMessage = errorResponse.error.message;
			} catch (JsonSyntaxException ignored) {
			}

			String exceptionMessage = "Error response " + status + " "
					+ urlConnection.getResponseMessage()
					+ (errorMessage == null ? "" : (": " + errorMessage))
					+ " for " + urlConnection.getURL();

			// TODO: the API should return 401, and we shouldn't have to parse
			// the message
			if (errorMessage != null)
				throw new HandlerException(exceptionMessage);
		}
	}
}
