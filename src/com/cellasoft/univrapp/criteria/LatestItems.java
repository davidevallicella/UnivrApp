package com.cellasoft.univrapp.criteria;

import android.net.Uri;
import com.cellasoft.univrapp.Config;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.model.Item.Items;
import com.cellasoft.univrapp.utils.Lists;

import java.util.List;

public class LatestItems implements ItemCriteria {
    public static final int ALL_CHANNELS = -1;
    public int channelId = ALL_CHANNELS;
    public static final byte NONE = 0;
    public static final byte OLDER = 1;
    public byte comparision = OLDER;
    public static final byte NEWER = 2;
    public Item compareToItem = null;
    public int maxItems = Config.MAX_ITEMS;

    public LatestItems(int channelId) {
        this(channelId, null, OLDER, Config.MAX_ITEMS);
    }

    public LatestItems(Item compareToItem, byte comparision) {
        this(compareToItem, comparision, Config.MAX_ITEMS);
    }

    public LatestItems(int channelId, Item compareToItem, byte comparision) {
        this(channelId, compareToItem, comparision, Config.MAX_ITEMS);
    }

    public LatestItems(Item compareToItem, byte comparision, int maxItems) {
        this(ALL_CHANNELS, compareToItem, comparision, maxItems);
    }

    public LatestItems(int channelId, Item compareToItem, byte comparision,
                       int maxItems) {
        this.channelId = channelId;
        this.compareToItem = compareToItem;
        this.comparision = comparision;
        this.maxItems = maxItems;
    }

    @Override
    public String getSelection() {
        StringBuilder sb = new StringBuilder();
        if (channelId != ALL_CHANNELS) {
            sb.append(Items.CHANNEL_ID + "=?");
        }
        if (compareToItem != null) {
            if (sb.length() > 0) {
                sb.append(" AND ");
            }
            if (comparision == OLDER) {
                sb.append("(" + Items.UPDATE_TIME + "<? OR ("
                        + Items.UPDATE_TIME + "=? AND (" + Items.PUB_DATE
                        + "<? OR (" + Items.PUB_DATE + " =? AND " + Items.ID
                        + ">?))))");
            } else if (comparision == NEWER) {
                sb.append("(" + Items.UPDATE_TIME + ">? OR ("
                        + Items.UPDATE_TIME + "=? AND (" + Items.PUB_DATE
                        + ">? OR (" + Items.PUB_DATE + " =? AND " + Items.ID
                        + "<?))))");
            }
        }
        if (sb.length() == 0)
            return null;
        else
            return sb.toString();
    }

    @Override
    public String[] getSelectionArgs() {
        List<String> args = Lists.newArrayList();

        if (channelId != ALL_CHANNELS) {
            args.add(String.valueOf(channelId));
        }
        if (compareToItem != null) {
            args.add(String.valueOf(compareToItem.updateTime));
            args.add(String.valueOf(compareToItem.updateTime));
            args.add(String.valueOf(compareToItem.pubDate.getTime()));
            args.add(String.valueOf(compareToItem.pubDate.getTime()));
            args.add(String.valueOf(compareToItem.id));
        }
        return args.toArray(new String[0]);
    }

    @Override
    public String getOrderBy() {
        if (comparision == OLDER) {
            return Items.UPDATE_TIME + " DESC, " + Items.PUB_DATE + " DESC, "
                    + Items.ID + " ASC";
        } else {
            return Items.UPDATE_TIME + " ASC, " + Items.PUB_DATE + " ASC, "
                    + Items.ID + " DESC";
        }
    }

    @Override
    public Uri getContentUri() {
        return Items.limit(maxItems);
    }
}