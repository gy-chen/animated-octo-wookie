package tw.edu.nutc.laalaa.note.datastore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * NoteStorage
 * 
 * 負責註記與註記內容的永久儲存
 * 
 */
public class NoteStorage {

	private final String TAG = "NoteStorage";
	private long mTimestamp;
	private NoteOpenHelper mOpenHelper;

	public final static int TYPE_EDITTEXT = 1;
	public final static int TYPE_CANVAS = 2;
	public final static int TYPE_PHOTO = 3;

	public NoteStorage(Context context, long timestamp) {
		Log.d(TAG, "received timestamp: " + timestamp);
		mTimestamp = timestamp;
		mOpenHelper = new NoteOpenHelper(context);
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
		Cursor c = db.query(NoteOpenHelper.NoteEntry.TABLE_NAME, projection,
				where, whereArgs, null, null, null, null);
		c.moveToFirst();
		int titleIndex = c
				.getColumnIndexOrThrow(NoteOpenHelper.NoteEntry.COLUMN_NAME_TITLE);
		return c.getString(titleIndex);
	}

	public void addNoteContent(byte[] bytes, int type) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_CONTENT, bytes);
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
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String where = NoteOpenHelper.NoteEntry._ID + " = ?";
		String[] whereArgs = { "" + mTimestamp };

		db.delete(NoteOpenHelper.NoteEntry.TABLE_NAME, where, whereArgs);
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

		db.delete(NoteOpenHelper.NoteContentEntry.TABLE_NAME, where, whereArgs);
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
