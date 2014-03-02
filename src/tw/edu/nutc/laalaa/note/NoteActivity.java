package tw.edu.nutc.laalaa.note;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import tw.edu.nutc.laalaa.note.datastore.NoteOpenHelper;
import tw.edu.nutc.laalaa.note.utils.CustomScrollView;
import tw.edu.nutc.laalaa.note.views.FracCanvas;
import tw.edu.nutc.laalaa.note.views.FracEditText;
import tw.edu.nutc.laalaa.note.views.FracImageView;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
	private NoteStorage mNoteStorage;
	private AtomicInteger mCounter = new AtomicInteger(1); // Initial value 1

	protected final static int TYPE_EDITTEXT = 1;
	protected final static int TYPE_CANVAS = 2;
	protected final static int TYPE_PHOTO = 3;

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

		// 初始化註記資料庫
		// initNoteStorage((new Date()).getTime());
		initNoteStorage(1393594645799l);
		// mNoteStorage.setTitle("Demo");
		// Log.d(TAG, "Note title: " + mNoteStorage.getTitle());

		// 讀回資料庫內容
		loadNoteStorageContent();
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
				if (type == TYPE_EDITTEXT) {
					loadEditTextContent(bytes);
				} else if (type == TYPE_CANVAS) {
					loadCanvasContent(bytes);
				} else if (type == TYPE_PHOTO) {
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
		canvas.setId(generateViewId());
		addView(canvas);
	}

	private void loadPhotoContent(byte[] bytes) {
		Log.d(TAG, "load photo");
		
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		
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

		Log.d(TAG, "onStop: saved");
	}

	private void addEditTextToStorage(FracEditText view) {
		String text = view.getText().toString();
		byte[] bytes = text.getBytes();
		mNoteStorage.addNoteContent(bytes, TYPE_EDITTEXT);
	}

	private void addCanvasToStorage(FracCanvas view) {
		// convert the canvas to bitmap
		// Drawable drawable = view.getDrawingCache();
		// Bitmap bitmap = convertDrawableToBitmap(drawable);
		//Bitmap bitmap = view.getDrawingCache();
		Bitmap bitmap = convertViewToBitmap(view);

		// get bitmap's bytes
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
		byte[] bytes = output.toByteArray();

		mNoteStorage.addNoteContent(bytes, TYPE_CANVAS);
	}

	private void addPhotoToStorage(FracImageView view) {
		Drawable drawable = view.getDrawable();
		Bitmap bitmap = convertDrawableToBitmap(drawable);
		/*File dir = Environment.getExternalStorageDirectory();
		try {
			File tempFile = File.createTempFile("demo", ".png", dir);
			tempFile.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
		byte[] bytes = output.toByteArray();

		mNoteStorage.addNoteContent(bytes, TYPE_PHOTO);
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
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
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

	protected void addEditText() {
		FracEditText view = new FracEditText(this);
		int viewId = generateViewId();
		view.setId(viewId);
		addView(view);
	}

	protected void addCanvas() {
		FracCanvas view = new FracCanvas(this);
		view.setDrawingCacheEnabled(true);
		view.setOnDrawListener(mOnDrawListener);
		int viewId = generateViewId();
		view.setId(viewId);
		addView(view);
	}

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
			Bundle extras = data.getExtras();
			Bitmap imageBitmap = (Bitmap) extras.get("data");
			FracImageView view = new FracImageView(this);
			view.setImageBitmap(imageBitmap);
			int viewId = generateViewId();
			view.setId(viewId);
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
		mNoteStorage = new NoteStorage(timestamp);
		mNoteStorage.createNote();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * NoteStorage
	 * 
	 * 負責註記與註記內容的永久儲存
	 * 
	 */
	public class NoteStorage {

		private long mTimestamp;
		private NoteOpenHelper mOpenHelper;

		public NoteStorage(long timestamp) {
			Log.d(TAG, "received timestamp: " + timestamp);
			mTimestamp = timestamp;
			mOpenHelper = new NoteOpenHelper(NoteActivity.this);
		}

		/**
		 * 設定註記簿的標題
		 * 
		 * @param title
		 *            標題名稱
		 */
		public void setTitle(String title) {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(NoteOpenHelper.NoteEntry.COLUMN_NAME_TITLE, title);

			db.update(NoteOpenHelper.NoteEntry.TABLE_NAME, values,
					NoteOpenHelper.NoteEntry._ID + " = ?", new String[] { ""
							+ mTimestamp });
		}

		/**
		 * 取得註記簿的標題
		 * 
		 * @return
		 */
		public String getTitle() {
			SQLiteDatabase db = mOpenHelper.getReadableDatabase();
			String[] projection = { NoteOpenHelper.NoteEntry.COLUMN_NAME_TITLE };
			String where = NoteOpenHelper.NoteEntry._ID + " = ?";
			String[] whereArgs = { "" + mTimestamp };
			Cursor c = db.query(NoteOpenHelper.NoteEntry.TABLE_NAME,
					projection, where, whereArgs, null, null, null, null);
			c.moveToFirst();
			int titleIndex = c
					.getColumnIndexOrThrow(NoteOpenHelper.NoteEntry.COLUMN_NAME_TITLE);
			return c.getString(titleIndex);
		}

		public void addNoteContent(byte[] bytes, int type) {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_CONTENT,
					bytes);
			values.put(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_TYPE, type);
			values.put(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_NOTE_ID,
					mTimestamp);

			db.insert(NoteOpenHelper.NoteContentEntry.TABLE_NAME, null, values);
		}

		public Cursor getNoteContents() {
			SQLiteDatabase db = mOpenHelper.getReadableDatabase();
			String[] projection = {
					NoteOpenHelper.NoteContentEntry.COLUMN_NAME_CONTENT,
					NoteOpenHelper.NoteContentEntry.COLUMN_NAME_TYPE };
			String where = NoteOpenHelper.NoteContentEntry.COLUMN_NAME_NOTE_ID
					+ " = ?";
			String[] whereArgs = { "" + mTimestamp };
			Cursor c = db.query(NoteOpenHelper.NoteContentEntry.TABLE_NAME,
					projection, where, whereArgs, null, null, null, null);
			return c;
		}

		/**
		 * 刪除註記簿
		 * 
		 */
		public void deleteNote() {

		}

		/**
		 * 刪除所有註記簿的內容
		 * 
		 */
		public void deleteAllNoteContent() {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();

			String where = NoteOpenHelper.NoteContentEntry.COLUMN_NAME_NOTE_ID
					+ " = ?";
			String[] whereArgs = { "" + mTimestamp };

			db.delete(NoteOpenHelper.NoteContentEntry.TABLE_NAME, where,
					whereArgs);
		}

		/**
		 * 實際建立筆記簿
		 * 
		 * 如果筆記簿已經存在，則此操作不會有什麼影響
		 */
		public void createNote() {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(NoteOpenHelper.NoteEntry._ID, mTimestamp);

			long rowId = db.insertWithOnConflict(
					NoteOpenHelper.NoteEntry.TABLE_NAME, null, values,
					SQLiteDatabase.CONFLICT_IGNORE);

			Log.d(TAG, "New note id: " + rowId);
			assert rowId == mTimestamp;
		}
	}

}
