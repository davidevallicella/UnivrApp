package com.cellasoft.univrapp.manager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

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
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.model.Lecturer.Lecturers;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.model.Item.Items;
import com.cellasoft.univrapp.provider.DBMS;
import com.cellasoft.univrapp.provider.Provider;
import com.cellasoft.univrapp.utils.ActiveList;
import com.cellasoft.univrapp.utils.Application;
import com.cellasoft.univrapp.utils.Constants;

public class ContentManager {

	public static final ChannelLoader FULL_CHANNEL_LOADER = new FullChannelLoader();
	public static final ChannelLoader LIGHTWEIGHT_CHANNEL_LOADER = new LightweightChannelLoader();
	public static final ItemLoader FULL_ITEM_LOADER = new FullItemLoader();
	public static final ItemLoader LIGHTWEIGHT_ITEM_LOADER = new LightweightItemLoader();
	public static final LecturerLoader LIGHTWEIGHT_LECTURER_LOADER = new LightweightLecturerLoader();
	public static final LecturerLoader FULL_LECTURER_LOADER = new FullLecturerLoader();

	public static final int ITEM_TYPE_CHANNEL = 1;
	public static final int ITEM_TYPE_ITEM = 2;

	private static final Map<Integer, WeakReference<Channel>> channelCache = new HashMap<Integer, WeakReference<Channel>>();

	private static ContentResolver cr;

	static {
		cr = Application.getInstance().getContentResolver();
	}

	public static boolean isEmpty() {
		ChannelLoader loader = new LightweightChannelLoader();
		Cursor cursor = cr.query(Channels.CONTENT_URI, loader.getProjection(),
				null, null, null);
		if (cursor.moveToNext()) {
			cursor.close();
			return false;
		}
		cursor.close();
		return true;
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

		if (lecturer.id == 0) {
			if (existLecturer(lecturer))
				return false;
			ContentValues values = new ContentValues();
			values.put(Lecturers.KEY, lecturer.key);
			values.put(Lecturers.DEST, lecturer.dest);
			values.put(Lecturers.THUMBNAIL, lecturer.thumbnail);
			values.put(Lecturers.NAME, lecturer.name);

			Uri contentUri = cr.insert(Lecturers.CONTENT_URI, values);
			lecturer.id = (int) ContentUris.parseId(contentUri);
		}

		return true;
	}

	public static void subscribe(Channel channel) {
		if (existChannel(channel))
			saveChannel(channel);
	}

	public static boolean saveChannel(Channel channel) {
		ContentValues values = new ContentValues();
		values.put(Channels.TITLE, channel.title);
		values.put(Channels.URL, channel.url);
		values.put(Channels.DESCRIPTION, channel.description);
		values.put(Channels.STARRED, channel.starred);
		values.put(Channels.IMAGE_URL, channel.imageUrl);
		if (!existChannel(channel)) {
			Uri contentUri = cr.insert(Channels.CONTENT_URI, values);
			channel.id = (int) ContentUris.parseId(contentUri);
		} else {
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

	public static void cleanChannel(Channel channel, int keepMaxItems) {
		// delete items
		Cursor cursor = cr.query(Items.limitAndStartAt(1, keepMaxItems - 1),
				new String[] { Items.ID }, Items.CHANNEL_ID + "=?",
				new String[] { String.valueOf(channel.id) }, Items.ID + " ASC");

		if (cursor.moveToNext()) {
			String selection = Items.CHANNEL_ID + "=?";
			int deletedItems = cr.delete(Items.CONTENT_URI, selection,
					new String[] { String.valueOf(channel.id) });
			if (Constants.DEBUG_MODE)
				Log.d("DEBUG", "Number of deleted items: " + deletedItems);
		} else {
			if (Constants.DEBUG_MODE)
				Log.d("DEBUG", "No item to be deleted");
		}
		cursor.close();
	}

	public static boolean saveItem(Item item) {
		ContentValues values = new ContentValues();

		if (existItem(item))
			return false;

		if (item.id == 0) {

			values.put(Items.TITLE, item.title);
			values.put(Items.DESCRIPTION, item.description);
			values.put(Items.PUB_DATE, item.pubDate.getTime());
			values.put(Items.LINK, item.link);
			values.put(Items.READ, item.read);
			values.put(Items.CHANNEL_ID, item.channel.id);
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
				new String[] { String.valueOf(channel.id) }, Items.PUB_DATE
						+ " DESC, " + Items.ID + " ASC");
		ActiveList<Item> items = channel.getItems();
		items.clear();
		while (cursor.moveToNext()) {
			Item item = loader.load(cursor);
			item.channel = channel;
			items.add(item);
		}
		cursor.close();
	}

	public static ArrayList<Lecturer> loadLecturersOfDest(int dest,
			LecturerLoader loader) {
		Cursor cursor = cr.query(Lecturers.CONTENT_URI, loader.getProjection(),
				Lecturers.DEST + "=?", new String[] { String.valueOf(dest) },
				Lecturers.ID + " ASC");
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

	public static void deleteImage(int imageId) {
		cr.delete(Images.CONTENT_URI, Provider.WHERE_ID,
				new String[] { String.valueOf(imageId) });
	}

	public static void deleteItem(Item item) {
		cr.delete(Items.CONTENT_URI, Items.ID + "=?",
				new String[] { String.valueOf(item.id) });
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
				new String[] { Lecturers.ID }, Lecturers.KEY + "=?",
				new String[] { String.valueOf(lecturer.key) }, null);
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
		Application.getInstance().deleteDatabase(DBMS.DATABASE_NAME);
	}

	public static void unsubscribe(Channel channel) {
		deleteChannel(channel);
	}
}
