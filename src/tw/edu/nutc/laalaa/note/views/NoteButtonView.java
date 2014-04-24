package tw.edu.nutc.laalaa.note.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NoteButtonView extends RelativeLayout {

	private TextView mTextView;
	private ImageView mImageView;

	{
		init();
	}

	public NoteButtonView(Context context) {
		super(context);
	}

	public NoteButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoteButtonView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private void init() {
		mTextView = new TextView(getContext());
		mTextView.setId(4416);
		LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		addView(mTextView, textParams);

		mImageView = new ImageView(getContext());
		mImageView.setBackgroundResource(android.R.drawable.btn_default);
		mImageView.setId(4413);
		LayoutParams imageParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		imageParams.addRule(RelativeLayout.ABOVE, 4416);
		addView(mImageView, imageParams);
	}

	public void setTitle(String noteTitle) {
		mTextView.setText(noteTitle);
	}

	public String getTitle() {
		return mTextView.getText().toString();
	}

	public void setSnapshot(Bitmap snapshot) {
		mImageView.setImageBitmap(snapshot);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
}
