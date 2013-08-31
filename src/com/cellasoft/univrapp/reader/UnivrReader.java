package com.cellasoft.univrapp.reader;

import static com.cellasoft.univrapp.Config.SERVER_URL;
import static com.cellasoft.univrapp.utils.LogUtils.LOGI;
import static com.cellasoft.univrapp.utils.LogUtils.LOGV;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.University;
import com.cellasoft.univrapp.rss.RSSFeed;
import com.cellasoft.univrapp.rss.RSSHandler.OnNewEntryCallback;
import com.cellasoft.univrapp.utils.ErrorResponse;
import com.cellasoft.univrapp.utils.HandlerException;
import com.cellasoft.univrapp.utils.JSONHandler;
import com.cellasoft.univrapp.utils.StreamUtils;
import com.cellasoft.univrapp.widget.ContactItemInterface;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class UnivrReader implements Serializable {

	private static final String TAG = makeLogTag(UnivrReader.class);
	private static final long serialVersionUID = 5743213346852835282L;

	protected static final int DEFAULT_RETRY_HANDLER_SLEEP_TIME = 3 * 1000;
	private static final int DEFAULT_NUM_RETRIES = 3;
	protected static int numRetries = DEFAULT_NUM_RETRIES;
	private String userAgent;

	public UnivrReader(Context context) {
		//this.context = context;
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

	public List<ContactItemInterface> executeGetJSON(University uni,
			JSONHandler handler) throws IOException {
		LOGI(TAG, "get Json (dest = " + uni.dest + ")");

		String serverUrl = SERVER_URL + "/lecturers.php?format=json&dest="
				+ uni.dest;

		URL url = new URL(serverUrl);
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();
		urlConnection.setRequestProperty("User-Agent", userAgent);
		urlConnection.setRequestProperty("Content-Type", "application/json");

		urlConnection.connect();
		throwErrors(urlConnection);

		InputStream is = urlConnection.getInputStream();
		String response = StreamUtils.readAllText(is);
		
		LOGV(TAG, "HTTP response: " + response);
		return handler.parse(response);
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
