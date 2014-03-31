package tw.edu.nutc.laalaa.note.views;

import tw.edu.nutc.laalaa.note.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class FracImageView extends ImageView {

	private Animation mRotatingAnimation = AnimationUtils.loadAnimation(
			getContext(), R.anim.rotate_around);

	public FracImageView(Context context) {
		super(context);
		init();
	}

	public FracImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FracImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		if (getDrawable() == null) {
			startRotateAnimation();
		}
	}

	public void startRotateAnimation() {
		startAnimation(mRotatingAnimation);
	}

	public void stopRotateAnimation() {
		mRotatingAnimation.cancel();
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		stopRotateAnimation();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		stopRotateAnimation();
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		stopRotateAnimation();
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		stopRotateAnimation();
	}
}
