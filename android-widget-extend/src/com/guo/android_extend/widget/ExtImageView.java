package com.guo.android_extend.widget;

import com.guo.android_extend.RotateRunable;
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
import android.widget.ImageView;

/**
 * @author gqj3375
 * @see RotatableImageButton
 * 
 * @support rotatable and scalable.
 * @note background will not rotate and scale .
 */

public class ExtImageView extends ImageView implements OnOrientationListener, AnimationListener {
	private final String TAG = this.getClass().toString();
	
	private Handler	mHandler;
	
	/**
	 * animation during time.
	 */
	private final int ANIMATION_TIME = OnOrientationListener.ANIMATION_TIME;
	
	/**
	 * for animation .
	 */
	private int mCurDegree;
	
	/**
	 * for scale.
	 */
	private float scaleX, scaleY;
	
	public ExtImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		preCreate(context);
	}

	public ExtImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		preCreate(context);
	}

	public ExtImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		preCreate(context);
	}

	private void preCreate(Context context) {
		mHandler = new Handler();
		mCurDegree = 0;
		scaleX = 1.0f;
		scaleY = 1.0f;
	}

	@Override
	public boolean OnOrientationChanged(int degree, int offset, int flag) {
		// TODO Auto-generated method stub
		if (!this.isShown()) {
			Log.i(TAG, "Not Shown!");
			return false;
		}
		
		Animation animation = new RotateAnimation (offset, 0,
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(ANIMATION_TIME);
		animation.setFillAfter(true);
		mHandler.post(new RotateRunable(animation, this, degree));
		
		mCurDegree = degree;
		return true;
	}
	
	@Override
	public int getCurrentOrientationDegree() {
		// TODO Auto-generated method stub
		return mCurDegree;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		this.setVisibility(View.GONE);
		this.setEnabled(false);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		this.setVisibility(View.VISIBLE);
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
		canvas.save();
		canvas.scale(scaleX, scaleY, canvas.getWidth() / 2f, canvas.getHeight() / 2f);
		canvas.rotate(-mCurDegree, canvas.getWidth() / 2, canvas.getHeight() / 2);
		super.onDraw(canvas);
		canvas.restore();
	}
	
	/**
	 * set scale percent.
	 * @param sx
	 * @param sy
	 */
	public void setScale(float sx, float sy) {
		scaleX = sx;
		scaleY = sy;
	}

}
