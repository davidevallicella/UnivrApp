package com.cellasoft.univrapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cellasoft.univrapp.criteria.LatestItems;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Item;

public class DisPlayWebPageActivity extends Activity {

	public static final String ITEM_ID_PARAM = "ItemId";
	  public static final String CHANNEL_ID_PARAM = "ChannelId";
	
	WebView webview;
	private Item currentItem;
	 private int itemId = 0;
	  private int channelId = LatestItems.ALL_CHANNELS;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		Intent in = getIntent();
		String page_url = in.getStringExtra("page_url");
		itemId = getIntent().getIntExtra(ITEM_ID_PARAM, itemId);
		channelId = getIntent().getIntExtra(CHANNEL_ID_PARAM, channelId);
		
		webview = (WebView) findViewById(R.id.webpage);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadUrl(page_url);

		webview.setWebViewClient(new DisPlayWebPageActivityClient());
	}
	
	 @Override
	  protected void onStart() {
	    super.onStart();
	    
	    currentItem = ContentManager.loadItem(itemId,
	        ContentManager.FULL_ITEM_LOADER,
	        ContentManager.LIGHTWEIGHT_CHANNEL_LOADER);        
	    
	  }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private class DisPlayWebPageActivityClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
	
	

}