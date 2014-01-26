package com.chaitech.catorcrab;

import com.chaitech.catorcrab.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ViewSwitcher;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	    // Let the ViewSwitcher do the animation listening for you
	    ((ViewSwitcher) findViewById(R.id.switcher)).setOnClickListener(new View.OnClickListener() {

	        @Override
	        public void onClick(View v) {
	            ViewSwitcher switcher = (ViewSwitcher) v;

	            if (switcher.getDisplayedChild() == 0) {
	                switcher.showNext();
	            } else {
	                switcher.showPrevious();
	            }
	        }
	    });
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
