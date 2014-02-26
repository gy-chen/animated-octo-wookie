package tw.edu.nutc.laalaa.note;

import java.util.ArrayList;

import tw.edu.nutc.laalaa.note.utils.CustomScrollView;
import tw.edu.nutc.laalaa.note.views.FracCanvas;
import tw.edu.nutc.laalaa.note.views.FracEditText;
import tw.edu.nutc.laalaa.note.views.FracImageView;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class NoteActivity extends Activity {

	private final String TAG = "NoteActivity";
	private final int REQUEST_TAKE_PHOTO = 4413;

	private LinearLayout mLayout;
	private CustomScrollView mScrollView;
	private ArrayList<Integer> mViewIds = new ArrayList<Integer>();
	private FracCanvas.OnDrawListener mOnDrawListener;

	protected final int TYPE_EDITTEXT = 1;
	protected final int TYPE_CANVAS = 2;
	protected final int TYPE_PHOTO = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 當開始繪圖時，停止ScrollView滑動
		mOnDrawListener = new FracCanvas.OnDrawListener() {

			@Override
			public void onStopDraw() {
				mScrollView.setScrollingEnabled(true);
			}

			@Override
			public void onStartDraw() {
				mScrollView.setScrollingEnabled(false);
			}
		};

		mLayout = (LinearLayout) findViewById(R.id.linear_layout);
		mScrollView = (CustomScrollView) findViewById(R.id.scrollview);

		Button addEditText = (Button) findViewById(R.id.add_edittext);
		addEditText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addView(TYPE_EDITTEXT);
			}
		});

		Button addCanvas = (Button) findViewById(R.id.add_canvas);
		addCanvas.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addView(TYPE_CANVAS);
			}
		});

		Button addImageView = (Button) findViewById(R.id.add_image);
		addImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addView(TYPE_PHOTO);
			}
		});
	}

	/**
	 * 新增一個筆記物件至筆記簿
	 * 
	 * 丟入的筆記物件型態不合法時，丟出IllegalArgumentException
	 * 
	 * @param type
	 *            筆記物件型態
	 */
	protected void addView(int type) {
		switch (type) {
		case TYPE_EDITTEXT:
			addEditText();
			break;
		case TYPE_CANVAS:
			addCanvas();
			break;
		case TYPE_PHOTO:
			addPhoto();
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	protected void addEditText() {
		FracEditText view = new FracEditText(this);
		int viewId = View.generateViewId();
		mViewIds.add(viewId);
		view.setId(viewId);
		addView(view);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	protected void addCanvas() {
		FracCanvas view = new FracCanvas(this);
		view.setOnDrawListener(mOnDrawListener);
		int viewId = View.generateViewId();
		mViewIds.add(viewId);
		view.setId(viewId);
		addView(view);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	protected void addPhoto() {
		// start a intent for take a photo
		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(intent, REQUEST_TAKE_PHOTO);
		}
	}

	protected void addView(View view) {
		mLayout.addView(view);
		// 延遲時間讓ScrollView可滑動到底
		mLayout.postDelayed(new Runnable() {

			@Override
			public void run() {
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		}, 50);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			Log.d(TAG, "Receive a bitmap from other activity");
			Bundle extras = data.getExtras();
			Bitmap imageBitmap = (Bitmap) extras.get("data");
			FracImageView view = new FracImageView(this);
			view.setImageBitmap(imageBitmap);
			int viewId = View.generateViewId();
			mViewIds.add(viewId);
			view.setId(viewId);
			addView(view);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
