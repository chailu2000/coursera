package com.chaitech.webwebview;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;

public class RoundBallActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.loadUrl("file:///android_asset/roundball/roundball.html");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.round_ball, menu);
		return true;
	}

}
