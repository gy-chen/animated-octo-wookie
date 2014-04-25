package tw.edu.nutc.laalaa.note;

import java.util.Date;

import tw.edu.nutc.laalaa.note.adapters.NoteAdatper;
import tw.edu.nutc.laalaa.note.datastore.NoteStorage;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;

public class MainActivity extends FragmentActivity {

	public final static String TAG = "MainActivity";
	public final static String DIALOG_NEW_NOTE_TAG = "new_note";

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);

		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new NoteAdatper(this));
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// 開啟Note
				Object timestamp = v.getTag();
				if (timestamp == null) {
					new NewNoteDialogFragment().show(
							getSupportFragmentManager(), DIALOG_NEW_NOTE_TAG);
				} else {
					openNote((Long) timestamp);
				}
			}
		});

	}

	private void openNote(Long timestamp) {
		Log.d(TAG, "New note!");
		Intent intent = new Intent(this, NoteActivity.class);
		intent.putExtra(NoteActivity.EXTRA_NOTE_TIMESTAMP, timestamp);
		startActivity(intent);
	}

	private void setNoteTitle(Long timestamp, String noteTitle) {
		NoteStorage storage = new NoteStorage(this, timestamp);
		storage.createNote();
		storage.setTitle(noteTitle);
	}

	private class NewNoteDialogFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			final View dialogView = inflater.inflate(
					R.layout.dialog_noteinformation, null);
			builder.setView(dialogView).setTitle("建立註記簿")
					.setPositiveButton("建立", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							EditText noteTitle = (EditText) dialogView
									.findViewById(R.id.note_title);
							Long timestamp = new Date().getTime();
							setNoteTitle(timestamp, noteTitle.getText()
									.toString());
							openNote(timestamp);
						}
					});
			return builder.create();
		}
	}
}
