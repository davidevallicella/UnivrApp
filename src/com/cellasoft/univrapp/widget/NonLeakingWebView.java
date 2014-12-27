package com.cellasoft.univrapp.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.utils.UIUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import static com.cellasoft.univrapp.utils.LogUtils.LOGD;

/**
 * see http://stackoverflow.com/questions/3130654/memory-leak-in-webview and
 * http://code.google.com/p/android/issues/detail?id=9375 Note that the bug does
 * NOT appear to be fixed in android 2.2 as romain claims
 * <p/>
 * Also, you must call {@link #destroy()} from your activity's onDestroy method.
 */
public class NonLeakingWebView extends WebView {
    private static final String ASSETS_PATH = "file:///android_asset/";
    private static Field sConfigCallback;
    transient private static ProgressBar web_progressBar;

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

    public NonLeakingWebView(final Context context, final AttributeSet attrs) {
        super(context.getApplicationContext(), attrs);
        init(context);
    }

    public NonLeakingWebView(final Context context, final AttributeSet attrs,
                             final int defStyle) {
        super(context.getApplicationContext(), attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {
        setWebViewClient(new MyWebViewClient((Activity) context));
        getSettings().setAllowFileAccess(true);
        getSettings().setAppCacheEnabled(true);
        getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url,
                                        final String userAgent, final String contentDisposition,
                                        final String mimetype, final long contentLength) {
                UIUtils.safeOpenLink(context, new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url)).setType(mimetype));
            }
        });
    }

    public void showArticle(final String html) throws Exception {
        if (html != null && html.length() > 0) {
            loadDataWithBaseURL(ASSETS_PATH, html, "text/html", "UTF-8", null);
            // scroll the webview up to the top
            scrollTo(0, 0);
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        if (sConfigCallback != null) {
            try {
                sConfigCallback.set(null, null);
            } catch (Exception e) {
                // throw new RuntimeException(e);
            }
        }
    }

    public void setProgressBar(ProgressBar pbar) {
        web_progressBar = pbar;
    }

    protected static class MyWebViewClient extends WebViewClient {
        transient protected WeakReference<Activity> activityRef;

        public MyWebViewClient(Activity activity) {
            super();
            this.activityRef = new WeakReference<Activity>(activity);
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view,
                                                final String url) {
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

        @Override
        public void onPageStarted(final WebView view, final String url,
                                  final Bitmap favicon) {
            if (BuildConfig.DEBUG) {
                LOGD("WebView", "onPageStarted");
            }
            super.onPageStarted(view, url, favicon);
            if (web_progressBar != null) {
                web_progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            if (BuildConfig.DEBUG) {
                LOGD("WebView", "onPageFinished ");
            }
            super.onPageFinished(view, url);
            if (web_progressBar != null) {
                web_progressBar.setVisibility(View.GONE);
            }
        }
    }
}