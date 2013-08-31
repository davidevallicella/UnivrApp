package com.cellasoft.univrapp.widget;

import static com.cellasoft.univrapp.utils.LogUtils.LOGD;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.utils.UIUtils;

/**
 * see http://stackoverflow.com/questions/3130654/memory-leak-in-webview and
 * http://code.google.com/p/android/issues/detail?id=9375 Note that the bug does
 * NOT appear to be fixed in android 2.2 as romain claims
 * 
 * Also, you must call {@link #destroy()} from your activity's onDestroy method.
 */
public class NonLeakingWebView extends WebView {
	private static Field sConfigCallback;
	private static ProgressBar web_progressBar;
	private static final String ASSETS_PATH = "file:///android_asset/";
	static {
		try {
			sConfigCallback = Class.forName("android.webkit.BrowserFrame")
					.getDeclaredField("sConfigCallback");
			sConfigCallback.setAccessible(true);
		} catch (Exception e) {
			// ignored
		}

	}

	public NonLeakingWebView(Context context) {
		super(context.getApplicationContext());
		init(context);
	}

	public NonLeakingWebView(Context context, AttributeSet attrs) {
		super(context.getApplicationContext(), attrs);
		init(context);
	}

	public NonLeakingWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context.getApplicationContext(), attrs, defStyle);
		init(context);
	}

	private void init(final Context context) {
		setWebViewClient(new MyWebViewClient((Activity) context));
		getSettings().setAllowFileAccess(true);
		getSettings().setRenderPriority(RenderPriority.HIGH);
		getSettings().setAppCacheEnabled(true);
		getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

		setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				UIUtils.safeOpenLink(context, new Intent(Intent.ACTION_VIEW,
						Uri.parse(url)).setType(mimetype));
			}
		});
	}

	public void setProgressBar(ProgressBar pbar) {
		web_progressBar = pbar;
	}

	public void showArticle(String html) throws Exception {
		if (html != null && html.length() > 0) {
			loadDataWithBaseURL(ASSETS_PATH, html, "text/html", "UTF-8", null);
			// scroll the webview up to the top
			scrollTo(0, 0);
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		try {
			if (sConfigCallback != null)
				sConfigCallback.set(null, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected static class MyWebViewClient extends WebViewClient {
		protected WeakReference<Activity> activityRef;

		public MyWebViewClient(Activity activity) {
			this.activityRef = new WeakReference<Activity>(activity);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
				final Activity activity = activityRef.get();
				if (activity != null) {
					UIUtils.safeOpenLink(activity, new Intent(
							Intent.ACTION_VIEW, Uri.parse(url)));
				}
			} catch (RuntimeException ignored) {
				// ignore any url parsing exceptions
			}
			return true;
		}

		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (BuildConfig.DEBUG) {
				LOGD("WebView", "onPageStarted");
			}
			super.onPageStarted(view, url, favicon);
			web_progressBar.setVisibility(View.VISIBLE);
		}

		public void onPageFinished(WebView view, String url) {
			if (BuildConfig.DEBUG) {
				LOGD("WebView", "onPageFinished ");
			}
			super.onPageFinished(view, url);
			web_progressBar.setVisibility(View.GONE);
		}
	}
}