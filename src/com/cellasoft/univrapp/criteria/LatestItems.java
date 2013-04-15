package com.cellasoft.univrapp.criteria;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.model.Item.Items;

public class LatestItems implements ItemCriteria {
	public static final int ALL_CHANNELS = -1;

	public static final byte NONE = 0;
	public static final byte OLDER = 1;
	public static final byte NEWER = 2;	

	public int channelId = ALL_CHANNELS;
	public Item compareToItem = null;
	public byte comparision = OLDER;
	public int maxItems = Constants.MAX_ITEMS;

	public LatestItems(int channelId) {
		this(channelId, null, OLDER, Constants.MAX_ITEMS);
	}

	public LatestItems(Item compareToItem, byte comparision) {
		this(compareToItem, comparision, Constants.MAX_ITEMS);
	}

	public LatestItems(int channelId, Item compareToItem, byte comparision) {
		this(channelId, compareToItem, comparision, Constants.MAX_ITEMS);
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
				sb.append("(" 
						+ Items.UPDATE_TIME + "<? OR ("
						+ Items.UPDATE_TIME + "=? AND (" 
						+ Items.PUB_DATE	+ "<? OR (" 
						+ Items.PUB_DATE 	+ " =? AND "
						+ Items.ID			+ ">?))))");
			} else if (comparision == NEWER) {
				sb.append("(" 
						+ Items.UPDATE_TIME + ">? OR ("
						+ Items.UPDATE_TIME + "=? AND (" 
						+ Items.PUB_DATE	+ ">? OR (" 
						+ Items.PUB_DATE 	+ " =? AND " 
						+ Items.ID			+ "<?))))");
			}
		}
		if (sb.length() == 0)
			return null;
		else
			return sb.toString();
	}

	@Override
	public String[] getSelectionArgs() {
		List<String> args = new ArrayList<String>();

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
			return	Items.UPDATE_TIME 	+ " DESC, " 
					+ Items.PUB_DATE 	+ " DESC, "
					+ Items.ID 			+ " ASC";
		} else {
			return Items.UPDATE_TIME 	+ " ASC, " 
					+ Items.PUB_DATE 	+ " ASC, "
					+ Items.ID 			+ " DESC";
		}
	}

	@Override
	public Uri getContentUri() {
		return Items.limit(maxItems);
	}
}