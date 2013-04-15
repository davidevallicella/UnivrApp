package com.cellasoft.univrapp.reader;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.List;

import android.util.Log;

import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.rss.RSSFeed;
import com.cellasoft.univrapp.rss.RSSHandler.OnNewEntryCallback;
import com.cellasoft.univrapp.utils.HttpUtility;
import com.cellasoft.univrapp.utils.StreamUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class UnivrReader implements Serializable {

	private static final String TAG = UnivrReader.class.getSimpleName();
	private static final long serialVersionUID = 5743213346852835282L;

	public RSSFeed fetchEntriesOfFeed(Channel channel, int maxItems,
			OnNewEntryCallback callback) throws Exception {

		InputStream is = HttpUtility.get(channel.url).getResponseBody();
		return RSSFeed.parse(is, maxItems, callback);
	}

	public static List<Lecturer> getLecturers() throws Exception {
		try {
			InputStream is = HttpUtility.get(Settings.getUniversity().GET_LECTURERS_LIST_URL)
					.getResponseBody();

			String json = StreamUtils.readAllText(is);

			Type type = new TypeToken<List<Lecturer>>() {
			}.getType();

			return new Gson().fromJson(json, type);
		} catch (Throwable e) {
			String message = e.getMessage();
			if (e instanceof SocketTimeoutException) {
				message = "Server is down .. wait a few seconds and try again";
				Log.e(TAG, "Error Socket timeout:  " + e.getMessage());
			}
			if (e instanceof UnivrReaderException) {
				message = "Connect error";
				Log.e(TAG, "Connect error:  " + e.getMessage());
			}
			if (e instanceof JsonSyntaxException) {
				message = "Error parse json";
				Log.e(TAG, "Error json: " + e.getMessage());
			}
			throw new UnivrReaderException(message);
		}
	}
}
