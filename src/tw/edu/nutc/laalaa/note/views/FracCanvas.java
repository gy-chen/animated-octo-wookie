package tw.edu.nutc.laalaa.note.views;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * FracCanvas
 * 
 * 可使用觸控繪圖的畫布
 * 
 * @author Guanyuo <gy.chen@gms.nutc.edu.tw>
 * 
 */
public class FracCanvas extends ImageView {

	private final String TAG = "FracCanvas";

	private final LinkedList<Path> mPaths = new LinkedList<Path>();
	private Path mCurrentPath = null;
	private RectF mBounds;
	private boolean mIsDrawing = false;
	private OnDrawListener mOnDrawListener;

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

	/**
	 * 使用者是否正在此畫布繪圖
	 * 
	 * @return true代表是，否則為不是
	 */
	public boolean isDrawing() {
		Log.d(TAG, "Check Drawing status " + mIsDrawing);
		return mIsDrawing;
	}

	/**
	 * 新增繪圖事件Listener
	 * 
	 * 當有繪圖事件發生時，會通知此Listener
	 * 
	 * @param listener
	 */
	public void setOnDrawListener(OnDrawListener listener) {
		mOnDrawListener = listener;
	}

	/**
	 * 取得目前的繪圖事件Listener
	 * 
	 * 如果目前沒有listener存在，則回傳null
	 * 
	 * @return
	 */
	public OnDrawListener getOnDrawListener() {
		return mOnDrawListener;
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawColor(0xffffff66);
		super.onDraw(canvas);
		
		for (Path path : mPaths) {
			canvas.drawPath(path, mPaint);
		}
		
		canvas.drawRect(mBounds, mPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, String.format("%s onTouchEvent", getId()));

		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (mCurrentPath == null) {
				mIsDrawing = true;
				if (mOnDrawListener != null) {
					mOnDrawListener.onStartDraw();
				}
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
			mIsDrawing = false;
			if (mOnDrawListener != null) {
				mOnDrawListener.onStopDraw();
			}
			mCurrentPath = null;
			break;
		}
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.d(TAG, String.format("onMeasure: (%d, %d)", getMeasuredWidth(),
				getMeasuredHeight()));
		Log.d(TAG, String.format("view size: (%d, %d", getWidth(), getHeight()));
		
		setMeasuredDimension(getMeasuredWidth(),
				getResources().getDisplayMetrics().heightPixels / 4);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		Log.i(TAG, "onSizeChanged w: " + w);
		Log.i(TAG, "onSizeChanged h: " + h);
		Log.i(TAG, "onSizeChanged oldw: " + oldw);
		Log.i(TAG, "onSizeChanged oldh: " + oldh);

		float xPadding = getPaddingLeft() + getPaddingRight();
		float yPadding = getPaddingTop() + getPaddingBottom();

		float ww = w - xPadding;
		float hh = h - yPadding;

		mBounds = new RectF(0, 0, ww - 1, hh - 1);
		mBounds.offsetTo(getPaddingLeft(), getPaddingRight());

		Log.i(TAG, "left: " + mBounds.left);
		Log.i(TAG, "right: " + mBounds.right);
		Log.i(TAG, "top: " + mBounds.top);
		Log.i(TAG, "bottom: " + mBounds.bottom);
	}

	/**
	 * OnDrawListener
	 * 
	 * onStartDraw() 當使用者開始下筆時，會呼叫此方法 onStopDraw() 當使用者停筆時，會呼叫此方法
	 * 
	 */
	public static interface OnDrawListener {
		void onStartDraw();

		void onStopDraw();
	}
}
