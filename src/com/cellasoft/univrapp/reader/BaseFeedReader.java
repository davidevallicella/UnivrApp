package com.cellasoft.univrapp.reader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.InputSource;

import com.cellasoft.univrapp.utils.StreamUtils;

public abstract class BaseFeedReader {

	public static enum XML_TAGS {
		RSS, CHANNEL, PUBDATE, DESCRIPTION, LINK, TITLE, ITEM, GUID;
	}

	public static URL feedUrl;

	protected BaseFeedReader(String url) {
		try {
			feedUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	protected static InputSource getInputStream() {
		StreamUtils.disableConnectionReuseIfNecessary();

		try {
			InputStream is = feedUrl.openConnection().getInputStream();
			return new InputSource(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
