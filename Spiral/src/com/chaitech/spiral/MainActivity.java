package com.chaitech.spiral;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private Bitmap bitmap;
	private Canvas canvas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		int X = displayMetrics.widthPixels;
		int Y = displayMetrics.heightPixels;

		Log.d("onCreate", "X=" + X);
		Log.d("onCreate", "Y=" + Y);

		bitmap = Bitmap.createBitmap(X, Y, Config.ARGB_8888);

		canvas = new Canvas(bitmap);

		Paint paint = new Paint();
		paint.setColor(0xffff0000);
		paint.setStrokeWidth(5f);

		float P10 = 0;
		float P20 = (Y - X) / 2;
		float P30 = X - 1;
		float P40 = (Y + X) / 2 - 1;

		// background set to yellow
		Paint fillPaint = new Paint();
		fillPaint.setColor(0xffffff00);
		canvas.drawRect(P10, P20, P30, P40, fillPaint);
		
		// stroke color set to blue
		Paint framePaint = new Paint();
		framePaint.setColor(0xff0000ff);
		framePaint.setStyle(Style.STROKE);
		
		// draw the first square
		canvas.drawRect(P10, P20, P30, P40, framePaint);

		float p = 0.95f;
		float q = 1 - p;
		float P1x, P2x, P3x, P4x, P1y, P2y, P3y, P4y;
		
		float P10x, P10y, P20x, P20y, P30x, P30y, P40x, P40y;
		P10x = P10;
		P10y = P20;
		P20x = P30;
		P20y = P20;
		P30x = P30;
		P30y = P40;
		P40x = P10;
		P40y = P40;

		for (int i = 1; i <= 75; i++) {		
			P1x = p * P10x + q * P20x;
			P1y = p * P10y + q * P20y;
			P2x = p * P20x + q * P30x;
			P2y = p * P20y + q * P30y;
			P3x = p * P30x + q * P40x;
			P3y = p * P30y + q * P40y;
			P4x = p * P40x + q * P10x;
			P4y = p * P40y + q * P10y;
			
			// draw a square by connecting its four corners 
			canvas.drawLine(P1x, P1y, P2x, P2y, framePaint);
			canvas.drawLine(P2x, P2y, P3x, P3y, framePaint);
			canvas.drawLine(P3x, P3y, P4x, P4y, framePaint);
			canvas.drawLine(P4x, P4y, P1x, P1y, framePaint);
			
			P10x = P1x;
			P10y = P1y;
			P20x = P2x;
			P20y = P2y;
			P30x = P3x;
			P30y = P3y;
			P40x = P4x;
			P40y = P4y;
		}

		ImageView imageView = new ImageView(this);
		imageView.setImageBitmap(bitmap);
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bitmap == null) {
					return;
				}

				// Send the public picture file to my friend...
				Uri uri = Uri.parse("smsto:8005551234");   
				Intent it = new Intent(Intent.ACTION_SENDTO, uri);   
				it.putExtra("sms_body", "Hi, just wanted you to know of my spiraling square app... :)");   
				startActivity(it);  
			}

		});

		setContentView(imageView);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
