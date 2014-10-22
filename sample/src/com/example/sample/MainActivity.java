package com.example.sample;

import com.guo.android_extend.CustomOrientationDetector;
import com.guo.android_extend.CustomOrientationDetector.OnOrientationListener;
import com.guo.android_extend.widget.ExtImageView;
import com.guo.android_extend.widget.HListView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	CustomOrientationDetector mODetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mODetector = new CustomOrientationDetector(this);
		mODetector.enable();
		
		HListView hv = (HListView) this.findViewById(R.id.listView1);
		ViewListAdapter vla = new ViewListAdapter(this);
		hv.setAdapter(vla);
		hv.setOnItemClickListener(vla);
		
	}
	
	class Holder {
		ExtImageView siv;
		TextView tv;
		int id;
	}
	
	class ViewListAdapter extends BaseAdapter implements OnItemClickListener {
		Context mContext;
		LayoutInflater mLInflater;
		String[] mNames = {
				"ImageViewTouch",
				"ListViewCache",
				"CustomWidget",
				"HListView",
				"Test2","Test6","Test6",
				"Test3","Test6","Test6",
				"Test4","Test6","Test6",
				"Test5","Test6","Test6",
				"Test6","Test6","Test6",
		};
		
		public ViewListAdapter(Context c) {
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
			} else if (arg2 == 1) {
				Intent intent = new Intent(MainActivity.this, ImageListActivity.class);
				startActivity(intent);
			} else if (arg2 == 2) {
				Intent intent = new Intent(MainActivity.this, WidgetActivity.class);
				startActivity(intent);
			} else if (arg2 == 3) {
				Intent intent = new Intent(MainActivity.this, HListActivity.class);
				startActivity(intent);
			}
		}
		
	}
	
}
