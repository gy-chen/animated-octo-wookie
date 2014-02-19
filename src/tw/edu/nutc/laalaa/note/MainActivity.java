package tw.edu.nutc.laalaa.note;

import tw.edu.nutc.laalaa.note.views.FracCanvas;
import tw.edu.nutc.laalaa.note.views.FracEditText;
import tw.edu.nutc.laalaa.note.views.FracImageView;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MainActivity extends Activity {

	private LinearLayout mLayout;
	private ScrollView mScrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mLayout = (LinearLayout) findViewById(R.id.linear_layout);
		mScrollView = (ScrollView) findViewById(R.id.scrollview);
		final Context context = this;
		
		Button addEditText = (Button) findViewById(R.id.add_edittext);
		addEditText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mLayout.addView(new FracEditText(context));
				mLayout.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
					}
				}, 50);
			}
		});
		
		Button addCanvas = (Button) findViewById(R.id.add_canvas);
		addCanvas.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FracCanvas newCanvas = new FracCanvas(context);
				mLayout.addView(newCanvas);
				mLayout.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
					}
				}, 50);
			}
		});
		
		Button addImageView = (Button) findViewById(R.id.add_image);
		addImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FracImageView imageView = new FracImageView(context);
				imageView.setImageResource(R.drawable.p1);
				mLayout.addView(imageView);
				mLayout.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
					}
				}, 50);
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
