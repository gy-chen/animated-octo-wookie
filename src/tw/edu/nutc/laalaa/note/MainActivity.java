package tw.edu.nutc.laalaa.note;

import tw.edu.nutc.laalaa.note.adapters.NoteAdatper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class MainActivity extends FragmentActivity {
	
	public final static String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);
		
		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new NoteAdatper(this));
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				Object timestamp = v.getTag();
				if (timestamp == null) {
					// TODO delete test value
					openNote(1393594645799l);
				}
				else {
					openNote((Long) timestamp);
				}
			}
		});
		
	}
	
	public void onNewNoteButtonClick(View v) {
		openNote(1393594645799l);
	}
	
	private void openNote(Long timestamp) {
		Log.d(TAG, "New note!");
		Intent intent = new Intent(this, NoteActivity.class);
		intent.putExtra(NoteActivity.EXTRA_NOTE_TIMESTAMP, timestamp);
		startActivity(intent);
	}

}
