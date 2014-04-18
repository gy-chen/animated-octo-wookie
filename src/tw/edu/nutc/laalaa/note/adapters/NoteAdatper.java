package tw.edu.nutc.laalaa.note.adapters;

import tw.edu.nutc.laalaa.note.datastore.NoteOpenHelper;
import tw.edu.nutc.laalaa.note.views.NoteButtonView;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class NoteAdatper extends BaseAdapter {

	private final static String TAG = "NoteAdapter";
	private Context mContext;
	private NoteOpenHelper mNoteOpenHelper;
	private Cursor mNoteCursor;

	public NoteAdatper(Context context) {
		mContext = context;
		mNoteOpenHelper = new NoteOpenHelper(context);
	}

	@Override
	public int getCount() {
		Log.d(TAG, "count: " + getNoteCount());
		return getNoteCount() + 1; // add start button
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 新增註記簿按鈕
		if (position == 0) {
			ImageView button = new ImageView(mContext);
			button.setImageResource(android.R.drawable.btn_star);
			button.setBackgroundResource(android.R.drawable.btn_default);
			button.setTag(null);
			return button;
		}
		else {
			NoteButtonView button = new NoteButtonView(mContext);
			// position minus 1 for the start button
			int dbPosition = position - 1;
			long timestamp = getNoteTimestamp(dbPosition);
			button.setTitle(getNoteTitle(dbPosition), timestamp);
			button.setTag(timestamp);
			return button;
		}
	}

	private int getNoteCount() {
		SQLiteDatabase db = mNoteOpenHelper.getReadableDatabase();

		String sql = "SELECT COUNT(*) FROM "
				+ NoteOpenHelper.NoteEntry.TABLE_NAME;
		Cursor c = db.rawQuery(sql, null);
		Log.d(TAG, "column count: " + c.getColumnCount());
		c.moveToFirst();
		int result = c.getInt(0);
		c.close();
		return result;
	}

	private String getNoteTitle(int position) {
		if (mNoteCursor == null) {
			mNoteCursor = initNoteCursor();
		}
		mNoteCursor.moveToPosition(position);
		int titleIndex = mNoteCursor.getColumnIndex(NoteOpenHelper.NoteEntry.COLUMN_NAME_TITLE);
		Log.d(TAG, "title index: " + titleIndex);
		return mNoteCursor.getString(titleIndex);
	}

	private long getNoteTimestamp(int position) {
		if (mNoteCursor == null) {
			mNoteCursor = initNoteCursor();
		}
		mNoteCursor.moveToPosition(position);
		int timestampIndex = mNoteCursor.getColumnIndex(NoteOpenHelper.NoteEntry._ID);
		Log.d(TAG, "timestamp index: " + timestampIndex);
		return mNoteCursor.getLong(timestampIndex);
	}

	private Cursor initNoteCursor() {
		SQLiteDatabase db = mNoteOpenHelper.getReadableDatabase();

		String[] columns = { NoteOpenHelper.NoteEntry.COLUMN_NAME_TITLE,
				NoteOpenHelper.NoteEntry._ID };
		Cursor c = db.query(NoteOpenHelper.NoteEntry.TABLE_NAME, columns, null,
				null, null, null, null);
		return c;
	}
}
