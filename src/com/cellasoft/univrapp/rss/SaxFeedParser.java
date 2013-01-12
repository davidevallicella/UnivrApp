package com.cellasoft.univrapp.rss;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.cellasoft.univrapp.model.Channel;

public class SaxFeedParser extends BaseFeedParser {

	public SaxFeedParser(String feedUrl) {
		super(feedUrl);
	}

	public int parse(Channel channel) {
		RSSHandler handler = new RSSHandler(channel);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(this.getInputStream(), handler);
		} catch (Exception e) {
		//	Log.i(Constants.LOG_TAG, e.getMessage());
			e.printStackTrace();
		}
		return handler.getNewItems();
	}
}
