package com.chaitech.webwebview;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;

public class JabberWockyActivity extends Activity {
	private MediaPlayer mp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jabberwocky);
		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.loadUrl("file:///android_asset/jabberwocky.html");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.jabber_wocky, menu);
		return true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mp = MediaPlayer.create(this, R.raw.psychopath);
		mp.start();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mp.stop();
		mp.release();
	}
	
	public void openWikiPage(View view) {
		String url = "http://en.wikipedia.org/wiki/Jabberwocky";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	public void loadPicture(View view) {
		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.loadUrl("file:///android_asset/jabberwocky.jpg");
	}
}
