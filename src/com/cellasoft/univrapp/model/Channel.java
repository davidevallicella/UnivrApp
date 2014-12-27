package com.cellasoft.univrapp.model;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.UnivrReaderFactory;
import com.cellasoft.univrapp.loader.ChannelLoader;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.manager.SynchronizationManager;
import com.cellasoft.univrapp.provider.Provider;
import com.cellasoft.univrapp.reader.UnivrReader;
import com.cellasoft.univrapp.rss.RSSFeed;
import com.cellasoft.univrapp.rss.RSSHandler.OnNewEntryCallback;
import com.cellasoft.univrapp.utils.ActiveList;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Observable;

public class Channel extends Observable implements ActionSupport, Serializable {

    private static final long serialVersionUID = 6999952067033640004L;
    public boolean updating = false;
    public int id;
    public int lecturerId;
    public String url;
    public String title;
    public String description;
    public String imageUrl;
    public int unread;
    public long updateTime;
    public boolean isSelected;
    public boolean starred;
    public boolean mute = false;
    private transient ActiveList<Item> items = new ActiveList<Item>();
    private Object synRoot = new Object();

    public Channel() {
        this.id = 0;
        this.lecturerId = 0;
        this.updateTime = new Timestamp(System.currentTimeMillis()).getTime();
    }

    public Channel(int id) {
        this.id = id;
        this.lecturerId = 0;
        this.updateTime = new Timestamp(System.currentTimeMillis()).getTime();
    }

    public Channel(String url) {
        this("", url);
    }

    public Channel(String title, String url) {
        this();
        this.title = title;
        this.url = url;
        this.starred = true;
    }

    public Channel(int lecturerId, String title, String url, String imageUrl,
                   String description) {
        this(title, url);
        this.lecturerId = lecturerId;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Channel(Channel channel) {
        this.id = channel.id;
        this.lecturerId = channel.lecturerId;
        this.url = channel.url;
        this.title = channel.title;
        this.description = channel.description;
        this.imageUrl = channel.imageUrl;
        this.unread = channel.unread;
        this.updateTime = channel.updateTime;
        this.isSelected = channel.isSelected;
        this.starred = channel.starred;
        this.mute = channel.mute;
        this.updating = channel.updating;
    }

    public static Channel findById(int id, ChannelLoader loader) {
        return ContentManager.loadChannel(id, loader);
    }

    public static List<Channel> loadAllChannels(ChannelLoader loader) {
        return ContentManager.loadAllChannels(loader);
    }

    public ActiveList<Item> getItems() {
        synchronized (items) {
            if (items == null) {
                items = new ActiveList<Item>();
            }
            return items;
        }
    }

    public void clearItems() {
        getItems().clear();
    }

    public boolean existItem(Item item) {
        return this.getItems().indexOf(item) >= 0;
    }

    public void addItem(Item item) {
        item.channel = this;

        synchronized (synRoot) {
            ActiveList<Item> items = this.getItems();
            if (items.indexOf(item) < 0) {
                // find insert location
                int position = 0;
                for (Item currentItem : this.items) {
                    if (currentItem.updateTime < item.updateTime) {
                        // if (currentItem.pubDate.before(item.pubDate)) {
                        items.add(position, item);
                        return;
                    }
                    position++;
                }
                items.add(item);
            }
        }
    }

    public void addItem(int position, Item item) {
        synchronized (synRoot) {
            ActiveList<Item> items = this.getItems();
            if (items.indexOf(item) < 0)
                items.add(position, item);
        }
    }

    public boolean isUpdating() {
        synchronized (synRoot) {
            return updating;
        }
    }

    public List<Item> update(int maxItems) {
        synchronized (synRoot) {
            if (updating)
                return null;
            updating = true;
            SynchronizationManager.getInstance().onSynchronizationStart(id);
            this.setChanged();
            this.notifyObservers(updating);
        }

        List<Item> newItems = updateItems(maxItems);
        updateTime = new Timestamp(System.currentTimeMillis()).getTime();
        save();

        saveItems(newItems);

        synchronized (synRoot) {
            updating = false;
            this.setChanged();
            this.notifyObservers(updating);
        }

        return newItems;
    }

    protected List<Item> updateItems(int maxItems) {
        int numberOfFetchedItems = 0;
        RSSFeed feed = null;
        try {
            UnivrReader reader = UnivrReaderFactory.getUnivrReader();
            while (true) {
                feed = reader.fetchEntriesOfFeed(this, maxItems,
                        new OnNewEntryCallback() {

                            @Override
                            public void onNewEntry(Item item) {
                                if (item.exist()) {
                                    throw new RuntimeException(
                                            "Found exist item. Stop parsing "
                                                    + Channel.this.title);
                                } else {
                                    item.channel = Channel.this;
                                }
                            }
                        });

                List<Item> entries = feed.getEntries();
                long updateTime = System.currentTimeMillis();
                for (int i = entries.size() - 1; i >= 0; i--) {
                    Item item = entries.get(i);
                    item.updateTime = updateTime++;
                    this.addItem(0, item);
                }

                numberOfFetchedItems += feed.getEntries().size();
                if (numberOfFetchedItems >= maxItems
                        || feed.getEntries().size() < Config.MAX_ITEMS_PER_FETCH) {
                    break;
                }

                try {
                    Thread.yield();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }

        if (feed != null)
            return feed.getEntries();

        return null;
    }

    private int saveItems(List<Item> items) {
        int newItems = 0;
        if (items != null && !items.isEmpty()) {
            if (exist()) {
                for (Item item : items) {
                    if (item.save()) {
                        newItems++;
                    }
                }
            }
        }

        return newItems;
    }

    public boolean isEmpty() {
        return this.getItems().size() == 0;
    }

    public int size() {
        return getItems().size();
    }

    public int indexOf(Item item) {
        return getItems().indexOf(item);
    }

    public void markChannelToStarred() {
        ContentManager.markChannelToStarred(this);
    }

    public void unmarkChannelToStarred() {
        ContentManager.unmarkChannelToStarred(this);
    }

    public void markChannelToMute() {
        ContentManager.markChannelToMute(this);
    }

    public void unmarkChannelToMute() {
        ContentManager.unmarkChannelToMute(this);
    }

    public boolean subscribe() {
        if (!ContentManager.existChannel(this)) {
            return save();
        }
        return false;
    }

    @Override
    public boolean save() {
        return ContentManager.saveChannel(this);
    }

    @Override
    public void delete() {
        this.clearItems();
        ContentManager.deleteChannel(this);
    }

    @Override
    public boolean exist() {
        return ContentManager.existChannel(this);
    }

    public int clean() {
        return ContentManager.cleanChannel(this);
    }

    public void loadLightweightItems() {
        ContentManager.loadAllItemsOfChannel(this,
                ContentManager.LIGHTWEIGHT_ITEM_LOADER);
    }

    public void loadFullItems() {
        ContentManager.loadAllItemsOfChannel(this,
                ContentManager.FULL_ITEM_LOADER);
    }

    @Override
    public String toString() {
        return String
                .format("Channel [id=%s, title=%s, description=%s, url=%s, thumbnail=%s]",
                        id, title, description, url, imageUrl);
    }

    public static final class Channels implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Provider.AUTHORITY + "/channels");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cellasoft.univrapp.provider.channels";

        public static final String ID = "ID";
        public static final String LECTURER_ID = "LECTURER_ID";
        public static final String TITLE = "TITLE";
        public static final String URL = "URL";
        public static final String DESCRIPTION = "DESCRIPTION";
        public static final String UPDATE_TIME = "UPDATE_TIME";
        public static final String UNREAD = "UNREAD";
        public static final String STARRED = "STARRED";
        public static final String MUTE = "MUTE";
        public static final String IMAGE_URL = "IMAGE_URL";

        private Channels() {
        }
    }
}
