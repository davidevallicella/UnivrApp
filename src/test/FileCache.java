package test;

import java.io.File;
import java.util.List;

import android.content.Context;

import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Image;
import com.cellasoft.univrapp.utils.Utils;

public class FileCache {
	private static FileCache instance;
	private static File cacheDir;
	private String secondLevelCacheDir;

	private FileCache(Context context) {
		// Find the dir to save cached images
		//cacheDir = Utils.getBestCacheDir(context, "/imagecache");
		this.secondLevelCacheDir = cacheDir.getAbsolutePath();
	}

	public static synchronized FileCache createInstance(Context context) {
		if (instance == null)
			instance = new FileCache(context);
		return instance;
	}

	public static synchronized FileCache getInstance() {
		return instance;
	}

	public static File getImageFile(String imageUrl) {
		return new File(getCacheFileName(imageUrl));
	}

	public static boolean isCached(String imageUrl) {
		File file = new File(getCacheFileName(imageUrl));
		return file.exists();
	}

	public static String getCacheFileName(String imageUrl) {
		long imageId = 0;
		int pos = imageUrl.lastIndexOf('#');
		if (pos > 0) {
			try {
				imageId = Integer.parseInt(imageUrl.substring(pos + 1));
			} catch (NumberFormatException nfe) {
			}
		}
		if (imageId == 0) {
			Image image = ContentManager.loadImage(imageUrl);
			if (image == null)
				return "";
			imageId = image.id;
		}
		return getCacheFileName(imageId);
	}

	public static String getCacheFileName(long imageId) {
		return instance.secondLevelCacheDir + "/" + imageId;
	}

	public static void clearCacheIfNecessary() {
		List<Integer> oldestImageIds = ContentManager.loadOldestImageIds(2000);
		for (int imageId : oldestImageIds) {
			try {
				File imageFile = new File(getCacheFileName(imageId));
				imageFile.delete();

				ContentManager.deleteImage(imageId);
			} catch (RuntimeException e) {
			}
		}
	}

	public static void clearCacheFolder() {
		if (cacheDir == null && !cacheDir.exists())
			return;

		File[] files = cacheDir.listFiles();
		if (files != null) {
			for (File file : files) {
				try {
					file.delete();
				} catch (Throwable t) {
				}
			}
		}
	}

}