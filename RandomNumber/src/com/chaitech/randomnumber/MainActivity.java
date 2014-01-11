package com.chaitech.randomnumber;

import java.security.SecureRandom;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private SecureRandom secureRandom;
	
	public MainActivity() {
		secureRandom = new SecureRandom();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/** Called when the user touches the button */
	public void getNextRandom(View view) {
	    // Do something in response to button click
		float nextFloat = secureRandom.nextFloat();
		TextView textView = (TextView) findViewById(R.id.textView1);
		textView.setText(String.valueOf(nextFloat));
	}
	
}
