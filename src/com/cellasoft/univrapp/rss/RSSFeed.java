package com.cellasoft.univrapp.rss;

import android.util.Log;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.rss.RSSHandler.OnNewEntryCallback;
import com.cellasoft.univrapp.utils.Lists;
import com.cellasoft.univrapp.utils.StreamUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class RSSFeed {

    private static SAXParserFactory factory = SAXParserFactory.newInstance();

    private int id;
    private String title;
    private String link;
    private String description;
    private Date updated;
    private List<Item> entries;

    public RSSFeed() {
        entries = Lists.newArrayList();
    }

    public static RSSFeed parse(InputStream is, int maxItems,
                                OnNewEntryCallback callback) {
        RSSHandler handler = new RSSHandler(maxItems);
        handler.setCallback(callback);

        try {
            // create a parser
            SAXParser parser = factory.newSAXParser();
            // create the reader (scanner)
            XMLReader xmlReader = parser.getXMLReader();
            // assign our handler
            xmlReader.setContentHandler(handler);
            // perform the synchronous parse
            xmlReader.parse(new InputSource(is));
        } catch (Exception e) {
            Log.e("ERROR", "Parser IO exception.");
        } finally {
            StreamUtils.closeQuietly(is);
        }

        return handler.getFeed();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return this.link;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public List<Item> getEntries() {
        return entries;
    }

    public void setEntries(List<Item> entries) {
        this.entries = entries;
    }

    public void addItem(Item item) {
        this.entries.add(item);
    }
}
