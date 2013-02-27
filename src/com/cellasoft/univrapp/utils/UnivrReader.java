package com.cellasoft.univrapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.rss.RSSHandler.OnNewEntryCallback;
import com.cellasoft.univrapp.rss.SaxFeedParser;

public class UnivrReader implements Serializable {

	private static final long serialVersionUID = 5743213346852835282L;

	public SaxFeedParser fetchEntriesOfFeed(Channel channel, int maxItems,
			OnNewEntryCallback callback) throws Exception {
		InputStream is = get(channel.url).getEntity().getContent();
		return SaxFeedParser.parse(is, maxItems, callback);
	}

	private HttpResponse post(String url, List<NameValuePair> params)
			throws UnivrReaderException, ClientProtocolException, IOException {

		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

		HttpResponse response = (HttpResponse) client.execute(httpPost);
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() != 200) {
			throw new UnivrReaderException(statusLine.getStatusCode(),
					statusLine.getReasonPhrase());
		}

		return response;
	}

	private HttpResponse get(String uri) throws UnivrReaderException,
			IOException, ClientProtocolException, Exception {

		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(uri);

		HttpResponse response = (HttpResponse) client.execute(httpGet);
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() != 200) {
			throw new UnivrReaderException(statusLine.getStatusCode(),
					statusLine.getReasonPhrase() + "(" + uri + ")");
		}
		return response;
	}

}
