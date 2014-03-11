package com.guo.android_extend.rotate;

import com.guo.android_extend.CustomOrientationDetector;
import com.guo.android_extend.CustomOrientationDetector.OnOrientationListener;

import android.content.Context;
import android.graphics.Canvas;
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
 * this class is used for rotate image button.
 * there is two method can achieve the target.
 * 1.Rotate the layout:
 * 	 before the button start animation:
 *   @see com.guo.android_extend.rotate.RotateRunable
 *   API Level 11+:
 *   View.setVisibility(View.GONE);
 *	 View.setPivotX(RotatableImageButton.this.getWidth() / 2);
 *	 View.setPivotY(RotatableImageButton.this.getHeight() / 2);
 *   View.setRotation(-mCurDegree);
 *   View.setVisibility(View.VISIBLE);
 *   
 * 2.Rotate the view:
 *   before the button draw on screen:
 *   Code at line 141:
 *   canvas.rotate(-mCurDegree, canvas.getWidth() / 2, canvas.getHeight() / 2);	
 *   
 * in current class, because of the popupWindow is relative with the button layout.
 * if we use method 1. the popuWindow's showAsDropDown will relative with the screen's 
 * orientation, and will not show in expectation position.
 */

public class RotatableImageButton extends ImageButton implements OnOrientationListener, AnimationListener {
	private final String TAG = this.getClass().toString();
	
	private Handler	mHandler;
	
	/**
	 * animation during time.
	 */
	private final int ANIMATION_TIME = 200;
	
	/**
	 * for animation .
	 */
	private int mCurDegree;
	
	public RotatableImageButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		preCreate(context);
	}

	public RotatableImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		preCreate(context);
	}

	public RotatableImageButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		preCreate(context);
	}

	private void preCreate(Context context) {
		mHandler = new Handler();
		mCurDegree = 0;
	}

	@Override
	public void OnOrientationChanged(int degree, int offset, int flag) {
		// TODO Auto-generated method stub
		if (this.getVisibility() == View.GONE) {
			mCurDegree = degree;
			return ;
		}
		if (flag == CustomOrientationDetector.ROTATE_NEGATIVE) {
			Animation mRotateNegative = new RotateAnimation (-offset, 0,
					Animation.RELATIVE_TO_SELF, 0.5f, 
					Animation.RELATIVE_TO_SELF, 0.5f);
			mRotateNegative.setDuration(ANIMATION_TIME);
			mRotateNegative.setFillAfter(true);
			mHandler.post(new RotateRunable(mRotateNegative, this, degree, false));
			
		} else if (flag == CustomOrientationDetector.ROTATE_POSITIVE) {
			Animation mRotatePositive = new RotateAnimation (offset, 0,
					Animation.RELATIVE_TO_SELF, 0.5f, 
					Animation.RELATIVE_TO_SELF, 0.5f);
			mRotatePositive.setDuration(ANIMATION_TIME);
			mRotatePositive.setFillAfter(true);
			mHandler.post(new RotateRunable(mRotatePositive, this, degree, false));
			
		} else {
			Log.i(TAG, "NO CHANGE");
		}
		mCurDegree = degree;
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

	/* (non-Javadoc)
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if(this.getVisibility() != View.GONE) {
			canvas.save();
			if (!ROTATE_LAYOUT) {
				canvas.rotate(-mCurDegree, this.getWidth() / 2, this.getHeight() / 2);
			}
			super.onDraw(canvas);
			canvas.restore();
		} else {
			super.onDraw(canvas);
		}
	}
	
	/**
	 * @return the mCurDegree
	 */
	public int getCurDegree() {
		return mCurDegree;
	}

	/**
	 * @param mCurDegree the mCurDegree to set
	 */
	public void setCurDegree(int mCurDegree) {
		this.mCurDegree = mCurDegree;
	}

}
