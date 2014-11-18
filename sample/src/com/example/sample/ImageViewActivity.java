package com.example.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import com.guo.android_extend.CustomOrientationDetector;
import com.guo.android_extend.controller.ImageController;
import com.guo.android_extend.widget.ExtImageView;

public class ImageViewActivity extends Activity {

	private final String TAG = this.getClass().toString();

	String mFilePath;
	CustomOrientationDetector mODetector;
	ExtImageView eiv;
	Bitmap mBitmap;
	Rect rect;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_imageview);
		
		mODetector = new CustomOrientationDetector(this);
		mODetector.enable();
		
		eiv = (ExtImageView) this.findViewById(R.id.imageView1);
		eiv.setBackgroundColor(Color.BLACK);
		eiv.setImageCtrl(new ImageController(this, eiv));
		mODetector.addReceiver(eiv);
		
		//initial data.
		if (!getIntentData(getIntent().getExtras())) {
			Log.e(TAG, "getIntentData fail!");
			eiv.setImageResource(R.drawable.ic_launcher);
		} else {
			mBitmap = loadImage(mFilePath);
			eiv.setImageBitmap(mBitmap);
		}

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		rect = eiv.getDrawable().copyBounds();
		Log.d(TAG, "view w =" + eiv.getWidth() + ",h =" + eiv.getHeight());
		Log.d(TAG, "rect =" + rect.toString());
		if (null != mBitmap) {
			mBitmap.recycle();
		}
		super.onDestroy();
	}

	private boolean getIntentData(Bundle bundle) {
		try {
			mFilePath = bundle.getString("imagePath");
			if (mFilePath == null || mFilePath.isEmpty()) {
				return false;
			}
			Log.i(TAG, "getIntentData:" + mFilePath);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param path
	 * @param SWidth screen width
	 * @param SHeight screen height
	 * @return bitmap
	 */
	private Bitmap loadImage(String path) {
		Bitmap res;
		try {
			ExifInterface exif = new ExifInterface(path);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			
			BitmapFactory.Options op = new BitmapFactory.Options();    
	        op.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(path, op);
	        Log.d(TAG, "Image:" + op.outWidth + "X" + op.outHeight);
			
	        op.inSampleSize = 2;
	        op.inJustDecodeBounds = false;  
	        //op.inMutable = true;
	        res = BitmapFactory.decodeFile(path, op);
	        
	        //rotate and scale.
	        Matrix matrix = new Matrix();
        	if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
			    matrix.postRotate(90);
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
			    matrix.postRotate(180);
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
			    matrix.postRotate(270);
			}
        	Bitmap bmp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
        	Log.d(TAG, "check target Image:" + bmp.getWidth() + "X" + bmp.getHeight());

			return bmp;
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
		return null;
	}
}
