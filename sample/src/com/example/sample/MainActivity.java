package com.example.sample;

import java.util.ArrayList;
import java.util.List;

import com.guo.android_extend.CustomOrientationDetector;
import com.guo.android_extend.CustomOrientationDetector.OnOrientationListener;
import com.guo.android_extend.widget.ExtRelativeLayout;
import com.guo.android_extend.widget.HorizontalListView;
import com.guo.android_extend.widget.HorizontalListView.OnItemScrollListener;
import com.guo.android_extend.widget.ExtImageView;
import com.guo.android_extend.effect.HLVEffectAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity" ;

	/**
	 * scale percent.
	 */
	float percent_add = 0.5f;
	
	CustomOrientationDetector mODetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		HorizontalListView hv = (HorizontalListView) this.findViewById(R.id.listView1);
		ViewListAdapter vla = new ViewListAdapter(hv);
		hv.setAdapter(vla);
		hv.setOnItemScrollListener(vla); //new HScrollListener()
		hv.setOnItemClickListener(vla);
		hv.setSelection(0);
		
		ViewList2Adapter vla2 = new ViewList2Adapter(this);
		HorizontalListView hv2 = (HorizontalListView) this.findViewById(R.id.listView2);
		hv2.setAdapter(vla2);
		hv2.setOnItemScrollListener(new HScrollListener2());
		hv2.setOnItemClickListener(vla2);
		
		
		mODetector = new CustomOrientationDetector(this);
		mODetector.enable();
	}

	private void scale(View v, float percent) {
		Holder holder = (Holder) v.getTag();
		if (holder.siv.setScale(percent, percent)) {
			holder.siv.invalidate();
		}
	}
	
	private void scale2(View v, float percent) {
		ExtRelativeLayout ert = (ExtRelativeLayout) v;
		ert.setScale(percent, percent);
		ert.invalidate();
	}
	
	class Holder {
		ExtImageView siv;
		TextView tv;
		int id;
	}
	
	class ViewListAdapter extends HLVEffectAdapter implements OnItemClickListener {
		
		LayoutInflater mLInflater;
		List<String> mNames;

		public ViewListAdapter(HorizontalListView context) {
			super(context);
			// TODO Auto-generated constructor stub
			mLInflater = LayoutInflater.from(context.getContext());
			mNames = new ArrayList<String>();
			for (int i = 0; i < 20; i++) {
				mNames.add("Test " + i);
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mNames.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Holder holder = null;
			if (convertView != null) {
				holder = (Holder) convertView.getTag();
			} else {
				convertView = mLInflater.inflate(R.layout.item_sample, null);
				holder = new Holder();
				holder.siv = (ExtImageView) convertView.findViewById(R.id.imageView1);
				holder.tv = (TextView) convertView.findViewById(R.id.textView1);
				convertView.setTag(holder);
			}
			
			holder.tv.setText(mNames.get(position));
			holder.id = position;
			mODetector.addReceiver(holder.siv);

			scale(convertView, SCAEL_PERCENT);
			
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Toast t = Toast.makeText(MainActivity.this, mNames.get(arg2), Toast.LENGTH_SHORT);
			t.show();
		}

		@Override
		public void frashViewList() {
			// TODO Auto-generated method stub
			notifyDataSetChanged();
		}

		@Override
		public void scaleView(View v, float percent) {
			// TODO Auto-generated method stub
			scale(v, percent);
		}
		
		@Override
		public void animaView(View v, Animation ani) {
			// TODO Auto-generated method stub
			Holder holder = (Holder) v.getTag();
			holder.siv.startAnimation(ani);
		}
		
		@Override
		public void animaClearView(View v) {
			// TODO Auto-generated method stub
			Holder holder = (Holder) v.getTag();
			holder.siv.clearAnimation();
		}

		@Override
		public int removeView(View v) {
			// TODO Auto-generated method stub
			int id = 0;
			Holder holder = (Holder) v.getTag();
			if (holder.id + 1 >= mNames.size()) {
				id = holder.id - 1;
			} else {
				id = holder.id;
			}
			if (id < 0) {
				holder.id = 0;
			}
			if (mNames.size() == 1) {
				id = holder.id;
			} else {
				mNames.remove(holder.id);
			}
			return id;
		}
	}
	
	class ViewList2Adapter extends BaseAdapter implements OnItemClickListener {
		Context mContext;
		LayoutInflater mLInflater;
		String[] mNames = {
				"ImageViewTouch",
				"Test1","Test6","Test6",
				"Test2","Test6","Test6",
				"Test3","Test6","Test6",
				"Test4","Test6","Test6",
				"Test5","Test6","Test6",
				"Test6","Test6","Test6",
		};
		
		public ViewList2Adapter(Context c) {
			// TODO Auto-generated constructor stub
			mContext = c;
			mLInflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mNames.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Holder holder = null;
			if (convertView != null) {
				holder = (Holder) convertView.getTag();
			} else {
				convertView = mLInflater.inflate(R.layout.item_sample2, null);
				holder = new Holder();
				holder.siv = (ExtImageView) convertView.findViewById(R.id.imageView1);
				holder.tv = (TextView) convertView.findViewById(R.id.textView1);
				convertView.setTag(holder);
			}
			
			holder.tv.setText(mNames[ position ]);
			
			convertView.setWillNotDraw(false);
			mODetector.addReceiver((OnOrientationListener) convertView);
			
			scale2(convertView, 1f - percent_add);
			
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Toast t = Toast.makeText(MainActivity.this, mNames[ arg2 ], Toast.LENGTH_SHORT);
			t.show();
			if (arg2 == 0) {
				Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);
				startActivity(intent);
			}
		}
		
	}
	/*
	class HScrollListener implements OnItemScrollListener {

		@Override
		public void OnScrollCenter(AdapterView<ListAdapter> adp, View v,
				int pos, float percent) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnScrollCenter pos=" + pos + ", percent=" + percent);
			float scale = (1f - percent_add) + percent_add * percent;
			scale(v, scale);
			if (adp.getChildCount() > pos + 1) {
				scale(adp.getChildAt(pos + 1), 1f + percent_add - scale);
			}
			if (pos > 0) {
				scale(adp.getChildAt(pos - 1), 1f - percent_add);
			}
			
		}

		@Override
		public void OnScrollStart(AdapterView<ListAdapter> adp) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnScrollStart");
		}

		@Override
		public void OnScrollEnd(AdapterView<ListAdapter> adp, int pos) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnScrollEnd pos=" + pos);
			mODetector.forceOrientationChanged();
		}

		@Override
		public boolean OnDraging(AdapterView<ListAdapter> adp, float dx,
				float dy) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnDraging dx=" + dx + ", dy=" + dy);
			return false;
		}

		@Override
		public boolean OnDragingOver(AdapterView<ListAdapter> adp) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnDragingOver");
			return true;
		}
		
	}
	*/
	class HScrollListener2 implements OnItemScrollListener {

		@Override
		public void OnScrollCenter(AdapterView<ListAdapter> adp, View v,
				int pos, float percent) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnScrollCenter pos=" + pos + ", percent=" + percent);
			float scale = (1f - percent_add) + percent_add * percent;
			scale2(v, scale);
			if (adp.getChildCount() > pos + 1) {
				scale2(adp.getChildAt(pos + 1), 1f + percent_add - scale);
			}
			if (pos > 0) {
				scale2(adp.getChildAt(pos - 1), 1f - percent_add);
			}
			
		}

		@Override
		public void OnScrollStart(AdapterView<ListAdapter> adp) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnScrollStart");
		}

		@Override
		public void OnScrollEnd(AdapterView<ListAdapter> adp, int pos) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnScrollEnd pos=" + pos);
			mODetector.forceOrientationChanged();
		}

		@Override
		public boolean OnDraging(AdapterView<ListAdapter> adp, float dx,
				float dy) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnDraging dx=" + dx + ", dy=" + dy);
			return false;
		}

		@Override
		public boolean OnDragingOver(AdapterView<ListAdapter> adp) {
			// TODO Auto-generated method stub
			Log.i(TAG, "OnDragingOver");
			return true;
		}
		
	}
	
}
