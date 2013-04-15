package com.cellasoft.univrapp.utils;

import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cellasoft.univrapp.Constants;
import com.github.droidfu.adapters.WebGalleryAdapter;
import com.github.droidfu.http.BetterHttpResponse;
import com.github.droidfu.widgets.WebImageView;

public class ImageLoader implements Runnable {
	private static final String TAG = ImageLoader.class.getSimpleName();

	public static final int CONNECT_TIMEOUT = 5 * 1000;
	public static final int READ_TIMEOUT = 10 * 1000;
	private static ThreadPoolExecutor executor;

	private static ImageCache imageCache;

	private static final int DEFAULT_POOL_SIZE = 2;

	public static final int BITMAP_DOWNLOADED_FAILED = 0;
	public static final int BITMAP_DOWNLOADED_SUCCESS = 1;

	protected static final int DEFAULT_RETRY_HANDLER_SLEEP_TIME = 2000;
	private static final int DEFAULT_NUM_RETRIES = 3;

	static final String BITMAP_EXTRA = "droidfu:extra_bitmap";

	protected static int numRetries = DEFAULT_NUM_RETRIES;

	/**
	 * @param numThreads
	 *            the maximum number of threads that will be started to download
	 *            images in parallel
	 */
	public static void setThreadPoolSize(int numThreads) {
		executor.setMaximumPoolSize(numThreads);
	}

	/**
	 * @param numAttempts
	 *            how often the image loader should retry the image download if
	 *            network connection fails
	 */
	public static void setMaxDownloadAttempts(int numAttempts) {
		ImageLoader.numRetries = numAttempts;
	}

	/**
	 * This method must be called before any other method is invoked on this
	 * class. Please note that when using ImageLoader as part of
	 * {@link WebImageView} or {@link WebGalleryAdapter}, then there is no need
	 * to call this method, since those classes will already do that for you.
	 * This method is idempotent. You may call it multiple times without any
	 * side effects.
	 * 
	 * @param context
	 *            the current context
	 */
	public static synchronized void initialize(Context context) {
		if (executor == null) {
			executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
					DEFAULT_POOL_SIZE, new LowestPriorityThreadFactory());
		}
		if (imageCache == null) {
			imageCache = ImageCache.createInstance(context, 70, 5);
		}
	}

	private String imageUrl;

	private Handler handler;

	private ImageLoader(String imageUrl) {
		this(imageUrl, new ImageLoaderHandler());
	}

	private ImageLoader(String imageUrl, ImageLoaderHandler handler) {
		this.imageUrl = imageUrl;
		this.handler = handler;
	}

	/**
	 * Triggers the image loader for the given image and handler. The image
	 * loading will be performed concurrently to the UI main thread, using a
	 * fixed size thread pool. The loaded image will not be automatically posted
	 * to an ImageView; instead, you can pass a custom
	 * {@link ImageLoaderHandler} and handle the loaded image yourself (e.g.
	 * cache it for later use).
	 * 
	 * @param imageUrl
	 *            the URL of the image to download
	 * @param handler
	 *            the handler which is used to handle the downloaded image
	 */
	public static void start(String imageUrl, ImageLoaderHandler handler) {
		ImageLoader loader = new ImageLoader(imageUrl, handler);
		synchronized (imageCache) {
			Bitmap image = imageCache.get(imageUrl);
			if (image == null) {
				// fetch the image in the background
				executor.execute(loader);
			} else {
				loader.notifyImageLoaded(image);
			}
		}
	}

	public static Bitmap get(String imageUrl) {
		synchronized (imageCache) {
			return imageCache.get(imageUrl);
		}
	}

	/**
	 * Clears the 1st-level cache (in-memory cache). A good candidate for
	 * calling in {@link android.app.Application#onLowMemory()}.
	 */
	public static void clearCache() {
		synchronized (imageCache) {
			imageCache.resetMemoryPurger();
		}
	}

	public void run() {
		Bitmap bitmap = downloadImage(imageUrl);
		notifyImageLoaded(bitmap);
	}

	private Bitmap downloadImage(String imageUrl) {
		if (imageUrl == null || imageUrl.equals("")) {
			return null;
		}

		Bitmap bitmap = null;
		if (Constants.DEBUG_MODE)
			Log.d("ImageLoader", "Download (" + imageUrl + ")");

		int timesTried = 1;

		while (timesTried <= numRetries) {
			try {
				// The bitmap isn't cached so download from the web
				BetterHttpResponse response = HttpUtility.get(imageUrl);
				InputStream is = response.getResponseBody();
				bitmap = ImageCache.decodeStream(is);

				// save in 1st level cache hit (memory)
				synchronized (imageCache) {
					imageCache.put(imageUrl, bitmap);
				}
				break;
			} catch (Throwable e) {
				if (e instanceof OutOfMemoryError) {
					if (Constants.DEBUG_MODE)
						Log.e(TAG, "Out of memory", e);
					clearCache();
				}
				Log.w(TAG, "download for " + imageUrl + " failed (attempt "
						+ timesTried + ")");
				try {
					Thread.sleep(DEFAULT_RETRY_HANDLER_SLEEP_TIME);
				} catch (InterruptedException e1) {
				}

				timesTried++;
			}
		}

		return bitmap;
	}

	public void notifyImageLoaded(Bitmap bitmap) {
		if (handler == null)
			return;

		Message message = new Message();
		message.what = bitmap != null ? BITMAP_DOWNLOADED_SUCCESS
				: BITMAP_DOWNLOADED_FAILED;
		if (bitmap != null) {
			Bundle data = new Bundle();
			data.putParcelable(BITMAP_EXTRA, bitmap);
			message.setData(data);
		}

		handler.sendMessage(message);
	}

	static class LowestPriorityThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(final Runnable r) {
			final Thread t = new Thread(r);
			t.setPriority(Thread.MIN_PRIORITY);
			return t;
		}

	}
}