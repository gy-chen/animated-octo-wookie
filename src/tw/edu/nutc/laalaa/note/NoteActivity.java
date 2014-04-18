package tw.edu.nutc.laalaa.note;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import tw.edu.nutc.laalaa.note.datastore.NoteOpenHelper;
import tw.edu.nutc.laalaa.note.datastore.NoteStorage;
import tw.edu.nutc.laalaa.note.utils.LoadImageAsyncTask;
import tw.edu.nutc.laalaa.note.views.CustomScrollView;
import tw.edu.nutc.laalaa.note.views.FracCanvas;
import tw.edu.nutc.laalaa.note.views.FracEditText;
import tw.edu.nutc.laalaa.note.views.FracImageView;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.Toast;

public class NoteActivity extends ActionBarActivity {

	public final static String EXTRA_NOTE_TIMESTAMP = "timestamp";

	private final String TAG = "NoteActivity";
	private final int REQUEST_TAKE_PHOTO = 4413;

	private LinearLayout mLayout;
	private CustomScrollView mScrollView;
	private ArrayList<Integer> mViewIds = new ArrayList<Integer>();
	private SparseArray<File> mCachedPhotoFiles = new SparseArray<File>();
	private File mCurrentCachedFile = null;
	private View mCurrentWorkingView;
	private FracCanvas.OnDrawListener mOnDrawListener;
	private NoteStorage mNoteStorage;
	private AtomicInteger mCounter = new AtomicInteger(1); // Initial value 1
	private View mCanvasSetting;
	private View mTrashBar;
	private CanvasOnGestureListener mCanvasGestureListener;
	private GestureDetector mCanvasGestureDetector;
	private OnTouchListener mCanvasOnTouchListener;
	private OnClickListener mPhotoOnClickListener;
	private DeleteViewOnGestureListener mDeleteViewOnGestureListener;
	private GestureDetector mDeleteViewGestureDetector;
	private OnTouchListener mDeleteViewOnTouchListener;
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

	public void onTrashButtonClick(View view) {
		Log.d(TAG, "Trash click!");
		if (mCurrentWorkingView != null) {
			deleteView(mCurrentWorkingView);
		} else {
			Log.w(TAG, "Current Working View is null");
		}
		dismissTrashBar();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

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

		// 設定刪除動作的動作
		mDeleteViewOnGestureListener = new DeleteViewOnGestureListener();
		mDeleteViewGestureDetector = new GestureDetector(this,
				mDeleteViewOnGestureListener);
		mDeleteViewOnTouchListener = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mCurrentWorkingView = v;
				Log.d(TAG, "set current working view: " + v.getId());
				mDeleteViewGestureDetector.onTouchEvent(event);
				return false;
			}
		};

		// 設定畫布的選單
		mCanvasGestureListener = new CanvasOnGestureListener();
		mCanvasGestureDetector = new GestureDetector(this,
				mCanvasGestureListener);
		mCanvasOnTouchListener = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mCurrentWorkingView = v;
				Log.d(TAG, "set current working view: " + v.getId());
				mDeleteViewGestureDetector.onTouchEvent(event);
				mCanvasGestureDetector.onTouchEvent(event);
				return false;
			}
		};
		// 避免相片被點選就產生LongPress事件
		mPhotoOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.cancelLongPress();
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
		initNoteStorage(getIntent().getLongExtra(EXTRA_NOTE_TIMESTAMP,
				new Date().getTime()));
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

	/**
	 * 顯示垃圾桶選單
	 * 
	 */
	public void showTrashBar() {
		if (mTrashBar == null) {
			ViewStub viewstub = (ViewStub) findViewById(R.id.viewstub_trashbar);
			viewstub.setVisibility(View.VISIBLE);
			mTrashBar = findViewById(R.id.trashbar);
			mTrashBar.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dismissTrashBar();
				}
			});
		} else {
			mTrashBar.setVisibility(View.VISIBLE);
		}
	}

	public void dismissTrashBar() {
		if (mTrashBar != null) {
			mTrashBar.setVisibility(View.GONE);
		}
	}

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
					String photoPath = new String(bytes);
					File photoFile = new File(photoPath);
					loadPhotoContent(photoFile);
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
		editText.setFocusableInTouchMode(true);
		editText.setOnTouchListener(mDeleteViewOnTouchListener);
		editText.setText(str);
		editText.setId(generateViewId());
		addView(editText);
	}

	private void loadCanvasContent(byte[] bytes) {
		Log.d(TAG, "load canvas");
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

		FracCanvas canvas = new FracCanvas(this);
		canvas.setFocusableInTouchMode(true);
		canvas.setImageBitmap(bitmap);
		canvas.setOnDrawListener(mOnDrawListener);
		canvas.setOnTouchListener(mCanvasOnTouchListener);
		canvas.setId(generateViewId());
		addView(canvas);
	}

	private void loadPhotoContent(File file) {
		Log.d(TAG, "load photo");

		/*
		 * BitmapFactory.Options options = new BitmapFactory.Options();
		 * options.inJustDecodeBounds = true;
		 * BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		 * options.inSampleSize = BitmapUtil.calculateInSampleWidth(options,
		 * mReqWidth); options.inJustDecodeBounds = false; Bitmap bitmap =
		 * BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		 */

		FracImageView photo = new FracImageView(this);
		photo.setFocusableInTouchMode(true);
		photo.setOnTouchListener(mDeleteViewOnTouchListener);
		photo.setOnClickListener(mPhotoOnClickListener);
		photo.setImageResource(R.drawable.spinner_black_48);
		photo.startRotateAnimation();
		photo.setId(generateViewId());
		addView(photo);

		// 紀錄相片物件所使用到的檔案
		mCachedPhotoFiles.put(photo.getId(), file);

		// start load image AsnycTask
		LoadImageAsyncTask asyncTask = new LoadImageAsyncTask(photo, mReqWidth);
		asyncTask.execute(file);
	}

	@Override
	public void onStop() {
		super.onStop();

		// delete old note contents
		mNoteStorage.deleteAllNoteContent();
		// add new note contents
		for (int viewId : mViewIds) {
			View view = findViewById(viewId);
			Log.d(TAG, String.format("store %s in storage", view.getId()));

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
		byte[] bytes;
		File photoFile = mCachedPhotoFiles.get(view.getId());
		if (photoFile != null) {
			bytes = photoFile.getAbsolutePath().getBytes();
			mNoteStorage.addNoteContent(bytes, NoteStorage.TYPE_PHOTO);
		}
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
			newEditText();
			break;
		case NoteStorage.TYPE_CANVAS:
			newCanvas();
			break;
		case NoteStorage.TYPE_PHOTO:
			newPhoto();
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void newEditText() {
		FracEditText view = new FracEditText(this);
		view.setFocusableInTouchMode(true);
		view.setOnTouchListener(mDeleteViewOnTouchListener);
		int viewId = generateViewId();
		view.setId(viewId);
		addView(view);
	}

	private void newCanvas() {
		FracCanvas view = new FracCanvas(this);
		view.setFocusableInTouchMode(true);
		view.setDrawingCacheEnabled(true);
		view.setOnDrawListener(mOnDrawListener);
		view.setOnTouchListener(mCanvasOnTouchListener);
		int viewId = generateViewId();
		view.setId(viewId);
		addView(view);
	}

	private void newPhoto() {
		// start a intent for take a photo
		File newPhotoFile = prepareNewPhotoFile();
		if (newPhotoFile != null) {
			Intent intent = new Intent();
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			mCurrentCachedFile = newPhotoFile;
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(mCurrentCachedFile));
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivityForResult(intent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	/**
	 * 準備儲存在外部空間的相片檔
	 * 
	 * 當無法在外部儲存空間建立檔案時，會使用Toast顯示訊息， 並回傳null
	 * 
	 * @return File|null
	 */
	private File prepareNewPhotoFile() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File storageDir = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"Notes");

			if (!storageDir.exists()) {
				if (!storageDir.mkdirs()) {
					Log.w(TAG, "failed to create pictures directory");
					Toast.makeText(this,
							R.string.toast_create_pictures_directory_error,
							Toast.LENGTH_LONG);
					return null;
				}
			}
			File storageFile;
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());
			storageFile = new File(storageDir, "IMG_" + timeStamp + ".jpg");
			Log.d(TAG,
					"make a external photo file: "
							+ storageFile.getAbsolutePath());
			return storageFile;
		} else {
			Toast.makeText(this, R.string.toast_externalstorage_error,
					Toast.LENGTH_LONG).show();
			return null;
		}
	}

	/**
	 * 將指定的View新增至Layout中
	 * 
	 * @param view
	 */
	protected void addView(View view) {
		mLayout.addView(view);
		mViewIds.add(view.getId());
		// Log.d(TAG, "view id: " + view.getId());
		// 延遲時間讓ScrollView可滑動到底
		mLayout.postDelayed(new Runnable() {

			@Override
			public void run() {
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		}, 50);
	}

	protected void deleteView(View view) {
		// check view id
		int viewId = view.getId();
		int viewIndex = mViewIds.indexOf(viewId);
		if (viewIndex == -1) {
			Log.w(TAG, "Unknown view id: " + viewId);
			return;
		}
		// remove the view from layout
		mLayout.removeView(view);
		mViewIds.remove(viewIndex);
		mLayout.clearDisappearingChildren();
		// remove cached files of that view
		File cachedFile = mCachedPhotoFiles.get(viewId);
		if (cachedFile != null) {
			cachedFile.delete();
			mCachedPhotoFiles.remove(viewId);
			Log.d(TAG, "remove cached photo file");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK
				&& mCurrentCachedFile != null) {
			Log.d(TAG, "Receive a bitmap from other activity");
			// set up FracImageView
			FracImageView view = new FracImageView(this);
			view.setFocusableInTouchMode(true);
			view.setOnTouchListener(mDeleteViewOnTouchListener);

			view.setOnClickListener(mPhotoOnClickListener);
			int viewId = generateViewId();
			view.setId(viewId);

			// set loading animation
			view.setImageResource(R.drawable.spinner_black_48);
			view.startRotateAnimation();

			// load image in background
			LoadImageAsyncTask loadImageAsyncTask = new LoadImageAsyncTask(
					view, mReqWidth);
			loadImageAsyncTask.execute(mCurrentCachedFile);
			mCachedPhotoFiles.put(viewId, mCurrentCachedFile);
			mCurrentCachedFile = null;

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

	private class DeleteViewOnGestureListener extends SimpleOnGestureListener {

		@Override
		public void onLongPress(MotionEvent event) {
			// // 跳出確認對話框
			// DeleteDialogFragment dialog = new DeleteDialogFragment();
			// dialog.setDeleteDialogListener(mDeleteDialogListner);
			// dialog.show(getSupportFragmentManager(), "delete_dialog");
			// // 如果已確認，刪除指定畫布
			// Log.d(TAG, "canvas| onLongPress: " + getCurrentView().getId());
			showTrashBar();
		}
	}

}
