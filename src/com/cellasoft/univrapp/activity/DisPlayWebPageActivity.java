package com.cellasoft.univrapp.activity;

import static com.cellasoft.univrapp.utils.LogUtils.LOGD;
import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cellasoft.univrapp.BuildConfig;
import com.cellasoft.univrapp.ConnectivityReceiver;
import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Item;
import com.cellasoft.univrapp.utils.FileUtils;
import com.cellasoft.univrapp.utils.FontUtils;
import com.cellasoft.univrapp.utils.Html;
import com.cellasoft.univrapp.utils.StreamUtils;
import com.cellasoft.univrapp.utils.UIUtils;
import com.cellasoft.univrapp.widget.NonLeakingWebView;
import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;

public class DisPlayWebPageActivity extends Activity {
	private static final String TAG = makeLogTag(DisPlayWebPageActivity.class
			.getName());

	public static final String ITEM_ID_PARAM = "ItemId";
	public static final String CHANNEL_ID_PARAM = "ChannelId";

	private NonLeakingWebView webView;

	private Item currentItem;
	private String page_url;
	protected volatile boolean running;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onCreate()");
			// UIUtils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.item_webview);

		Intent in = getIntent();
		page_url = in.getStringExtra("page_url");
		int id = in.getIntExtra(ITEM_ID_PARAM, 0);
		currentItem = new Item(id);
		running = true;
		init();
	}

	private void init() {
		webView = (NonLeakingWebView) findViewById(R.id.itemWebView);
		webView.setProgressBar((ProgressBar) findViewById(R.id.progressBar));

		UIUtils.setHardwareAccelerated(this, webView, true);
		UIUtils.keepScreenOn(this, true);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		FontUtils.setRobotoFont(this, (ViewGroup) getWindow().getDecorView());
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onStart()");
		}
		super.onStart();
		loadPage();
	}

	@Override
	protected void onStop() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onStop()");
		}

		webView.stopLoading();
		webView.clearCache(false);
		webView.freeMemory();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (BuildConfig.DEBUG) {
			LOGD(TAG, "onDestroy()");
		}
		running = false;
		unbindWebView();
		currentItem = null;
		super.onDestroy();
	}

	private void unbindWebView() {
		webView.setFocusable(true);
		webView.removeAllViews();
		webView.clearHistory();
		webView.destroy();
		webView = null;
	}

	private void loadPage() {
		BetterAsyncTask<Void, Void, String> task = new BetterAsyncTask<Void, Void, String>(
				this) {

			@Override
			protected void after(final Context context, final String html) {
				if (running) {
					runOnUiThread(new Runnable() {
						public void run() {
							try {
								webView.showArticle(html);
							} catch (Exception e) {
								handleError(
										context,
										new Exception(
												"Errore durante la visualizzazione! Segnala il bug!"));
							}
						}
					});
				}
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
				if (ConnectivityReceiver.hasGoodEnoughNetworkConnection()) {
					currentItem = Item.findItemById(currentItem.id,
							ContentManager.FULL_ITEM_LOADER);

					String html = StreamUtils.readFromUrl(page_url);
					return modifyHtml(html);
				} else
					throw new UnivrReaderException(getResources().getString(
							R.string.univrapp_connection_exception));
			}
		});
		task.disableDialog();

		UIUtils.execute(task, (Void[]) null);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	private String modifyHtml(String html) {
		DateFormat dateFormat = new SimpleDateFormat(
				"MMMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault());
		String article = FileUtils.getFileFromAssets(getApplicationContext(),
				"article.html");

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
				attach.append("<tr>")
						.append("<td><img style=\"width: 100%;\" src=\"attachment.png\" /></td>")
						.append("<td>" + file + "</td>").append("</tr>");
			}

			files.clear();
			files = null;
			article = article.replace("{files}", attach.toString());
		}

		return article;
	}
}