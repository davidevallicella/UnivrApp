//package test;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadPoolExecutor;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.os.IBinder;
//import android.util.Log;
//
//import com.cellasoft.univrapp.Application;
//import com.cellasoft.univrapp.ConnectivityReceiver;
//import com.cellasoft.univrapp.Constants;
//import com.cellasoft.univrapp.Settings;
//import com.cellasoft.univrapp.model.Image;
//import com.cellasoft.univrapp.model.Image.Images;
//import com.cellasoft.univrapp.utils.ImageFetcher;
//import com.github.droidfu.services.BetterService;
//
//public class DownloadingService extends BetterService {
//	private static final int DEFAULT_POOL_SIZE = 2;
//	private static final int UPDATE_INTERVAL = 1000 * 30;
//
//	private Object synRoot = new Object();
//	private Object synDownload = new Object();
//	private boolean downloading = false;
//	private int totalDownloads = 0;
//	private ThreadPoolExecutor executor;
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		return null;
//	}
//
//	@SuppressWarnings("deprecation")
//	@Override
//	public void onStart(Intent intent, int startId) {
//		super.onStart(intent, startId);
//		if (executor == null) {
//			executor = (ThreadPoolExecutor) Executors
//					.newFixedThreadPool(DEFAULT_POOL_SIZE);
//		}
//
//		new Thread(new Runnable() {
//			public void run() {
//				startDownloadingImages();
//			}
//		}).start();
//	}
//
//	@Override
//	public void onDestroy() {
//		if (Constants.DEBUG_MODE)
//			Log.d(Constants.LOG_TAG, "Destroy Downloading Service!");
//		super.onDestroy();
//		stopDownloadingImages();
//	}
//
//	@Override
//	public void onLowMemory() {
//		if (Constants.DEBUG_MODE)
//			Log.d(Constants.LOG_TAG, "LowMemory Downloading Service!");
//		super.onLowMemory();
//		stopDownloadingImages();
//	}
//
//	private void stopDownloadingImages() {
//		synchronized (synRoot) {
//			downloading = false;
//		}
//		if (executor != null) {
//			executor.shutdownNow();
//		}
//	}
//
//	protected void startDownloadingImages() {
//		if (!Settings.getDownloadImages()) {
//			return;
//		}
//
//		synchronized (synRoot) {
//			if (downloading)
//				return;
//			downloading = true;
//		}
//
//		if (Constants.DEBUG_MODE)
//			Log.d(Constants.LOG_TAG, "Start downloading service at "
//					+ new Date());
//
//		if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()
//				&& Settings.getDownloadImages()) {
//			try {
//				downloadImages();
//			} catch (Throwable t) {
//				t.printStackTrace();
//			}
//		}
//
//		synchronized (synRoot) {
//			downloading = false;
//		}
//
//		if (Constants.DEBUG_MODE)
//			Log.d(Constants.LOG_TAG, "Stop downloading service at "
//					+ new Date());
//
//		if (Settings.getDownloadImages()) {
//			scheduleNextDownload();
//		}
//		stopSelf();
//	}
//
//	private void downloadImages() {
//		ArrayList<Image> queuedImages = Images.loadAllQueuedImages();
//		totalDownloads = queuedImages.size();
//
//		for (final Image image : queuedImages) {
//			if (!downloading)
//				return;
//
//			try {
//				image.status = Image.IMAGE_STATUS_DOWNLOADING;
//				image.save();
//
//				downloadImage(image.url, image.id, new DownloadCallback() {
//					public void onComplete() {
//						synchronized (synDownload) {
//							totalDownloads--;
//						}
//						image.status = Image.IMAGE_STATUS_DOWNLOADED;
//						image.save();
//					}
//
//					public void onSkip() {
//						synchronized (synDownload) {
//							totalDownloads--;
//						}
//					}
//
//					public void onFail() {
//						synchronized (synDownload) {
//							totalDownloads--;
//						}
//						if (image.retries == Image.MAX_RETRIES) {
//							image.status = Image.IMAGE_STATUS_FAILED;
//						} else {
//							image.status = Image.IMAGE_STATUS_QUEUED;
//							image.increaseRetries();
//						}
//
//						image.save();
//					}
//				});
//			} catch (Exception e) {
//				synchronized (synDownload) {
//					totalDownloads--;
//				}
//				e.printStackTrace();
//			}
//		}
//
//		while (totalDownloads > 0 && downloading) {
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				break;
//			}
//		}
//	}
//
//	private void downloadImage(final String imageUrl, final int imageId,
//			final DownloadCallback callback) {
//		if (ImageFetcher.getInstance().isCached(imageUrl)) {
//			callback.onComplete();
//			return;
//		}
//
//		try {
//			executor.execute(new Runnable() {
//				public void run() {
//					try {
//						if (downloading) {
//							ImageFetcher.getInstance().processBitmap(imageUrl);
//							callback.onComplete();
//						} else {
//							callback.onSkip();
//						}
//					} catch (Throwable e) {
//						e.printStackTrace();
//						callback.onFail();
//					}
//				}
//			});
//		} catch (Throwable t) {
//			callback.onFail();
//		}
//	}
//
//	private void scheduleNextDownload() {
//		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//		Intent intent = new Intent(this, DownloadingService.class);
//		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
//				0);
//		long firstWake = System.currentTimeMillis() + UPDATE_INTERVAL;
//		am.set(AlarmManager.RTC, firstWake, pendingIntent);
//	}
//
//	public static void cancelScheduledDownloads() {
//		Context context = Application.getInstance();
//		AlarmManager am = (AlarmManager) context
//				.getSystemService(Context.ALARM_SERVICE);
//		Intent intent = new Intent(context, DownloadingService.class);
//		PendingIntent pendingIntent = PendingIntent.getService(
//				Application.getInstance(), 0, intent, 0);
//		am.cancel(pendingIntent);
//	}
//
//	private interface DownloadCallback {
//		void onComplete();
//
//		void onSkip();
//
//		void onFail();
//	}
//}