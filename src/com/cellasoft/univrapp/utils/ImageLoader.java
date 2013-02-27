package com.cellasoft.univrapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cellasoft.univrapp.activity.R;

/**
 * Realizes an background image loader backed by a two-level FIFO cache. If the
 * image to be loaded is present in the cache, it is set immediately on the
 * given view. Otherwise, a thread from a thread pool will be used to download
 * the image in the background and set the image on the view as soon as it
 * completes.
 * 
 * @author Matthias Kaeppler
 */
public class ImageLoader implements Runnable {
	public static final int CONNECT_TIMEOUT = 5 * 1000;
	public static final int READ_TIMEOUT = 10 * 1000;

	private static ThreadPoolExecutor executor;

	private static ImageCache imageCache;

	private static final int DEFAULT_POOL_SIZE = 2;

	public static final int BITMAP_DOWNLOADED_FAILED = 0;
	public static final int BITMAP_DOWNLOADED_SUCCESS = 1;

	static final String BITMAP_EXTRA = "droidfu:extra_bitmap";

	private static int numAttempts = 3;

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
		ImageLoader.numAttempts = numAttempts;
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
			executor = (ThreadPoolExecutor) Executors
					.newFixedThreadPool(DEFAULT_POOL_SIZE);
		}
		if (imageCache == null) {
			imageCache = ImageCache.createInstance(context, 50, 5);
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
			imageCache.clear();
		}
	}

	public void run() {
		Bitmap bitmap = downloadImage(imageUrl);
		notifyImageLoaded(bitmap);
	}

	public static final int THUMBNAIL_HEIGHT = 50;
	public static final int THUMBNAIL_WIDTH = 50;

	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
	// byte[] byteArray = baos.toByteArray();

	private Bitmap downloadImage(String imageUrl) {
		Bitmap bitmap = null;
		if (imageUrl == null || imageUrl.equals("")) {
			return bitmap;
		}

		int timesTried = 1;
		while (timesTried <= numAttempts) {
			try {
				URL url = new URL(imageUrl);
				URLConnection con = url.openConnection();
				con.setConnectTimeout(READ_TIMEOUT);
				con.setReadTimeout(READ_TIMEOUT);
				bitmap = createThumbnail(con.getInputStream());
				synchronized (imageCache) {
					imageCache.put(imageUrl, bitmap);
				}
				break;
			} catch (Throwable e) {
				e.printStackTrace();
				Log.w(ImageLoader.class.getSimpleName(), "download for "
						+ imageUrl + " failed (attempt " + timesTried + ")");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
				}

				timesTried++;
			}
		}
		return bitmap;
	}

	private Bitmap createThumbnail(InputStream is) throws IOException {
		Bitmap bitmap = null;
		bitmap = BitmapFactory.decodeStream(is);
		if (null != bitmap) {
			is.close();
			Float width = Float.valueOf(bitmap.getWidth());
			Float height = Float.valueOf(bitmap.getHeight());
			Float ratio = width / height;
			bitmap = Bitmap.createScaledBitmap(bitmap,
					(int) (THUMBNAIL_HEIGHT * ratio), THUMBNAIL_HEIGHT, false);
		} else {
			bitmap = BitmapFactory.decodeResource(Application.getInstance().getResources(), R.drawable.thumb);
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
}