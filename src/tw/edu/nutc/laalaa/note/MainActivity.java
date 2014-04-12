package tw.edu.nutc.laalaa.note;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends FragmentActivity {
	
	public final static String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);
		
	}
	
	public void onNewNoteButtonClick(View v) {
		Log.d(TAG, "New note!");
		Intent intent = new Intent(this, NoteActivity.class);
		intent.putExtra(NoteActivity.EXTRA_NOTE_TIMESTAMP, 1393594645799l);
		startActivity(intent);
	}

}
