package com.cellasoft.univrapp.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.InputSource;

public abstract class BaseFeedParser {

	public static enum XML_TAGS {
		RSS, CHANNEL, PUBDATE, DESCRIPTION, LINK, TITLE, ITEM, GUID;
	}

	public static URL feedUrl;

	protected BaseFeedParser(String url) {
		try {
			feedUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	protected static InputSource getInputStream() {
		try {
			InputStream is = feedUrl.openConnection().getInputStream();
			return new InputSource(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
