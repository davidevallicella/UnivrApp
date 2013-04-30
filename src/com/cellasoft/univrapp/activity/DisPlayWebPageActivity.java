package com.cellasoft.univrapp.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.utils.FileUtils;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.Html;
import com.cellasoft.univrapp.utils.StreamUtils;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;

public class DisPlayWebPageActivity extends Activity {

	public static final String ITEM_ID_PARAM = "ItemId";
	public static final String CHANNEL_ID_PARAM = "ChannelId";

	WebView webview;
	private int itemId = 0;
	Item currentItem;
	String page_url;
	ProgressBar web_progressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.item_webview);

		Intent in = getIntent();
		page_url = in.getStringExtra("page_url");
		itemId = in.getIntExtra(ITEM_ID_PARAM, 0);
		webview = (WebView) findViewById(R.id.itemWebView);

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		webview.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType(mimetype);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		webview.getSettings().setPluginsEnabled(false);
		webview.getSettings().setAllowFileAccess(true);
		webview.getSettings().setRenderPriority(RenderPriority.HIGH);
		webview.getSettings().setAppCacheEnabled(true);
		webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webview.setScrollBarStyle(0);
		webview.setWebViewClient(new MyWebViewClient());
		web_progressBar = (ProgressBar) findViewById(R.id.web_progressBar);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadPage();
	}

	@Override
	protected void onDestroy() {
		webview.freeMemory();
		super.onDestroy();
	}

	private void loadPage() {
		BetterAsyncTask<Void, Void, String> task = new BetterAsyncTask<Void, Void, String>(
				this) {

			@Override
			protected void after(Context context, final String html) {
				runOnUiThread(new Runnable() {
					public void run() {
						showArticle(html);
					}
				});

			}

			@Override
			protected void handleError(Context context, Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				finish();

			}
		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, String>() {

			@Override
			public String call(BetterAsyncTask<Void, Void, String> task)
					throws Exception {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

				if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
					currentItem = ContentManager.loadItem(itemId,
							ContentManager.FULL_ITEM_LOADER,
							ContentManager.LIGHTWEIGHT_CHANNEL_LOADER);
					return StreamUtils.readFromUrl(page_url, "utf-8");
				} else
					throw new UnivrReaderException(getResources().getString(
							R.string.univrapp_connection_exception));
			}
		});
		task.disableDialog();
		task.execute();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
		}

		return super.onKeyDown(keyCode, event);
	}

	private String modifyHtml(String html) {
		DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy  HH:mm");
		String article = FileUtils.getFileFromAssets("article.html");

		article = article.replace("{content}", Html.parserPage(html))
				.replace("{title_url}", currentItem.link)
				.replace("{title}", currentItem.title)
				.replace("{date}", dateFormat.format(currentItem.pubDate));

		List<String> files = Html.getAttachment(html);
		if (!files.isEmpty()) {
			Document doc = Jsoup.parse(article);
			doc.select("div#attachment").removeAttr("style");
			article = doc.html();
			StringBuilder attach = new StringBuilder();
			for (String file : files) {
				System.out.println("----" + file);
				attach.append("<tr>");
				attach.append("<td><img style=\"width: 100%;\" src=\"attachment.png\" /></td>");
				attach.append("<td><div class=\"file\"><a href=\"" + file
						+ "\">" + Html.getFileNameToPath(file)
						+ "</a></div></td>");
				attach.append("</tr>");
			}

			article = article.replace("{files}", attach.toString());
		}

		return article;
	}

	private void showArticle(String html) {
		// parse the html, load it in the webview
		String parsedHtml = modifyHtml(html);
		webview.loadDataWithBaseURL("file:///android_asset/", parsedHtml,
				"text/html", "utf-8", null);

		// scroll the webview up to the top
		webview.scrollTo(0, 0);

	}

	class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			webview.loadUrl(url);
			return true;
		}

		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.d("WebView", "onPageStarted");
			super.onPageStarted(view, url, favicon);
			web_progressBar.setVisibility(View.VISIBLE);
		}

		public void onPageFinished(WebView view, String url) {
			Log.d("WebView", "onPageFinished ");
			super.onPageFinished(view, url);
			web_progressBar.setVisibility(View.GONE);
		}
	}

}