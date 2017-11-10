package com.guo.android_extend.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.guo.android_extend.widget.controller.TouchController;

/**
 * create by gqjjqg,.
 * easy to use opengl surface..
 */

public class ExtSurfaceView extends SurfaceView implements ExtOrientationDetector.OnOrientationListener {
	private final String TAG = this.getClass().getSimpleName();

	private Handler mHandler;

	/**
	 * animation during time.
	 */
	private final int ANIMATION_TIME = RotateRunable.ANIMATION_TIME;

	private int mCurDegree;

	private double mAspectRatio;
	private boolean mFitMaxArea;
	/**
	 * for touch point rotate.
	 */
	private TouchController mTouchController;

	/**
	 * for dispatch touch event process.
	 */
	private TouchController.OnDispatchTouchEventListener mOnDispatchTouchEventListener;

	public ExtSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public ExtSurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	private void onCreate() {
		mAspectRatio = 0.0;
		mHandler = new Handler();
		mTouchController = new TouchController();
	}

	public void setAutoFitMax(boolean enable) {
		mFitMaxArea = enable;
	}

	public void setAspectRatio(double ratio) {
		if (mAspectRatio != ratio) {
			mAspectRatio = ratio;
			requestLayout();
		}
	}

	public void setAspectRatio(int width, int height) {
		double ratio = ((double)width / (double)height);
        if (mAspectRatio != ratio) {
            mAspectRatio = ratio;
            requestLayout();
        }
    }
	
	/* (non-Javadoc)
	 * @see android.view.SurfaceView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		// TODO Auto-generated method stub
		int width = MeasureSpec.getSize(widthSpec);
        int height = MeasureSpec.getSize(heightSpec);

        if (mAspectRatio != 0) {
			int w1 = width, h1 = height;
	        if (w1 > h1 * mAspectRatio) {
	            w1 = (int) (h1 * mAspectRatio + .5);
	        } else {
				h1 = (int) (w1 / mAspectRatio + .5);
	        }
			if (mFitMaxArea) {
				int w2 = width, h2 = height;
				double ratio = 1.0 / mAspectRatio;
				if (w2 > h2 * ratio) {
					w2 = (int) (h2 * ratio + .5);
				} else {
					h2 = (int) (w2 / ratio + .5);
				}
				if (w1 * h1 > w2 * h2) {
					width = w1;
					height = h1;
				} else {
					width = w2;
					height = h2;
				}
			} else {
				width = w1;
				height = h1;
			}
        }

		widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthSpec, heightSpec);
	}

	@Override
	public boolean OnOrientationChanged(int degree, int offset, int flag) {
		if (flag != ExtOrientationDetector.ROTATE_FORCE_REDO) {
			if (!this.isShown()) {
				Log.i(TAG, "Not Shown!");
				return false;
			}
		}

		Animation animation = new RotateAnimation(offset, 0,
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
		return mCurDegree;
	}

	/* (non-Javadoc)
	 * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		MotionEvent newEvent = ev;
		if (mTouchController != null) {
			newEvent = mTouchController.obtainTouchEvent(ev, this.getWidth(), this.getHeight(), mCurDegree);
		}
		// TODO Auto-generated method stub
		if (mOnDispatchTouchEventListener != null) {
			mOnDispatchTouchEventListener.onDispatchTouchEvent(this, ev);
		}
		return super.dispatchTouchEvent(newEvent);
	}

	public void setOnDispatchTouchEventListener(TouchController.OnDispatchTouchEventListener listener) {
		mOnDispatchTouchEventListener = listener;
	}

}
