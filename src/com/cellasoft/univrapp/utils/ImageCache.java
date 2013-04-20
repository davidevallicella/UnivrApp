package com.cellasoft.univrapp.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.cellasoft.univrapp.Constants;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.github.droidfu.http.BetterHttpResponse;
import com.google.common.collect.MapMaker;

public class ImageCache implements Map<String, Bitmap> {
	private static final String TAG = ImageCache.class.getSimpleName();
	private static final int MEMORY_PURGE_DELAY = 120 * 1000;

	private static ImageCache instance;

	public static final int THUMBNAIL_HEIGHT = 60;
	public static final int THUMBNAIL_WIDTH = 60;

	private static int cachedImageQuality = 100;
	private Map<String, Bitmap> memoryCache;

	private static CompressFormat compressedPNGImageFormat = CompressFormat.PNG;

	private final Handler mHandler;
	private final MemoryPurger mPurger;

	private ImageCache(Context context, int initialCapacity,
			int concurrencyLevel) {
		memoryCache = new MapMaker().initialCapacity(initialCapacity)
				.concurrencyLevel(concurrencyLevel).weakValues().makeMap();
		FileCache.createInstance(context);
		mHandler = new Handler();
		mPurger = new MemoryPurger(this);
	}

	public static synchronized ImageCache createInstance(Context context,
			int initialCapacity, int concurrencyLevel) {
		instance = new ImageCache(context, initialCapacity, concurrencyLevel);
		return instance;
	}

	public static synchronized ImageCache getInstance() {
		return instance;
	}

	/**
	 * @param cachedImageQuality
	 *            the quality of images being compressed and written to disk
	 *            (2nd level cache) as a number in [0..100]
	 */
	public void setCachedImageQuality(int imageQuality) {
		cachedImageQuality = imageQuality;
	}

	public int getCachedImageQuality() {
		return cachedImageQuality;
	}

	@Override
	public synchronized Bitmap get(Object key) {
		String imageUrl = (String) key;
		Bitmap bitmap = memoryCache.get(imageUrl);

		if (bitmap != null) {
			if (Constants.DEBUG_MODE)
				Log.d("Image cache", "1nd level cache (memory)");
			// 1st level cache hit (memory)
			return bitmap;
		}

		// 2nd level cache hit (disk)
		File imageFile = FileCache.getImageFile(imageUrl);
		if (imageFile.exists()) {
			if (Constants.DEBUG_MODE)
				Log.d("Image cache", "2nd level cache (disk)");

			try {
				bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
			} catch (Throwable e) {
				// treat decoding errors as a cache miss
			}

			if (bitmap == null) {
				return null;
			}

			memoryCache.put(imageUrl, bitmap);
			return bitmap;
		}

		// cache miss
		return null;
	}

	@Override
	public Bitmap put(String imageUrl, Bitmap bitmap) {
		if (imageUrl == null || bitmap == null)
			return null;

		try {
			File imageFile = FileCache.getImageFile(imageUrl);
			imageFile.createNewFile();
			FileOutputStream ostream = new FileOutputStream(imageFile);
			imageFile = null;

			bitmap.compress(compressedPNGImageFormat, cachedImageQuality,
					ostream);

			StreamUtils.closeQuietly(ostream);
		} catch (Throwable e) {
			if (e instanceof FileNotFoundException)
				if (Constants.DEBUG_MODE)
					Log.e(TAG, "File not found", e);
			if (e instanceof IOException)
				if (Constants.DEBUG_MODE)
					Log.e(TAG, "Could not get remote image", e);
		}

		if (Constants.DEBUG_MODE)
			Log.d("Image cache", "Chached " + imageUrl);

		return memoryCache.put(imageUrl, bitmap);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Bitmap> t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		return memoryCache.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return memoryCache.containsValue(value);
	}

	@Override
	public Bitmap remove(Object key) {
		Bitmap bitmap = memoryCache.remove(key);
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		return null;
	}

	@Override
	public Set<String> keySet() {
		return memoryCache.keySet();
	}

	@Override
	public Set<java.util.Map.Entry<String, Bitmap>> entrySet() {
		return memoryCache.entrySet();
	}

	@Override
	public int size() {
		return memoryCache.size();
	}

	@Override
	public boolean isEmpty() {
		return memoryCache.isEmpty();
	}

	@Override
	public void clear() {
		try {
			memoryCache.clear();
			FileCache.clearCacheFolder();
			System.gc();
		} catch (final Exception e) {
			Log.e(TAG, "Unknown exception", e);
		}
	}

	@Override
	public Collection<Bitmap> values() {
		return memoryCache.values();
	}

	public static void downloadImage(String imageUrl)
			throws UnivrReaderException, ConnectException {

		if (Constants.DEBUG_MODE)
			Log.d("ImageCache", "Download (" + imageUrl + ")");

		try {
			// The bitmap isn't cached so download from the web
			BetterHttpResponse response = HttpUtility.get(imageUrl);
			InputStream is = response.getResponseBody();
			Bitmap bitmap = decodeStream(is);
			ByteArrayInputStream bs = bitmapToInputStream(bitmap);
			bitmap.recycle();
			bitmap = null;
			File imageFile = new File(FileCache.getCacheFileName(imageUrl));
			imageFile.createNewFile();
			OutputStream os = new FileOutputStream(imageFile);
			StreamUtils.writeStream(bs, os);
			StreamUtils.closeQuietly(os);
			StreamUtils.closeQuietly(bs);
		} catch (Throwable e) {
			if (e instanceof MalformedURLException)
				if (Constants.DEBUG_MODE)
					Log.e(TAG, "Bad image URL", e);
			if (e instanceof IOException)
				if (Constants.DEBUG_MODE)
					Log.e(TAG, "Could not get remote image", e);
		}
	}

	private static ByteArrayInputStream bitmapToInputStream(Bitmap src) {
		ByteArrayOutputStream os = new ByteArrayOutputStream(8 * 1024);
		src.compress(compressedPNGImageFormat, cachedImageQuality, os);

		byte[] array = os.toByteArray();
		StreamUtils.closeQuietly(os);
		return new ByteArrayInputStream(array);
	}

	static Bitmap decodeStream(InputStream is) throws IOException {
		if (is == null)
			return null;

		final BitmapFactory.Options mOptions = new BitmapFactory.Options();
		mOptions.inPurgeable = true;
		mOptions.inDither = false;
		Bitmap bitmap = BitmapFactory.decodeStream(is, null, mOptions);

		StreamUtils.closeQuietly(is);

		if (bitmap != null) {
			ImageScale scale = new ImageScale(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
			return scale.transform(bitmap);
		}

		return bitmap;
	}

	public void resetMemoryPurger() {
		mHandler.removeCallbacks(mPurger);
		mHandler.postDelayed(mPurger, MEMORY_PURGE_DELAY);
	}

	private static final class MemoryPurger implements Runnable {

		final ImageCache cache;

		MemoryPurger(final ImageCache cache) {
			this.cache = cache;
		}

		@Override
		public void run() {
			cache.clear();
		}

	}
}