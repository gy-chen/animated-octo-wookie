package tw.edu.nutc.laalaa.note.datastore;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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

	public final static int ENVIROMENT_KEY_TITLE = 1;
	public final static int ENVIROMENT_KEY_SNAPSHOT = 2;

	private final static String KEY_TIMESTAMP = "timestamp";
	private final static String KEY_TITLE = "title";
	private final static String KEY_CONTENTS = "contents";
	private final static String KEY_INDEX = "index";
	private final static String KEY_CONTENT = "content";
	private final static String KEY_TYPE = "type";

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
		values.put(NoteOpenHelper.NoteMetaEntry._ID, ENVIROMENT_KEY_TITLE);
		values.put(NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_NOTE_ID, mTimestamp);
		values.put(NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_CONTENT, title);

		db.insertWithOnConflict(NoteOpenHelper.NoteMetaEntry.TABLE_NAME, null,
				values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	/**
	 * 取得註記簿的標題
	 * 
	 * @return
	 */
	public String getTitle() {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		String[] projection = { NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_CONTENT };
		String where = NoteOpenHelper.NoteMetaEntry._ID + " = ? AND "
				+ NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_NOTE_ID + " = ?";
		String[] whereArgs = { "" + ENVIROMENT_KEY_TITLE, "" + mTimestamp };
		Cursor c = db.query(NoteOpenHelper.NoteMetaEntry.TABLE_NAME,
				projection, where, whereArgs, null, null, null, null);
		if (c.getCount() != 0) {
			c.moveToFirst();
			int titleIndex = c
					.getColumnIndexOrThrow(NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_CONTENT);
			byte[] blob = c.getBlob(titleIndex);
			return new String(blob);
		} else {
			return null;
		}
	}

	public void setSnapshot(Bitmap bitmap) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 80, output);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(NoteOpenHelper.NoteMetaEntry._ID, ENVIROMENT_KEY_SNAPSHOT);
		values.put(NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_NOTE_ID, mTimestamp);
		values.put(NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_CONTENT,
				output.toByteArray());

		db.insertWithOnConflict(NoteOpenHelper.NoteMetaEntry.TABLE_NAME, null,
				values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public Bitmap getSnapshot() {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		String[] projection = { NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_CONTENT };
		String where = NoteOpenHelper.NoteMetaEntry._ID + " = ? AND "
				+ NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_NOTE_ID + " = ?";
		String[] whereArgs = { Integer.toString(ENVIROMENT_KEY_SNAPSHOT),
				Long.toString(mTimestamp) };
		Cursor c = db.query(NoteOpenHelper.NoteMetaEntry.TABLE_NAME,
				projection, where, whereArgs, null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			int contentIndex = c
					.getColumnIndex(NoteOpenHelper.NoteMetaEntry.COLUMN_NAME_CONTENT);
			byte[] content = c.getBlob(contentIndex);
			return BitmapFactory.decodeByteArray(content, 0, content.length);
		} else {
			return null;
		}
	}

	/**
	 * 新增此註記簿的註記內容
	 * 
	 * @param bytes
	 * @param type
	 *            註記種類
	 */
	public void addNoteContent(byte[] bytes, int type) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_CONTENT, bytes);
		values.put(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_TYPE, type);
		values.put(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_NOTE_ID,
				mTimestamp);

		db.insert(NoteOpenHelper.NoteContentEntry.TABLE_NAME, null, values);
	}

	/**
	 * 取得此註記簿的註記內容
	 * 
	 * @return
	 */
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
	 * 實際建立註記簿
	 * 
	 * 如果註記簿已經存在，則此操作不會有什麼影響
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

	/**
	 * 將此註記簿轉換成JSON格式
	 * 
	 * 輸出的JSON格式為： {"timestamp": 筆記簿的timestamp, "title": 筆記簿標題 "content": [{
	 * "index": 筆記順序, "content": 筆記內容, "type": 筆記種類}, ...] } 筆記種類的代表意義為： 1: 文字
	 * 2: 畫布 3: 相片 筆記內容皆用Base64轉換成ASCII可編碼內容。
	 * 
	 * 當從相片的路徑讀取內容時發生錯誤，會以null值代替相片內容。
	 * 
	 * @return JSON字串
	 */
	public String toJSON() {
		try {
			JSONObject mainJson = new JSONObject();

			mainJson.put(KEY_TIMESTAMP, mTimestamp);
			String noteTitle = getTitle();
			mainJson.put(KEY_TITLE, noteTitle == null ? JSONObject.NULL
					: noteTitle);

			JSONArray contentsJson = new JSONArray();
			Cursor contentCursor = getNoteContents();
			if (contentCursor.moveToFirst()) {
				do {
					JSONObject contentJson = new JSONObject();
					int contentIndex = contentCursor
							.getColumnIndex(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_CONTENT);
					int typeIndex = contentCursor
							.getColumnIndex(NoteOpenHelper.NoteContentEntry.COLUMN_NAME_TYPE);
					int type = contentCursor.getInt(typeIndex);
					byte[] bytes = contentCursor.getBlob(contentIndex);
					contentJson.put(KEY_INDEX, contentCursor.getPosition());
					contentJson.put(KEY_TYPE, type);
					if (type == TYPE_PHOTO) {
						byte[] output = decodePhotoFileContent(bytes);
						if (output == null) {
							contentJson.put(KEY_CONTENT, JSONObject.NULL);
						} else {
							contentJson.put(KEY_CONTENT, Base64.encodeToString(
									output, Base64.DEFAULT));
						}
					} else {
						contentJson.put(KEY_CONTENT,
								Base64.encodeToString(bytes, Base64.DEFAULT));
					}
					contentsJson.put(contentJson);
				} while (contentCursor.moveToNext());
			}

			mainJson.put(KEY_CONTENTS, contentsJson);
			return mainJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			assert false;
			return null;
		}
	}

	private byte[] decodePhotoFileContent(byte[] path) {
		String filePath = new String(path);
		File file = new File(filePath);
		DataInputStream input = null;
		try {
			input = new DataInputStream(new FileInputStream(file));
			byte[] result = new byte[(int) file.length()];
			input.readFully(result);
			return result;
		} catch (FileNotFoundException e) {
			Log.w(TAG, filePath + " not found.");
			return null;
		} catch (IOException e) {
			Log.w(TAG, "cannot read " + filePath + ". " + e.getMessage());
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					Log.w(TAG,
							"cannot close " + filePath + ". " + e.getMessage());
				}
			}
		}
	}
}
