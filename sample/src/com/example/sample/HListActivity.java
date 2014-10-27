/**
 * Project : sample
 * File : TestActivity.java
 * 
 * The MIT License
 * Copyright (c) 2014 QiJiang.Guo (qijiang.guo@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package com.example.sample;

import com.guo.android_extend.widget.AbsHAdapterView;
import com.guo.android_extend.widget.ExtImageView;
import com.guo.android_extend.widget.HListView;
import com.guo.android_extend.widget.AbsHAdapterView.OnScrollListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HListActivity extends Activity implements OnItemClickListener,
	OnItemLongClickListener, OnClickListener, OnScrollListener {
	
	HListView mHListView;
	ViewList1Adapter mAdapter1;
	ViewList2Adapter mAdapter2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_hlist);
		mAdapter1 = new ViewList1Adapter(this);
		mAdapter2 = new ViewList2Adapter(this);
		
		mHListView = (HListView) this.findViewById(R.id.hlist);
		mHListView.setAdapter(mAdapter1);
		mHListView.setOnItemClickListener(this);
		mHListView.setOnItemLongClickListener(this);
		mHListView.setOnScrollListener(this);
		
		//mHListView.scrollTo(1422);
		mHListView.scrollToItem(5);
		
		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0.5F, 
				Animation.RELATIVE_TO_SELF, 0);
		animation.setDuration(1000);
		animation.setFillAfter(true);
		
		LayoutAnimationController controller = new LayoutAnimationController(animation);
		controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
		controller.setInterpolator(new AccelerateInterpolator());
		mHListView.setLayoutAnimation(controller );
		
		View v = this.findViewById(R.id.button1);
		v.setOnClickListener(this);
		v = this.findViewById(R.id.button2);
		v.setOnClickListener(this);
		v = this.findViewById(R.id.button3);
		v.setOnClickListener(this);
		v = this.findViewById(R.id.button4);
		v.setOnClickListener(this);
	}
	
	class Holder {
		ExtImageView siv;
		TextView tv;
		int id;
	}
	
	class ViewList1Adapter extends BaseAdapter {
		Context mContext;
		LayoutInflater mLInflater;
		int key = 0;
		String[] mNames = 
			/*{
				"Test1","Test2","Test3",
				"Test4","Test5","Test6",
				"Test7","Test8","Test9",
				"Test10","Test11","Test12",
				"Test13","Test14","Test15",
				"Test16","Test17","Test18",
				"Test19","Test20","Test21",
			};*/
		/*{
			"Test1","Test2","Test3",
			"Test4","Test5","Test6",
		};*/
		
		{
			"Test1","Test2","Test3",
		};
		
		public ViewList1Adapter(Context c) {
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
			
			holder.id = position;
			holder.tv.setText("T1->"+ key + " | " + mNames[ position ]);
			holder.siv.setBackgroundColor(Color.CYAN);
			
			convertView.setWillNotDraw(false);
			
			return convertView;
		}
		
	}
	
	class ViewList2Adapter extends BaseAdapter {
		Context mContext;
		LayoutInflater mLInflater;
		int key = 0;
		String[] mNames = 
			{
				"Test1","Test2","Test3",
				"Test4","Test5","Test6",
				"Test7","Test8","Test9",
				"Test10","Test11","Test12",
				"Test13","Test14","Test15",
				"Test16","Test17","Test18",
				"Test19","Test20","Test21",
			};
		/*{
			"Test1","Test2","Test3",
			"Test4","Test5","Test6",
		};*/
		
		/*{
			"Test1","Test2","Test3",
		};*/
		
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
			
			holder.id = position;
			holder.tv.setText("T2->"+ key + " | " + mNames[ position ]);
			holder.siv.setBackgroundColor(Color.RED);
			
			convertView.setWillNotDraw(false);
			
			return convertView;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Log.d("onItemClick", "onItemClick = " + arg2 + "pos=" + mHListView.getScroll());
		Holder holder = (Holder) arg1.getTag();
		holder.siv.setBackgroundColor(Color.BLUE);
		
		mHListView.scrollSmoothToItem(arg2);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		Log.d("onItemClick", "onItemLongClick = " + arg2);
		Holder holder = (Holder) arg1.getTag();
		holder.siv.setBackgroundColor(Color.GREEN);
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.d("onClick", "onClick");
		if (v.getId() == R.id.button1) {
			if (mHListView.getAdapter() == mAdapter2) {
				mHListView.setAdapter(mAdapter1);
			} else if (mHListView.getAdapter() == mAdapter1){
				mHListView.setAdapter(null);
			} else {
				mHListView.setAdapter(mAdapter2);
			}
			
		} else if (v.getId() == R.id.button2) {
			String[] p = mAdapter1.mNames;
			mAdapter1.mNames = mAdapter2.mNames;
			mAdapter2.mNames = p;
			
			mAdapter1.notifyDataSetChanged();
			mAdapter2.notifyDataSetChanged();
		} else if (v.getId() == R.id.button3) {
			mAdapter1.key++;
			mAdapter2.key++;
			mAdapter1.notifyDataSetInvalidated();
			mAdapter2.notifyDataSetInvalidated();
		} else if (v.getId() == R.id.button4) {
			mHListView.enableOverScroll(!mHListView.isOverScrollEnable());
		}
	}

	@Override
	public void onScrollStateChanged(AbsHAdapterView view, int scrollState) {
		// TODO Auto-generated method stub
		Log.d("onScrollStateChanged", "scrollState=" + scrollState);
	}

	@Override
	public void onScroll(AbsHAdapterView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		//Log.d("onScroll", "first=" + firstVisibleItem + ",visible =" + visibleItemCount + ",total=" + totalItemCount);
		
	}
}
