package com.chaitech.headortail;

import java.security.SecureRandom;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class MainActivity extends Activity {
	private SecureRandom secureRandom;

	public MainActivity() {
		secureRandom = new SecureRandom();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		flipper.setFlipInterval(120);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Called when the user taps */
	public void flipCoin(View view) {
		// Do something in response to button click

		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);

		TextView textView = (TextView) findViewById(R.id.textView2);
		if (flipper.isFlipping()) {
			flipper.stopFlipping();
			flipper.clearAnimation();

			boolean head = secureRandom.nextBoolean();
			if (head) {
				flipper.setDisplayedChild(1);
				textView.setText(R.string.head_text);
			} else {
				flipper.setDisplayedChild(0);
				textView.setText(R.string.tail_text);
			}
		} else {
			textView.setText("");
			flipper.startFlipping();
			flipper.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.flip));
		}
	}

}
