package com.cellasoft.univrapp.reader;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import android.util.Log;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.Settings;
import com.cellasoft.univrapp.activity.R;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.rss.RSSFeed;
import com.cellasoft.univrapp.rss.RSSHandler.OnNewEntryCallback;
import com.cellasoft.univrapp.utils.HttpUtility;
import com.cellasoft.univrapp.utils.StreamUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UnivrReader implements Serializable {

	private static final String TAG = UnivrReader.class.getSimpleName();
	private static final long serialVersionUID = 5743213346852835282L;

	protected static final int DEFAULT_RETRY_HANDLER_SLEEP_TIME = 3000;
	private static final int DEFAULT_NUM_RETRIES = 3;
	protected static int numRetries = DEFAULT_NUM_RETRIES;

	public RSSFeed fetchEntriesOfFeed(Channel channel, int maxItems,
			OnNewEntryCallback callback) throws Exception {
		InputStream is = HttpUtility.get(channel.url).getEntity().getContent();
		return RSSFeed.parse(is, maxItems, callback);
	}

	public static List<Lecturer> getLecturers() throws Exception {

		int timesTried = 1;

		while (timesTried <= numRetries) {
			if (Constants.DEBUG_MODE)
				Log.d(TAG, "Connect to server " + timesTried);

			try {
				InputStream is = HttpUtility
						.get(Settings.getUniversity().GET_LECTURERS_LIST_URL)
						.getEntity().getContent();

				String json = StreamUtils.readAllText(is);

				Type type = new TypeToken<List<Lecturer>>() {
				}.getType();

				List<Lecturer> lecturers = (List<Lecturer>) new Gson()
						.fromJson(json, type);

				if (lecturers != null && !lecturers.isEmpty())
					return lecturers;

			} catch (Throwable e) {
				if (Constants.DEBUG_MODE)
					Log.e(TAG, e.getMessage(), e);
			}

			try {
				Thread.sleep(DEFAULT_RETRY_HANDLER_SLEEP_TIME);
			} catch (InterruptedException e) {
			}

			timesTried++;
		}
		throw new UnivrReaderException(Application.getInstance().getResources()
				.getString(R.string.univrapp_server_exception));
	}

}
