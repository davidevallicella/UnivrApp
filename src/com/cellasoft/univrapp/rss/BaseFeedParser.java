package com.cellasoft.univrapp.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public abstract class BaseFeedParser implements FeedParser {

	public static enum XML_TAGS {
		RSS, CHANNEL, PUBDATE, DESCRIPTION, LINK, TITLE, ITEM, GUID;
	}

	private final URL feedUrl;

	protected BaseFeedParser(String feedUrl) {
		try {
			this.feedUrl = new URL(feedUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	protected InputStream getInputStream() {
		try {
			return feedUrl.openConnection().getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
