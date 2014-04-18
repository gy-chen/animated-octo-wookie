package tw.edu.nutc.laalaa.note.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class NoteAdatper extends BaseAdapter {

	public final static int TAG_TIMESTAMP = 4416;
	
	private Context mContext;

	public NoteAdatper(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		// TODO 使用NoteStorage的紀錄
		return 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 新增註記簿按鈕
		if (position == 0) {
			ImageView button = new ImageView(mContext);
			button.setImageResource(android.R.drawable.btn_star);
			button.setBackgroundResource(android.R.drawable.btn_default);
			button.setTag(TAG_TIMESTAMP, null);
			return button;
		}
		// TODO 讀取NoteStorage的資料
		return null;
	}

}
