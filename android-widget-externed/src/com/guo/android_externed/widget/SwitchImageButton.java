package com.guo.android_externed.widget;

import com.guo.android_externed.widget.CustomOrientationDetector.OnOrientationListener;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;

/**
 * @author gqj3375
 * true / false the image resource use mRes[0] / mRes[1].
 * the specific resource will relative with the button status.
 * 
 * @note use setImageRes to specific the resource id.
 * 		 use getSwitchStatus to check the button's status. 
 * 		 use setSwitchStatus to set the resource.
 * 
 * @see RotatableImageButton.
 * @see RotateRunable.
 */
public class SwitchImageButton extends ImageButton implements OnOrientationListener, AnimationListener {
	private final String TAG = this.getClass().toString();

	private boolean mStatus = false;
	
	private int[] mRes;
	
	private Handler	mHandler;
	
	/**
	 * animation during time.
	 */
	private final int ANIMATION_TIME = 200;
	
	private SwitchOnClickListener mSwitchOnClickListener;
	
	private class SwitchOnClickListener implements OnClickListener {
		
		private OnClickListener mOnClickListener;
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mStatus = (mStatus ? false : true);
			if (mRes != null) {
				SwitchImageButton.this.setImageResource(mRes[mStatus ? 1 : 0]);
				SwitchImageButton.this.invalidate();
			}
			if (mOnClickListener != null) {
				mOnClickListener.onClick(v);
			}
		}
		
		public void setOnClickListener(OnClickListener l) {
			mOnClickListener = l;
		}
	}
	
	public SwitchImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		PreCreate(context);
	}

	public SwitchImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		PreCreate(context);
	}

	public SwitchImageButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		PreCreate(context);
	}
	
	private void PreCreate(Context context) {
		mHandler = new Handler();
		mSwitchOnClickListener = new SwitchOnClickListener();
	}
	
	/**
	 * set the res . res.length should be > 2
	 * res[0] is show for status true.
	 * res[1] is otherwise.
	 * @param res
	 */
	public void setImageRes(int[] res) {
		mRes = res;
	}
	
	/**
	 * get current status.
	 * @return
	 */
	public boolean getSwitchStatus() {
		return mStatus;
	}
	
	/**
	 * set status.
	 * @param status
	 */
	public void setSwitchStatus(boolean status) {
		mStatus = status;
		setImageResource(mRes[mStatus ? 1 : 0]);
	}

	/* (non-Javadoc)
	 * @see android.view.View#setOnClickListener(android.view.View.OnClickListener)
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		// TODO Auto-generated method stub
		mSwitchOnClickListener.setOnClickListener(l);
		super.setOnClickListener(mSwitchOnClickListener);
	}
	
	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		this.setEnabled(false);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		this.setEnabled(true);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnOrientationChanged(int degree, int offset, int flag) {
		// TODO Auto-generated method stub
		if (flag == CustomOrientationDetector.ROTATE_NEGATIVE) {
			Animation mRotateNegative = new RotateAnimation (-offset, 0,
					Animation.RELATIVE_TO_SELF, 0.5f, 
					Animation.RELATIVE_TO_SELF, 0.5f);
			mRotateNegative.setDuration(ANIMATION_TIME);
			mRotateNegative.setFillAfter(true);
			mHandler.post(new RotateRunable(mRotateNegative, this, degree, true));
			
		} else if (flag == CustomOrientationDetector.ROTATE_POSITIVE) {
			Animation mRotatePositive = new RotateAnimation (offset, 0,
					Animation.RELATIVE_TO_SELF, 0.5f, 
					Animation.RELATIVE_TO_SELF, 0.5f);
			mRotatePositive.setDuration(ANIMATION_TIME);
			mRotatePositive.setFillAfter(true);
			mHandler.post(new RotateRunable(mRotatePositive, this, degree, true));
		} else {
			Log.i(TAG, "NO CHANGE");
		}
	}
	
}
