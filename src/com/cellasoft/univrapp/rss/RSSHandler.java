package com.cellasoft.univrapp.rss;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.RSSItem;
import com.cellasoft.univrapp.rss.BaseFeedParser.XML_TAGS;

public class RSSHandler extends DefaultHandler {

	final int RSS_CHANNEL = 0;
	final int RSS_CHANNEL_TITLE = 1;
	final int RSS_CHANNEL_LINK = 2;
	final int RSS_CHANNEL_DESCRIPTION = 3;
	final int RSS_CHANNEL_IMAGE = 4;

	final int RSS_ITEM = 10;
	final int RSS_ITEM_TITLE = 20;
	final int RSS_ITEM_LINK = 30;
	final int RSS_ITEM_DESCRIPTION = 40;
	final int RSS_ITEM_GUID = 50;
	final int RSS_ITEM_PUB_DATE = 60;

	private Channel channel;
	private RSSItem currentItem;
	private StringBuilder builder;
	private int newItems = 0;

	int currentState = 0;

	public RSSHandler(Channel channel) {
		this.channel = channel;
	}

	public Channel getChannel() {
		return this.channel;
	}

	public int getNewItems() {
		return newItems;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		currentState = RSS_CHANNEL;
		builder = new StringBuilder();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		try {
			switch (XML_TAGS.valueOf(localName.toUpperCase().trim())) {
			case CHANNEL:
				currentState = RSS_CHANNEL;
				break;
			case ITEM:
				currentItem = new RSSItem();
				currentItem._channel = this.channel;
				currentState = RSS_ITEM;
				break;
			case TITLE:
				if (currentState >= RSS_ITEM) {
					currentState = RSS_ITEM_TITLE;
				} else if (currentState == RSS_CHANNEL) {
					currentState = RSS_CHANNEL_TITLE;
				}
				break;
			case DESCRIPTION:
				if (currentState >= RSS_ITEM) {
					currentState = RSS_ITEM_DESCRIPTION;
				} else {
					currentState = RSS_CHANNEL_DESCRIPTION;
				}
				break;
			case LINK:
				if (currentState >= RSS_ITEM) {
					currentState = RSS_ITEM_LINK;
				} else if (currentState == RSS_CHANNEL) {
					currentState = RSS_CHANNEL_LINK;
				}
				break;
			case GUID:
				if (currentState >= RSS_ITEM) {
					currentState = RSS_ITEM_GUID;
				}
				break;
			case PUBDATE:
				if (currentState >= RSS_ITEM) {
					currentState = RSS_ITEM_PUB_DATE;
				}
				break;
			default:
				currentState = RSS_CHANNEL;
			}
		} catch (Exception e) {
			currentState = RSS_CHANNEL;
			throw new SAXException("Unknown TAG: "
					+ localName.toUpperCase().trim());
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);

		String theFullText = builder.toString().trim();

		switch (XML_TAGS.valueOf(localName.toUpperCase().trim())) {
		case ITEM:
			channel.addItem(currentItem);
			newItems++;
			currentState = RSS_CHANNEL;
			// if (channel.getItems().size() == Constants.MAX_ITEMS) {
			// throw new SAXException("Reaching maximum items. Stop parsing.");
			// }
			break;
		case TITLE:
			if (currentState == RSS_ITEM_TITLE) {
				currentItem.setTitle(theFullText);
				currentState = RSS_ITEM;
			} else if (currentState == RSS_CHANNEL_TITLE) {
				if (channel.title == null)
					channel.title = theFullText;
				currentState = RSS_CHANNEL;
			}
			break;
		case LINK:
			if (currentState == RSS_ITEM_LINK) {
				currentItem.setLink(theFullText);
				if (ContentManager.existItem(currentItem)) {
					throw new SAXException(
							"Trovato item gi� esistente. Stop parsing.");
				}
				currentState = RSS_ITEM;
			} else if (currentState == RSS_CHANNEL_LINK) {
				channel.url = theFullText;
				currentState = RSS_CHANNEL;
			}
			break;
		case DESCRIPTION:
			if (currentState == RSS_ITEM_DESCRIPTION) {
				currentItem.setDescription(theFullText);
				currentState = RSS_ITEM;
			} else if (currentState == RSS_CHANNEL_DESCRIPTION) {
				channel.description = theFullText;
				currentState = RSS_CHANNEL;
			}
			break;
		case PUBDATE:
			if (currentState == RSS_ITEM_PUB_DATE) {
				currentItem.setDate(theFullText);
				currentState = RSS_ITEM;
			} else
				currentState = RSS_CHANNEL;
			break;
		case GUID:
			if (currentState == RSS_ITEM_GUID) {
				currentItem.setGuid(theFullText);
				currentState = RSS_ITEM;
			} else
				currentState = RSS_CHANNEL;
			break;
		default:
		}

		builder.setLength(0);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if (builder != null)
			for (int i = start; i < start + length; i++)
				if (ch[i] != '\n' && ch[i] != '\t')
					builder.append(ch[i]);
	}
}
