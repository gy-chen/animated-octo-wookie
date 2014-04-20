package tw.edu.nutc.laalaa.note.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class NoteButtonView extends TextView {

	public NoteButtonView(Context context) {
		super(context);
		init();
	}

	public NoteButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NoteButtonView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		setBackgroundResource(android.R.drawable.btn_default);
	}

	public void setTitle(String noteTitle, long noteTimestamp) {
		Date date = new Date(noteTimestamp);
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		setText(noteTitle + ":\n " + dateFormat.format(date));
	}
	
	public String getTitle() {
		return getText().toString();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
}
