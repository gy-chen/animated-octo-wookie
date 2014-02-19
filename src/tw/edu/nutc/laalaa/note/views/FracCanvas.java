package tw.edu.nutc.laalaa.note.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class FracCanvas extends ImageView {

	private final String TAG = "FracCanvas";

	private Paint mPaint;

	{
		mPaint = new Paint();
		mPaint.setTextAlign(Paint.Align.CENTER);
	}

	public FracCanvas(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public FracCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public FracCanvas(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d(TAG, "onDraw");

		canvas.drawColor(0xffffff66);
		canvas.drawText("FracCanvas", getWidth() / 2, getHeight() / 2, mPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.d(TAG, String.format("onMeasure: (%d, %d)", widthMeasureSpec,
				heightMeasureSpec));
		Log.d(TAG, String.format("view size: (%d, %d", getWidth(), getHeight()));
		setMeasuredDimension(getResources().getDisplayMetrics().widthPixels,
				getResources().getDisplayMetrics().heightPixels / 4);
	}
}
