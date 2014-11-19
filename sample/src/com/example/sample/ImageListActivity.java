package com.example.sample;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guo.android_extend.cache.BitmapMonitor;
import com.guo.android_extend.cache.BitmapMonitorThread;
import com.guo.android_extend.widget.ExtImageView;


/**
 * A list view example where the 
 * data for the list comes from an array of strings.
 */
public class ImageListActivity extends ListActivity implements OnItemClickListener , FilenameFilter{

	private BitmapMonitorThread<ExtImageView, String> mCacheThread;
	
	private ListImage mListImage;
	private String mImagePath;
	
	public class Holder {
		ExtImageView siv;
		TextView tv;
		int id;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mImagePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
        File dir = new File(mImagePath);
        File[] files = null;
        if (dir.exists()) {
			files = dir.listFiles(this);
        } else {
        	files = new File[0];
        }
     
        mListImage = new ListImage(this, files);
        setListAdapter(mListImage);
		getListView().setOnItemClickListener(this);
        mCacheThread = new BitmapMonitorThread<ExtImageView, String>(new Handler());
        mCacheThread.start();
        
    }
    
    private class ListImage extends BaseAdapter {
    	
    	private class Data {
    		String name;
    		String path;
    		public Data(String name, int id, String path) {
    			this.name = name;
    			this.path = path;	
    		}
    	}
    	
    	private class Mointor extends BitmapMonitor<ExtImageView, String> {

    		public Mointor(ExtImageView view, String id) {
				super(view, id);
				// TODO Auto-generated constructor stub
			}

    		@Override
    		public Bitmap decodeImage() {
    			// TODO Auto-generated method stub
    			super.mBitmap = null;
    			try {
    				BitmapFactory.Options op = new BitmapFactory.Options();    
    		        op.inJustDecodeBounds = true;
    		        super.mBitmap = BitmapFactory.decodeFile(super.mBitmapID, op);
    		        op.inJustDecodeBounds = false;
    		        int h = op.outHeight;  
    		        int w = op.outWidth;  
    		        int beWidth = w / 320;  
    		        int beHeight = h / 240;  
    		        int be = 1;  
    		        if (beWidth < beHeight) {  
    		            be = beWidth;  
    		        } else {  
    		            be = beHeight;  
    		        }  
    		        if (be <= 0) {  
    		            be = 1;  
    		        }  
    		        op.inSampleSize = be;  
    		        super.mBitmap = BitmapFactory.decodeFile(super.mBitmapID, op);  
    		        super.mBitmap = ThumbnailUtils.extractThumbnail(super.mBitmap, 320, 240,  
    		                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
    			} catch (Exception e) {
    		    	e.printStackTrace();
    		    }
    			return super.mBitmap;
    		}

			@Override
			protected void freshBitmap(boolean isOld) {
				// TODO Auto-generated method stub
				super.mView.setImageResource(0);
    			super.mView.setImageBitmap(null);
    			if (!isOld && super.mBitmap != null) {
	    			super.mView.setImageBitmap(super.mBitmap);
    			} else {
    				super.mView.setImageResource(R.drawable.ic_launcher);
    			}
				super.mView.invalidate();
			}

        }
    	
    	List<Data> mData;
    	LayoutInflater mLInflater;
    	
		public ListImage(Context context, File[] files) {
			super();
			mLInflater = LayoutInflater.from(context);
			mData = new ArrayList<Data>();
			for (int i = 0; i < files.length; i++) {
				mData.add(new Data(files[i].getName(), i, files[i].getAbsolutePath()));
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mData.get(position);
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
			
			Data data = mData.get(position);
			
			holder.tv.setText(data.name);
			holder.siv.setImageResource(R.drawable.ic_launcher);
			
			mCacheThread.postLoadBitmap(new Mointor(holder.siv, data.path));
			return convertView;
		}
    	
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		String full = ((ListImage.Data)mListImage.getItem(position)).path;
		Log.d("Image", full);
		Intent intent = new Intent(ImageListActivity.this, ImageViewActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("imagePath", full);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public boolean accept(File arg0, String arg1) {
		// TODO Auto-generated method stub
		return arg1.endsWith(".jpg") || arg1.endsWith(".png");
	}
    
}
