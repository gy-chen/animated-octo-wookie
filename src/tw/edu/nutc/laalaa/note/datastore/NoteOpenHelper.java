package tw.edu.nutc.laalaa.note.datastore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NoteOpenHelper extends SQLiteOpenHelper {

	private static final String TAG = "NoteOpenHelper";
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "notes.db";

	public NoteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE Note (id TIMESTAMP NOT NULL PRIMARY KEY, title VARCHAR(45));");
		db.execSQL("CREATE TABLE NoteContent (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, content BLOB NOT NULL, type INT NOT NULL, note_id TIMESTAMP NOT NULL, FOREIGN KEY (note_id) REFERENCES Note (id) ON DELETE CASCADE ON UPDATE CASCADE);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Logs that the database is being upgraded
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");

		// Kills the table and existing data
		db.execSQL("DROP TABLE IF EXISTS NoteContent");
		db.execSQL("DROP TABLE IF EXISTS Note");

		// Recreates the database with a new version
		onCreate(db);
	}

}
