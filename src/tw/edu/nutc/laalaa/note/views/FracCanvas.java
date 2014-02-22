package tw.edu.nutc.laalaa.note.views;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class FracCanvas extends ImageView {

	private final String TAG = "FracCanvas";
	
	private final LinkedList<Path> mPaths = new LinkedList<Path>();
	private Path mCurrentPath = null;

	private Paint mPaint;

	{
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(5f);
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
		for (Path path : mPaths) {
			canvas.drawPath(path, mPaint);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, String.format("%s onTouchEvent", getId()));
		
		int action = event.getAction();
		switch(action) {
		case MotionEvent.ACTION_DOWN:
			if (mCurrentPath == null) {
				mCurrentPath = new Path();
				mCurrentPath.moveTo(event.getX(), event.getY());
				mPaths.add(mCurrentPath);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			assert mCurrentPath != null;
			mCurrentPath.lineTo(event.getX(), event.getY());
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mCurrentPath = null;
			break;
		}
		return true;
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
