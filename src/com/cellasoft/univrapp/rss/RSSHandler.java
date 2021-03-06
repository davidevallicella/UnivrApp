package com.cellasoft.univrapp.rss;

import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.reader.BaseFeedReader.XML_TAGS;
import com.cellasoft.univrapp.utils.DateUtils;
import com.cellasoft.univrapp.utils.Html;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Locale;

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

    private Item currentItem;
    private StringBuilder builder;
    private int maxItems = 20;
    private int currentState = 0;
    private RSSFeed feed;
    private OnNewEntryCallback callback;

    public RSSHandler(int maxItems) {
        this.maxItems = maxItems;
    }

    public OnNewEntryCallback getCallback() {
        return callback;
    }

    public void setCallback(OnNewEntryCallback callback) {
        this.callback = callback;
    }

    public RSSFeed getFeed() {
        return feed;
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
            switch (getTag(localName)) {
                case CHANNEL:
                    feed = new RSSFeed();
                    currentState = RSS_CHANNEL;
                    break;
                case ITEM:
                    currentItem = new Item();
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
                    + localName.toUpperCase(Locale.getDefault()).trim());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);

        String theFullText = cleanUpText(builder);

        switch (getTag(localName)) {
            case ITEM:
                if (callback != null) {
                    try {
                        callback.onNewEntry(currentItem);
                    } catch (Throwable t) {
                        throw new SAXException(t.getMessage());
                    }
                }

                feed.addItem(currentItem);
                currentState = RSS_CHANNEL;
                if (feed.getEntries().size() == maxItems) {
                    throw new SAXException("Reaching maximum items (" + maxItems
                            + "). Stop parsing.");
                }
                break;
            case TITLE:
                if (currentState == RSS_ITEM_TITLE) {
                    currentItem.title = Html.decode(theFullText);
                    currentState = RSS_ITEM;
                } else if (currentState == RSS_CHANNEL_TITLE) {
                    feed.setTitle(Html.decode(theFullText));
                    currentState = RSS_CHANNEL;
                }
                break;
            case LINK:
                if (currentState == RSS_ITEM_LINK) {
                    currentItem.link = theFullText;
                    if (currentItem.exist()) {
                        throw new SAXException(
                                "Trovato item gi� esistente. Stop parsing.");
                    }
                    currentState = RSS_ITEM;
                } else if (currentState == RSS_CHANNEL_LINK) {
                    feed.setLink(theFullText);
                    currentState = RSS_CHANNEL;
                }
                break;
            case DESCRIPTION:
                if (currentState == RSS_ITEM_DESCRIPTION) {
                    currentItem.description = theFullText.replace(",", "<br/>");
                    currentState = RSS_ITEM;
                } else if (currentState == RSS_CHANNEL_DESCRIPTION) {
                    feed.setDescription(theFullText);
                    currentState = RSS_CHANNEL;
                }
                break;
            case PUBDATE:
                if (currentState == RSS_ITEM_PUB_DATE) {
                    currentItem.pubDate = DateUtils.parseRfc822(theFullText);
                    currentState = RSS_ITEM;
                } else
                    currentState = RSS_CHANNEL;
                break;
            case GUID:
                if (currentState == RSS_ITEM_GUID) {
                    currentItem.guid = theFullText;
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
        if (builder != null)
            builder.append(ch, start, length);
    }

    private XML_TAGS getTag(String localName) {
        return XML_TAGS.valueOf(localName.toUpperCase(Locale.getDefault())
                .trim());
    }

    private String cleanUpText(StringBuilder sb) {
        if (sb == null)
            return null;
        return sb.toString().replace("\r", "").replace("\t", "")
                .replace("\n", "").trim();
    }

    public interface OnNewEntryCallback {
        void onNewEntry(Item item);
    }
}
