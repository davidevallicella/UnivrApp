package com.cellasoft.univrapp.manager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.criteria.ItemCriteria;
import com.cellasoft.univrapp.loader.ChannelLoader;
import com.cellasoft.univrapp.loader.FullChannelLoader;
import com.cellasoft.univrapp.loader.FullItemLoader;
import com.cellasoft.univrapp.loader.FullLecturerLoader;
import com.cellasoft.univrapp.loader.ItemLoader;
import com.cellasoft.univrapp.loader.LecturerLoader;
import com.cellasoft.univrapp.loader.LightweightChannelLoader;
import com.cellasoft.univrapp.loader.LightweightItemLoader;
import com.cellasoft.univrapp.loader.LightweightLecturerLoader;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Channel.Channels;
import com.cellasoft.univrapp.model.Image;
import com.cellasoft.univrapp.model.Image.Images;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.model.Item.Items;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.model.Lecturer.Lecturers;
import com.cellasoft.univrapp.provider.DatabaseHelper;
import com.cellasoft.univrapp.provider.Provider;
import com.cellasoft.univrapp.utils.ActiveList;

public class ContentManager {

	public static final ChannelLoader FULL_CHANNEL_LOADER = new FullChannelLoader();
	public static final ChannelLoader LIGHTWEIGHT_CHANNEL_LOADER = new LightweightChannelLoader();
	public static final ItemLoader FULL_ITEM_LOADER = new FullItemLoader();
	public static final ItemLoader LIGHTWEIGHT_ITEM_LOADER = new LightweightItemLoader();
	public static final LecturerLoader LIGHTWEIGHT_LECTURER_LOADER = new LightweightLecturerLoader();
	public static final LecturerLoader FULL_LECTURER_LOADER = new FullLecturerLoader();

	public static final int ITEM_TYPE_CHANNEL = 1;
	public static final int ITEM_TYPE_ITEM = 2;

	private static Set<String> recentReadArticles = new HashSet<String>();
	private static final Map<Integer, WeakReference<Channel>> channelCache = new HashMap<Integer, WeakReference<Channel>>();

	private static ContentResolver cr;

	static {
		cr = Application.getInstance().getContentResolver();
	}

	public static Channel loadChannel(int id, ChannelLoader loader) {
		Channel channel = getChannelFromCache(id);
		if (channel != null)
			return channel;

		Cursor cursor = cr.query(Channels.CONTENT_URI, loader.getProjection(),
				Provider.WHERE_ID, new String[] { String.valueOf(id) }, null);
		if (cursor.moveToFirst()) {
			channel = loader.load(cursor);
			putChannelToCache(channel);
		}
		cursor.close();

		return channel;
	}

	public static boolean saveLecturer(Lecturer lecturer) {
		ContentValues values = new ContentValues();
		values.put(Lecturers.ID, lecturer.id);
		values.put(Lecturers.KEY, lecturer.key);
		values.put(Lecturers.DEST, lecturer.dest);
		values.put(Lecturers.THUMBNAIL, lecturer.thumbnail);
		values.put(Lecturers.NAME, lecturer.name);
		values.put(Lecturers.TELEPHONE, lecturer.telephone);
		values.put(Lecturers.EMAIL, lecturer.email);
		values.put(Lecturers.OFFICE, lecturer.office);
		values.put(Lecturers.DEPARTMENT, lecturer.department);
		values.put(Lecturers.SECTOR, lecturer.sector);

		if (!existLecturer(lecturer)) {
			cr.insert(Lecturers.CONTENT_URI, values);
		} else {
			cr.update(Lecturers.CONTENT_URI, values, Provider.WHERE_ID,
					new String[] { String.valueOf(lecturer.id) });
		}

		return true;
	}

	public static void subscribe(Channel channel) {
		if (existChannel(channel))
			saveChannel(channel);
	}

	public static boolean saveChannel(Channel channel) {
		ContentValues values = new ContentValues();

		if (channel.id == 0) {
			values.put(Channels.LECTURER_ID, channel.lecturerId);
			values.put(Channels.TITLE, channel.title);
			values.put(Channels.URL, channel.url);
			values.put(Channels.DESCRIPTION, channel.description);
			values.put(Channels.STARRED, channel.starred);
			values.put(Channels.MUTE, channel.mute);
			values.put(Channels.UPDATE_TIME, channel.updateTime);
			values.put(Channels.IMAGE_URL, channel.imageUrl);
			Uri contentUri = cr.insert(Channels.CONTENT_URI, values);
			channel.id = (int) ContentUris.parseId(contentUri);
		} else {
			values.put(Channels.UPDATE_TIME, channel.updateTime);
			cr.update(Channels.CONTENT_URI, values, Provider.WHERE_ID,
					new String[] { String.valueOf(channel.id) });
		}

		// invalidate cache
		Channel channelInCache = getChannelFromCache(channel.id);
		if (channelInCache != null) {
			channelCache.remove(channel.id);
			putChannelToCache(channel);
		}

		return true;
	}

	public static void deleteChannel(Channel channel) {
		cr.delete(Items.CONTENT_URI, Items.CHANNEL_ID + "=?",
				new String[] { String.valueOf(channel.id) });
		cr.delete(Channels.CONTENT_URI, Provider.WHERE_ID,
				new String[] { String.valueOf(channel.id) });
		channel.id = 0;

		if (isChannelInCache(channel.id)) {
			channelCache.remove(channel.id);
		}
	}

	public static int cleanChannel(Channel channel) {
		return cr.delete(Items.CONTENT_URI, Items.CHANNEL_ID + "=?",
				new String[] { String.valueOf(channel.id) });
	}

	// ASC a->z 1->n
	// DESC z->a n->1

	public static void cleanUp(Channel channel, int keepMaxItems) {
		// delete old items
		Cursor cursor = cr.query(Items.limitAndStartAt(1, keepMaxItems - 1),
				new String[] { Items.ID, Items.PUB_DATE, Items.UPDATE_TIME },
				Items.CHANNEL_ID + "=?",
				new String[] { String.valueOf(channel.id) }, Items.UPDATE_TIME
						+ " DESC, " + Items.PUB_DATE + " DESC, " + Items.ID
						+ " ASC");

		if (cursor.moveToNext()) {
			long id = cursor.getLong(0);
			long lastPubDate = cursor.getLong(1);
			long updateTime = cursor.getLong(2);
			cursor.close();

			String selection = Items.CHANNEL_ID + "=? AND ("
					+ Items.UPDATE_TIME + "<? OR (" + Items.UPDATE_TIME
					+ "=? AND (" + Items.PUB_DATE + "<? OR (" + Items.PUB_DATE
					+ " =? AND " + Items.ID + ">?))))";

			int deletedItems = cr.delete(
					Items.CONTENT_URI,
					selection,
					new String[] { String.valueOf(channel.id),
							String.valueOf(updateTime),
							String.valueOf(updateTime),
							String.valueOf(lastPubDate),
							String.valueOf(lastPubDate), String.valueOf(id) });

			if (Constants.DEBUG_MODE)
				Log.d("DEBUG", "Number of deleted items: " + deletedItems);
		} else {
			if (Constants.DEBUG_MODE)
				Log.d("DEBUG", "No item to be deleted");
			cursor.close();
		}
	}

	public static boolean saveItem(Item item) {
		ContentValues values = new ContentValues();
		if (item.id == 0) {
			if (existItem(item))
				return false;

			values.put(Items.TITLE, item.title);
			values.put(Items.DESCRIPTION, item.description);
			values.put(Items.PUB_DATE, item.pubDate.getTime());
			values.put(Items.LINK, item.link);
			values.put(Items.READ, item.read);
			values.put(Items.CHANNEL_ID, item.channel.id);
			values.put(Items.UPDATE_TIME, item.updateTime);

			Uri contentUri = cr.insert(Items.CONTENT_URI, values);
			item.id = (int) ContentUris.parseId(contentUri);

		} else {
			values.put(Items.READ, item.read);
			cr.update(Items.CONTENT_URI, values, Provider.WHERE_ID,
					new String[] { String.valueOf(item.id) });
		}

		return true;
	}

	public static Item loadItem(int id, ItemLoader loader,
			ChannelLoader channelLoader) {
		Cursor cursor = cr.query(Items.CONTENT_URI, loader.getProjection(),
				Items.ID + "=?", new String[] { String.valueOf(id) }, null);
		Item item = null;
		if (cursor.moveToNext()) {
			item = loader.load(cursor);
			if (channelLoader != null) {
				item.channel = loadChannel(item.channel.id, channelLoader);
			}
		}
		cursor.close();

		return item;
	}

	public static List<Item> loadItems(ItemCriteria criteria,
			ItemLoader loader, ChannelLoader channelLoader) {
		Cursor cursor = cr.query(criteria.getContentUri(),
				loader.getProjection(), criteria.getSelection(),
				criteria.getSelectionArgs(), criteria.getOrderBy());
		List<Item> items = new ArrayList<Item>();
		items.clear();
		while (cursor.moveToNext()) {
			Item item = loader.load(cursor);
			if (channelLoader != null) {
				item.channel = loadChannel(item.channel.id, channelLoader);
			}
			items.add(item);
		}
		cursor.close();
		return items;
	}

	public static ArrayList<Channel> loadAllChannels(ChannelLoader loader) {
		Cursor cursor = cr.query(Channels.CONTENT_URI, loader.getProjection(),
				null, null, null);
		ArrayList<Channel> channels = new ActiveList<Channel>();
		while (cursor.moveToNext()) {
			Channel channel = loader.load(cursor);
			putChannelToCache(channel);
			channels.add(channel);
		}
		cursor.close();
		return channels;
	}

	public static void loadAllItemsOfChannel(Channel channel, ItemLoader loader) {
		Cursor cursor = cr.query(Items.CONTENT_URI, loader.getProjection(),
				Items.CHANNEL_ID + "=?",
				new String[] { String.valueOf(channel.id) }, Items.UPDATE_TIME
						+ " DESC, " + Items.PUB_DATE + " DESC, " + Items.ID
						+ " ASC");

		ActiveList<Item> items = channel.getItems();
		items.clear();
		while (cursor.moveToNext()) {
			Item item = loader.load(cursor);
			item.channel = channel;
			items.add(item);
		}
		cursor.close();
	}

	public static boolean existSubscription(int lecturerId) {
		Cursor cursor = cr.query(Channels.CONTENT_URI,
				new String[] { Channels.LECTURER_ID }, Channels.LECTURER_ID
						+ "=?", new String[] { String.valueOf(lecturerId) },
				null);

		boolean result = cursor.moveToFirst();
		cursor.close();
		return result;
	}

	public static ArrayList<Lecturer> loadLecturersOfDest(int dest,
			LecturerLoader loader) {
		Cursor cursor = cr.query(Lecturers.CONTENT_URI, loader.getProjection(),
				Lecturers.DEST + "=?", new String[] { String.valueOf(dest) },
				Lecturers.NAME + " ASC, " + Lecturers.ID + " ASC");
		ArrayList<Lecturer> lecturers = new ActiveList<Lecturer>();
		while (cursor.moveToNext()) {
			Lecturer lecturer = loader.load(cursor);
			lecturers.add(lecturer);
		}
		cursor.close();
		return lecturers;
	}

	public static ArrayList<Lecturer> loadAllLecturers(LecturerLoader loader) {
		Cursor cursor = cr.query(Channels.CONTENT_URI, loader.getProjection(),
				null, null, null);
		ArrayList<Lecturer> lecturers = new ActiveList<Lecturer>();
		while (cursor.moveToNext()) {
			Lecturer lecturer = loader.load(cursor);
			lecturers.add(lecturer);
		}
		cursor.close();
		return lecturers;
	}

	public static Lecturer loadLecturer(int id, LecturerLoader loader) {
		Cursor cursor = cr.query(Lecturers.CONTENT_URI, loader.getProjection(),
				Provider.WHERE_ID, new String[] { String.valueOf(id) }, null);
		Lecturer lecturer = null;

		if (cursor.moveToFirst()) {
			lecturer = loader.load(cursor);
		}
		cursor.close();
		return lecturer;
	}

	public static ArrayList<Image> loadAllQueuedImages() {
		return loadImages(Image.IMAGE_STATUS_QUEUED);
	}

	public static Image loadImage(String url) {
		Cursor cursor = cr.query(Images.CONTENT_URI, new String[] { Images.ID,
				Images.URL, Images.STATUS }, Images.URL + "=?",
				new String[] { url }, null);
		Image image = null;
		if (cursor.moveToFirst()) {
			image = new Image(cursor.getInt(0), cursor.getString(1),
					(byte) cursor.getInt(2));
		}
		cursor.close();
		return image;
	}

	public static ArrayList<Image> loadImages(int status) {
		Cursor cursor = cr.query(Images.CONTENT_URI, new String[] { Images.ID,
				Images.URL, Images.STATUS, Images.RETRIES }, Images.STATUS
				+ "=?", new String[] { String.valueOf(status) },
				Images.UPDATE_TIME + " DESC, " + Images.RETRIES + " ASC, "
						+ Images.ID);
		ArrayList<Image> images = new ArrayList<Image>();
		while (cursor.moveToNext()) {
			Image image = new Image(cursor.getInt(0), cursor.getString(1),
					(byte) cursor.getInt(2));
			image.retries = (byte) cursor.getInt(3);
			images.add(image);
		}
		cursor.close();
		return images;
	}

	public static boolean saveImage(Image image) {
		ContentValues values = new ContentValues();
		if (image.id == 0) {
			if (existImage(image))
				return false;

			values.put(Images.URL, image.url);
			values.put(Images.STATUS, image.status);
			values.put(Images.UPDATE_TIME, image.updateTime);
			values.put(Images.RETRIES, image.retries);
			Uri contentUri = cr.insert(Images.CONTENT_URI, values);
			image.id = (int) ContentUris.parseId(contentUri);
		} else {
			values.put(Images.STATUS, image.status);
			values.put(Images.RETRIES, image.retries);
			cr.update(Images.CONTENT_URI, values, Provider.WHERE_ID,
					new String[] { String.valueOf(image.id) });
		}

		return true;
	}

	public static List<Integer> loadOldestImageIds(int keepMaxItems) {
		Cursor cursor = cr.query(Images.CONTENT_URI,
				new String[] { Images.COUNT }, null, null, null);
		int totalImages = 0;
		if (cursor.moveToNext()) {
			totalImages = cursor.getInt(0);
		}
		cursor.close();
		ArrayList<Integer> images = new ArrayList<Integer>();
		if (totalImages - keepMaxItems > 0) {
			cursor = cr.query(Images.limit(totalImages - keepMaxItems),
					new String[] { Images.ID }, null, null, Images.ID + " ASC");

			while (cursor.moveToNext()) {
				images.add(cursor.getInt(0));
			}
			cursor.close();
		}
		return images;
	}

	public static void deleteImage(Image image) {
		cr.delete(Images.CONTENT_URI, Provider.WHERE_ID,
				new String[] { String.valueOf(image.id) });
	}

	public static void deleteImage(int imageId) {
		cr.delete(Images.CONTENT_URI, Provider.WHERE_ID,
				new String[] { String.valueOf(imageId) });
	}

	public static void deleteItem(Item item) {
		cr.delete(Items.CONTENT_URI, Provider.WHERE_ID,
				new String[] { String.valueOf(item.id) });
	}

	public static void deleteLecturer(Lecturer lecturer) {
		cr.delete(Lecturers.CONTENT_URI, Provider.WHERE_ID,
				new String[] { String.valueOf(lecturer.id) });
	}

	public static void deleteAllLecturers() {
		Cursor cursor = cr.query(Lecturers.CONTENT_URI,
				new String[] { Lecturers.ID }, null, null, null);
		while (cursor.moveToNext()) {
			int id = cursor.getInt(0);
			cr.delete(Lecturers.CONTENT_URI, Provider.WHERE_ID,
					new String[] { String.valueOf(id) });
		}
		cursor.close();
	}

	public static void deleteAllImages() {
		Cursor cursor = cr.query(Images.CONTENT_URI,
				new String[] { Images.ID }, null, null, null);
		while (cursor.moveToNext()) {
			int id = cursor.getInt(0);
			cr.delete(Images.CONTENT_URI, Provider.WHERE_ID,
					new String[] { String.valueOf(id) });
		}
		cursor.close();
	}

	public static boolean existItem(Item item) {
		return existItem(item.link);
	}

	public static boolean existItem(String link) {
		Cursor cursor = cr.query(Items.CONTENT_URI, new String[] { Items.ID },
				Items.LINK + "=?", new String[] { link }, null);
		boolean result = cursor.moveToFirst();
		cursor.close();
		return result;
	}

	public static boolean existChannel(Channel channel) {
		Cursor cursor = cr.query(Channels.CONTENT_URI,
				new String[] { Channels.ID }, Channels.URL + "=?",
				new String[] { channel.url }, null);
		boolean result = cursor.moveToFirst();
		cursor.close();
		return result;
	}

	public static boolean existLecturer(Lecturer lecturer) {
		Cursor cursor = cr.query(Lecturers.CONTENT_URI,
				new String[] { Lecturers.ID }, Lecturers.ID + "=?",
				new String[] { String.valueOf(lecturer.id) }, null);
		boolean result = cursor.moveToFirst();
		cursor.close();
		return result;
	}

	public static boolean existImage(Image image) {
		Cursor cursor = cr.query(Images.CONTENT_URI,
				new String[] { Images.ID }, Images.URL + "=?",
				new String[] { image.url }, null);
		boolean result = cursor.moveToFirst();
		cursor.close();
		return result;
	}

	public static void clearReadArticles() {
		recentReadArticles.clear();
	}

	public static boolean isItemRead(int itemId) {
		return recentReadArticles.contains(String.valueOf(itemId));
	}

	public static void markItemAsRead(Item item) {
		saveItemReadState(item, Item.READ);
	}

	public static void markAllItemsOfChannelAsRead(Channel channel) {
		Cursor cursor = cr.query(Items.CONTENT_URI, new String[] { Items.ID, },
				Items.CHANNEL_ID + "=? AND " + Items.READ + "=0",
				new String[] { String.valueOf(channel.id) }, null);
		Item item = new Item();
		while (cursor.moveToNext()) {
			item.id = cursor.getInt(0);
			item.read = Item.UNREAD;
			markItemAsRead(item);
		}
		for (Item channelItem : channel.getItems()) {
			channelItem.read = Item.READ;
		}
		cursor.close();
	}

	public static void saveItemReadState(Item item, int readState) {
		if (item.isRead())
			return;

		item.read = Item.READ;
		ContentValues values = new ContentValues();
		values.put(Items.READ, readState);
		cr.update(Items.CONTENT_URI, values, Provider.WHERE_ID,
				new String[] { String.valueOf(item.id) });

		String key = String.valueOf(item.id);
		if (!recentReadArticles.contains(key)) {
			recentReadArticles.add(key);
		}
	}

	public static void markChannelToStarred(Channel channel) {
		if (channel.starred)
			return;

		channel.starred = true;
		ContentValues values = new ContentValues();
		if (channel.id != 0) {
			values.put(Channels.STARRED, 1);
			cr.update(Channels.CONTENT_URI, values, Provider.WHERE_ID,
					new String[] { String.valueOf(channel.id) });
		}
	}

	public static void unmarkChannelToStarred(Channel channel) {
		if (!channel.starred)
			return;

		channel.starred = false;
		ContentValues values = new ContentValues();
		if (channel.id != 0) {
			values.put(Channels.STARRED, 0);
			cr.update(Channels.CONTENT_URI, values, Provider.WHERE_ID,
					new String[] { String.valueOf(channel.id) });
		}
	}

	public static void markChannelToMute(Channel channel) {
		if (channel.mute)
			return;

		channel.mute = true;
		ContentValues values = new ContentValues();
		if (channel.id != 0) {
			values.put(Channels.MUTE, 1);
			cr.update(Channels.CONTENT_URI, values, Provider.WHERE_ID,
					new String[] { String.valueOf(channel.id) });
		}
	}

	public static void unmarkChannelToMute(Channel channel) {
		if (!channel.mute)
			return;

		channel.mute = false;
		ContentValues values = new ContentValues();
		if (channel.id != 0) {
			values.put(Channels.MUTE, 0);
			cr.update(Channels.CONTENT_URI, values, Provider.WHERE_ID,
					new String[] { String.valueOf(channel.id) });
		}
	}

	/**
	 * 
	 * @param cr
	 * @return a map of ChannelId <-> Unread count
	 */
	public static Map<Integer, Integer> countUnreadItemsForEachChannel() {
		Cursor cursor = cr.query(
				Items.countUnreadEachChannel(),
				new String[] { Items.CHANNEL_ID, Items.UNREAD_COUNT },
				Items.READ + "=? OR " + Items.READ + "=?",
				new String[] { String.valueOf(Item.UNREAD),
						String.valueOf(Item.KEPT_UNREAD) }, null);
		Map<Integer, Integer> unreadCounts = new HashMap<Integer, Integer>();
		while (cursor.moveToNext()) {
			unreadCounts.put(cursor.getInt(0), cursor.getInt(1));
		}
		cursor.close();
		return unreadCounts;
	}

	public static int countUnreadItems() {
		Cursor cursor = cr.query(
				Items.countUnread(),
				new String[] { Items.UNREAD_COUNT },
				Items.READ + "=? OR " + Items.READ + "=?",
				new String[] { String.valueOf(Item.UNREAD),
						String.valueOf(Item.KEPT_UNREAD) }, null);
		int unreadCounts = 0;
		if (cursor.moveToNext()) {
			unreadCounts = cursor.getInt(0);
		}
		cursor.close();
		return unreadCounts;
	}

	private static boolean isChannelInCache(long channelId) {
		return channelCache.containsKey(channelId);
	}

	private static void putChannelToCache(Channel channel) {
		if (!isChannelInCache(channel.id)) {
			channelCache.put(channel.id, new WeakReference<Channel>(channel));
		}
	}

	private static Channel getChannelFromCache(int id) {
		if (isChannelInCache(id)) {
			WeakReference<Channel> channelRef = channelCache.get(id);
			return channelRef.get();
		}
		return null;
	}

	public static void clearDatabase() {
		Application.getInstance().deleteDatabase(DatabaseHelper.DATABASE_NAME);
	}

	public static void unsubscribe(Channel channel) {
		deleteChannel(channel);
	}
}
