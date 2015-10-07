package io.reon;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import io.reon.api.R;

public class MainWebActivity extends Activity implements LocalService.ConnectionListener<WebContext> {
	LocalService.Connection<WebContext> myServiceConnection;
	protected WebView webView;
	protected ReonWebViewClient myWebViewClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webView = configureWebView();
		myServiceConnection = new LocalService.Connection<WebContext>(this, Service.class).withListener(this);
	}

	protected WebView configureWebView() {
		setContentView(R.layout.activity_main_web);
		WebView w = (WebView) findViewById(R.id.webView);
		w.getSettings().setLoadWithOverviewMode(true);
		w.getSettings().setBuiltInZoomControls(false);
		w.getSettings().setJavaScriptEnabled(true);
		w.getSettings().setDatabaseEnabled(true);
		w.getSettings().setDomStorageEnabled(true);
		w.getSettings().setAllowContentAccess(true);
		w.getSettings().setAppCacheEnabled(true);
		return w;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myServiceConnection!= null) {
			myServiceConnection.close();
			myServiceConnection = null;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		webView.resumeTimers();
	}

	@Override
	protected void onStop() {
		super.onStop();
		webView.pauseTimers();
	}

	@Override
	public void onServiceConnected(WebContext service) {
		if (myWebViewClient == null) {
			myWebViewClient = new ReonWebViewClient(service, getPackageName());
		}
		webView.setWebViewClient(myWebViewClient);
		webView.loadUrl(myWebViewClient.getRootUrl());
	}

	@Override
	public void onServiceDisconnected(WebContext service) {
	}

	@Override
	public void finish() {
		webView.clearHistory();
		webView.clearCache(true);
		webView.loadUrl("about:blank");
		super.finish();
	}
}


