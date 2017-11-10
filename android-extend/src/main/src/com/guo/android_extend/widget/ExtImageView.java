package com.guo.android_extend.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.guo.android_extend.widget.ExtOrientationDetector.OnOrientationListener;
import com.guo.android_extend.widget.controller.AbstractController;
import com.guo.android_extend.widget.controller.AbstractController.ControllerListener;

/**
 * @author gqj3375
 * @see ExtImageButton
 * 
 * rotatable and scalable.
 * background will not rotate and scale .
 */

public class ExtImageView extends ImageView implements ControllerListener, OnOrientationListener, AnimationListener {
	private final String TAG = this.getClass().toString();
	
	private Handler	mHandler;
	
	/**
	 * animation during time.
	 */
	private final int ANIMATION_TIME = RotateRunable.ANIMATION_TIME;
	
	/**
	 * for animation .
	 */
	private int mCurDegree;
	private int mPreDegree;
	
	/**
	 * for scale.
	 */
	private float scaleX, scaleY;
	private boolean isEnableScale;
	/**
	 * for touchable.
	 */
	private AbstractController mImageCtrl;
	
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
		mPreDegree = -90;
		scaleX = 1.0f;
		scaleY = 1.0f;
		isEnableScale = true;
		
		mImageCtrl = null;
	
	}

	@Override
	public boolean OnOrientationChanged(int degree, int offset, int flag) {
		// TODO Auto-generated method stub
		if (flag != ExtOrientationDetector.ROTATE_FORCE_REDO) {
			if (!this.isShown()) {
				Log.i(TAG, "Not Shown!");
				return false;
			}
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
		if (mImageCtrl != null && mCurDegree != mPreDegree) {
			//if rotate 90 degree , exchange the width and height, reinit the controller.
			if (mCurDegree == 0 || mCurDegree == 180) {
				mImageCtrl.initialize( getDrawable().getBounds().width(), 
									   getDrawable().getBounds().height(),
									   canvas.getWidth(), 
									   canvas.getHeight());
			} else {
				mImageCtrl.initialize(getDrawable().getBounds().height(), 
						   getDrawable().getBounds().width(),
						   canvas.getWidth(), 
						   canvas.getHeight());
			}
			mPreDegree = mCurDegree;
		}
		
		if (mImageCtrl != null) {
			mImageCtrl.beforeDraw(canvas);
		}
			
		canvas.save();
		canvas.scale(scaleX, scaleY, this.getWidth() / 2f, this.getHeight() / 2f);
		canvas.rotate(-mCurDegree, this.getWidth() / 2f, this.getHeight() / 2f);
		super.onDraw(canvas);
		canvas.restore();
		
		if (mImageCtrl != null) {
			mImageCtrl.afterDraw(canvas);
		}
	}
	
	@Override
	public PointF getCenterPoint() {
		// TODO Auto-generated method stub
		RectF bounds = new RectF();
		bounds.set(this.getDrawable().getBounds());
		this.getImageMatrix().mapRect(bounds);
		return new PointF(bounds.centerX(), bounds.centerY());
	}

	/**
	 * @param isEnable enable scale.
	 */
	public void enableScale(boolean isEnable) {
		isEnableScale = isEnable;
	}
	
	/**
	 * set scale percent.
	 * @param sx scale x
	 * @param sy scale y
	 * @return  set ok is true.
	 */
	public boolean setScale(float sx, float sy) {
		if (isEnableScale) {
			scaleX = sx;
			scaleY = sy;
			return true;
		}
		return false;
	}
	
	/**
	 * @return the mImageCtrl
	 */
	public AbstractController getImageCtrl() {
		return mImageCtrl;
	}

	/**
	 * @param mImageCtrl the mImageCtrl to set
	 */
	public void setImageCtrl(AbstractController mImageCtrl) {
		this.mImageCtrl = mImageCtrl;
		this.setOnTouchListener(mImageCtrl);
	}

}
