package tw.edu.nutc.laalaa.note.utils;

import java.io.File;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class LoadImageAsyncTask extends AsyncTask<File, Void, Bitmap> {

	private final static String TAG = "LoadImageAsyncTask";
	
	private WeakReference<ImageView> mImageViewReference;
	private int mReqWidth;

	public LoadImageAsyncTask(ImageView view, int reqWidth) {
		mImageViewReference = new WeakReference<ImageView>(view);
		mReqWidth = reqWidth;
	}

	@Override
	protected Bitmap doInBackground(File... params) {
		Log.d(TAG, "start loading photo file in background");
		File photoFile = params[0];

		// TODO: remove this
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// downsample photo file
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
		int inSampleSize = BitmapUtil
				.calculateInSampleWidth(options, mReqWidth);
		options.inJustDecodeBounds = false;
		options.inSampleSize = inSampleSize;
		Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(),
				options);
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		Log.d(TAG, "finish load photo file");
		if (mImageViewReference.get() != null) {
			mImageViewReference.get().setImageBitmap(bitmap);
		}
	}
}
