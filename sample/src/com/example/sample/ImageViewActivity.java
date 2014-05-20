package com.example.sample;

import com.guo.android_extend.CustomOrientationDetector;
import com.guo.android_extend.ImageViewController;
import com.guo.android_extend.widget.ExtImageView;

import android.app.Activity;
import android.os.Bundle;

public class ImageViewActivity extends Activity {
	private final String TAG = this.getClass().toString();

	CustomOrientationDetector mODetector;
	
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
		
		ExtImageView eiv = (ExtImageView) this.findViewById(R.id.imageView1);
		eiv.setImageCtrl(new ImageViewController(this, eiv));
		
		mODetector.addReceiver(eiv);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


}
