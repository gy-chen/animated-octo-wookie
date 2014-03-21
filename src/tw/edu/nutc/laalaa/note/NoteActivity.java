package tw.edu.nutc.laalaa.note;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import tw.edu.nutc.laalaa.note.datastore.NoteOpenHelper;
import tw.edu.nutc.laalaa.note.datastore.NoteStorage;
import tw.edu.nutc.laalaa.note.utils.BitmapUtil;
import tw.edu.nutc.laalaa.note.utils.CustomScrollView;
import tw.edu.nutc.laalaa.note.views.FracCanvas;
import tw.edu.nutc.laalaa.note.views.FracEditText;
import tw.edu.nutc.laalaa.note.views.FracImageView;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class NoteActivity extends Activity {

	public final static String EXTRA_NOTE_TIMESTAMP = "timestamp";

	private final String TAG = "NoteActivity";
	private final int REQUEST_TAKE_PHOTO = 4413;

	private LinearLayout mLayout;
	private CustomScrollView mScrollView;
	private ArrayList<Integer> mViewIds = new ArrayList<Integer>();
	private SparseArray<File> mCachedPhotoFiles = new SparseArray<File>();
	private File mCurrentCachedFile = null;
	private FracCanvas.OnDrawListener mOnDrawListener;
	private NoteStorage mNoteStorage;
	private AtomicInteger mCounter = new AtomicInteger(1); // Initial value 1
	private View mCanvasSetting;
	private GestureDetector mCanvasGestureDetector;
	private OnTouchListener mCanvasOnTouchListener;
	private int mReqWidth;

	/**
	 * 當選擇顏色的按鈕被按下時觸發的事件
	 * 
	 * @param view
	 */
	public void onCanvasChooseColorButtonClick(View view) {
		int viewId = view.getId();
		Paint newPaint;
		switch (viewId) {
		case R.id.button_red_color:
			newPaint = FracCanvas.RED_PAINT;
			break;
		case R.id.button_blue_color:
			newPaint = FracCanvas.BLUE_PAINT;
			break;
		case R.id.button_yellow_color:
			newPaint = FracCanvas.YELLOW_PAINT;
			break;
		case R.id.button_green_color:
			newPaint = FracCanvas.GREEN_PAINT;
			break;
		case R.id.button_orange_color:
			newPaint = FracCanvas.ORANGE_PAINT;
			break;
		case R.id.button_purple_color:
			newPaint = FracCanvas.PURPLE_PAINT;
			break;
		case R.id.button_black_color:
			newPaint = FracCanvas.BLACK_PAINT;
			break;
		default:
			newPaint = FracCanvas.BLACK_PAINT;
			Log.w(TAG, "Unknown view call onChooseColorButtonClick method");
		}
		FracCanvas.setCurrentPaint(newPaint);
		toggleCanvasSettingMenu();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set view width
		mReqWidth = getResources().getDisplayMetrics().widthPixels;

		// 當開始繪圖時，停止ScrollView滑動
		mOnDrawListener = new FracCanvas.OnDrawListener() {

			@Override
			public void onStopDraw() {
				Log.d(TAG, "start scrolling");
				mScrollView.setScrollingEnabled(true);
			}

			@Override
			public void onStartDraw() {
				Log.d(TAG, "stop scrolling");
				mScrollView.setScrollingEnabled(false);
			}
		};

		// 設定畫布的選單
		mCanvasGestureDetector = new GestureDetector(this,
				new CanvasOnGestureListener());
		mCanvasOnTouchListener = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mCanvasGestureDetector.onTouchEvent(event);
				return false;
			}
		};

		mLayout = (LinearLayout) findViewById(R.id.linear_layout);
		mScrollView = (CustomScrollView) findViewById(R.id.scrollview);

		Button addEditText = (Button) findViewById(R.id.add_edittext);
		addEditText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addView(NoteStorage.TYPE_EDITTEXT);
			}
		});

		Button addCanvas = (Button) findViewById(R.id.add_canvas);
		addCanvas.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addView(NoteStorage.TYPE_CANVAS);
			}
		});

		Button addImageView = (Button) findViewById(R.id.add_image);
		addImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addView(NoteStorage.TYPE_PHOTO);
			}
		});

		Button menuButton = (Button) findViewById(R.id.menu_test);
		menuButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggleCanvasSettingMenu();
			}
		});

		// 初始化註記資料庫
		// initNoteStorage((new Date()).getTime());
		initNoteStorage(getIntent().getLongExtra(EXTRA_NOTE_TIMESTAMP,
				1393594645799l));
		// mNoteStorage.setTitle("Demo");
		// Log.d(TAG, "Note title: " + mNoteStorage.getTitle());

		// 讀回資料庫內容
		loadNoteStorageContent();
	}

	/**
	 * 切換畫布設定選單的顯示狀態
	 * 
	 */
	public void toggleCanvasSettingMenu() {
		if (mCanvasSetting == null) {
			ViewStub viewstub = (ViewStub) findViewById(R.id.viewstub_canvassetting);
			viewstub.setVisibility(View.VISIBLE);
			mCanvasSetting = findViewById(R.id.canvas_setting_view);
		} else {
			mCanvasSetting
					.setVisibility(mCanvasSetting.getVisibility() == View.VISIBLE ? View.INVISIBLE
							: View.VISIBLE);
		}

		// animation
		if (mCanvasSetting.getVisibility() == View.VISIBLE) {
			mCanvasSetting.startAnimation(AnimationUtils.loadAnimation(
					NoteActivity.this, R.anim.fade_in));
		} else {
			mCanvasSetting.startAnimation(AnimationUtils.loadAnimation(
					NoteActivity.this, R.anim.fade_out));
		}
	}

	/*
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) {
	 * super.onPrepareOptionsMenu(menu); menu.clear(); MenuInflater inflater =
	 * getMenuInflater(); inflater.inflate(mMenuRes, menu); Log.d(TAG,
	 * "onPrepareOptionsMenu"); return true; }
	 */

	private void loadNoteStorageContent() {
		Cursor c = mNoteStorage.getNoteContents();
		if (c.moveToFirst()) {
			do {
				int contentIndex = c
						.getColumnIndex(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_CONTENT);
				int typeIndex = c
						.getColumnIndex(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_TYPE);
				byte[] bytes = c.getBlob(contentIndex);
				int type = c.getInt(typeIndex);
				if (type == NoteStorage.TYPE_EDITTEXT) {
					loadEditTextContent(bytes);
				} else if (type == NoteStorage.TYPE_CANVAS) {
					loadCanvasContent(bytes);
				} else if (type == NoteStorage.TYPE_PHOTO) {
					loadPhotoContent(bytes);
				} else {
					Log.w(TAG, "Unknown type: " + type);
				}
			} while (c.moveToNext());
		}
	}

	private void loadEditTextContent(byte[] bytes) {
		Log.d(TAG, "load edittext");

		String str = new String(bytes);

		FracEditText editText = new FracEditText(this);
		editText.setText(str);
		editText.setId(generateViewId());
		addView(editText);
	}

	private void loadCanvasContent(byte[] bytes) {
		Log.d(TAG, "load canvas");
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

		FracCanvas canvas = new FracCanvas(this);
		canvas.setImageBitmap(bitmap);
		canvas.setOnDrawListener(mOnDrawListener);
		canvas.setOnTouchListener(mCanvasOnTouchListener);
		canvas.setId(generateViewId());
		addView(canvas);
	}

	private void loadPhotoContent(byte[] bytes) {
		Log.d(TAG, "load photo");

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		options.inSampleSize = BitmapUtil.calculateInSampleWidth(options,
				mReqWidth);
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
				options);

		FracImageView photo = new FracImageView(this);
		photo.setImageBitmap(bitmap);
		photo.setId(generateViewId());
		addView(photo);
	}

	@Override
	public void onStop() {
		super.onStop();

		// delete old note contents
		mNoteStorage.deleteAllNoteContent();
		// add new note contents
		for (int viewId : mViewIds) {
			View view = findViewById(viewId);

			if (view instanceof FracEditText) {
				addEditTextToStorage((FracEditText) view);
			} else if (view instanceof FracCanvas) {
				addCanvasToStorage((FracCanvas) view);
			} else if (view instanceof FracImageView) {
				addPhotoToStorage((FracImageView) view);
			} else {
				Log.w(TAG, "Unknown view type: " + view);
			}
		}

		// clear cached photos
		for (int i = 0; i < mCachedPhotoFiles.size(); i++) {
			int key = mCachedPhotoFiles.keyAt(i);
			mCachedPhotoFiles.get(key).delete();
		}
		
		Log.d(TAG, "onStop: saved");
		Log.d(TAG, "saved contents json: " + mNoteStorage.toJSON());
	}

	private void addEditTextToStorage(FracEditText view) {
		String text = view.getText().toString();
		byte[] bytes = text.getBytes();
		mNoteStorage.addNoteContent(bytes, NoteStorage.TYPE_EDITTEXT);
	}

	private void addCanvasToStorage(FracCanvas view) {
		// convert the canvas to bitmap
		// Drawable drawable = view.getDrawingCache();
		// Bitmap bitmap = convertDrawableToBitmap(drawable);
		// Bitmap bitmap = view.getDrawingCache();
		Bitmap bitmap = convertViewToBitmap(view);

		// get bitmap's bytes
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
		byte[] bytes = output.toByteArray();

		mNoteStorage.addNoteContent(bytes, NoteStorage.TYPE_CANVAS);
	}

	private void addPhotoToStorage(FracImageView view) {
		Drawable drawable = view.getDrawable();
		// TODO: load cached bitmap instead
		Bitmap bitmap = convertDrawableToBitmap(drawable);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
		byte[] bytes = output.toByteArray();

		mNoteStorage.addNoteContent(bytes, NoteStorage.TYPE_PHOTO);
	}

	private Bitmap convertDrawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	private Bitmap convertViewToBitmap(View view) {
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		return bitmap;
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
		case NoteStorage.TYPE_EDITTEXT:
			addEditText();
			break;
		case NoteStorage.TYPE_CANVAS:
			addCanvas();
			break;
		case NoteStorage.TYPE_PHOTO:
			addPhoto();
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void addEditText() {
		FracEditText view = new FracEditText(this);
		int viewId = generateViewId();
		view.setId(viewId);
		addView(view);
	}

	private void addCanvas() {
		FracCanvas view = new FracCanvas(this);
		view.setDrawingCacheEnabled(true);
		view.setOnDrawListener(mOnDrawListener);
		view.setOnTouchListener(mCanvasOnTouchListener);
		int viewId = generateViewId();
		view.setId(viewId);
		addView(view);
	}

	private void addPhoto() {
		// start a intent for take a photo
		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		File cacheDir = getExternalCacheDir();
		try {
			mCurrentCachedFile = File.createTempFile("cachePhoto", ".jpg",
					cacheDir);
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(mCurrentCachedFile));
		} catch (IOException e) {
			Log.d(TAG, e.toString());
		}
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(intent, REQUEST_TAKE_PHOTO);
		}
	}

	protected void addView(View view) {
		mLayout.addView(view);
		mViewIds.add(view.getId());
		// 延遲時間讓ScrollView可滑動到底
		mLayout.postDelayed(new Runnable() {

			@Override
			public void run() {
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		}, 50);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			Log.d(TAG, "Receive a bitmap from other activity");
			FracImageView view = new FracImageView(this);
			int viewId = generateViewId();
			view.setId(viewId);
			// use cached photo file if it is exists
			Bitmap imageBitmap;
			if (mCurrentCachedFile == null) {
				Bundle extras = data.getExtras();
				imageBitmap = (Bitmap) extras.get("data");
			} else {
				// downsampling
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(mCurrentCachedFile.getAbsolutePath(),
						options);
				options.inSampleSize = BitmapUtil.calculateInSampleWidth(
						options, mReqWidth);
				options.inJustDecodeBounds = false;
				imageBitmap = BitmapFactory.decodeFile(
						mCurrentCachedFile.getAbsolutePath(), options);
				mCachedPhotoFiles.put(viewId, mCurrentCachedFile);
				mCurrentCachedFile = null;
			}
			view.setImageBitmap(imageBitmap);
			addView(view);
		}
	}

	/**
	 * 產生View的Id
	 * 
	 * @return
	 */
	private int generateViewId() {
		return mCounter.getAndIncrement();
	}

	private void initNoteStorage(long timestamp) {
		mNoteStorage = new NoteStorage(this, timestamp);
		mNoteStorage.createNote();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class CanvasOnGestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			toggleCanvasSettingMenu();
			return true;
		}
	}
}
